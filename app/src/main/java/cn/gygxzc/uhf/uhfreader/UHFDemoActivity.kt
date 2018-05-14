package cn.gygxzc.uhf.uhfreader

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.EditText
import android.widget.LinearLayout
import cn.gygxzc.uhf.kotlin.flatSingleEvent
import cn.gygxzc.uhf.uhf.entity.TagInfo
import cn.gygxzc.uhf.uhf.model.IReadModel
import cn.gygxzc.uhf.uhf.model.IWriteModel
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.*

/**
 * @author niantuo
 * @createdTime 2018/5/14 15:09
 *
 *  标签的读写测试
 *  包括读EPC，TID，USER
 *  写EPC，USER 数据
 */
class UHFDemoActivity : AppCompatActivity(), AnkoLogger {

    private val mLoading: MaterialDialog by lazy { createLoadingDialog() }
    private val mReader: IReadModel by lazy { IReadModel.create() }
    private val mWriter: IWriteModel by lazy { IWriteModel.create() }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verticalLayout {
            linearLayout {
                orientation = LinearLayout.HORIZONTAL
                padding = dip(10)
                button("读EPC").setOnClickListener { readEPC() }
                        .apply {
                            padding = dip(10)
                            lparams(wrapContent, wrapContent)
                        }
                button("写EPC").setOnClickListener { writeEPC() }
                        .apply {
                            padding = dip(10)
                            lparams(wrapContent, wrapContent) {
                                leftMargin = dip(10)
                            }
                        }
                button("读TID").setOnClickListener { readTID() }
                        .apply {
                            padding = dip(10)
                            lparams(wrapContent, wrapContent) {
                                leftMargin = dip(10)
                            }
                        }
            }.lparams(matchParent, wrapContent)
            editText {
                id = R.id.uhf_epc
                padding = dip(10)
                singleLine = true
                textSize = 14f
            }.lparams(matchParent, wrapContent)

            editText {
                id = R.id.uhf_tid
                padding = dip(10)
                singleLine = true
                textSize = 14f
            }.lparams(matchParent, wrapContent)

            linearLayout {
                orientation = LinearLayout.HORIZONTAL
                padding = dip(10)
                button("读用户数据").setOnClickListener { readUser() }
                        .apply {
                            padding = dip(10)
                            lparams(wrapContent, wrapContent)
                        }
                button("写用户数据").setOnClickListener { writeToUser() }
                        .apply {
                            padding = dip(10)
                            lparams(wrapContent, wrapContent) {
                                leftMargin = dip(10)
                            }
                        }

            }.lparams(matchParent, wrapContent)
            editText {
                id = R.id.uhf_user
                padding = dip(10)
                setLines(2)
                textSize = 14f
            }
            textView {
                text = "输入密码"
                padding = dip(10)
                textSize = 15f
            }
            editText {
                id = R.id.uhf_pwd
                textSize = 14f
                padding = dip(10)
                setText("00000000")
            }
        }
    }


    private fun getPwd(): String {
        return find<EditText>(R.id.uhf_pwd).text.toString()
    }


    private fun readEPC() {
        mLoading.show()
        mReader.inventoryRealTime()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatSingleEvent()
                .doOnNext {
                    val editText = find<EditText>(R.id.uhf_epc)
                    editText.setText(it.epc)
                    editText.tag = it
                    mLoading.hide()
                    toast("读取成功")
                }
                .doOnError {
                    mLoading.hide()
                    error("标签读取失败->", it)
                }
                .subscribe()
    }

    private fun readTID() {
        mLoading.show()
        val tagInfo = checkEpc()
        if (tagInfo == null) {
            toast("请先选择标签")
            return
        }
        mReader.readTID(tagInfo.epc, getPwd())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatSingleEvent()
                .doOnNext {
                    find<EditText>(R.id.uhf_tid).setText(it)
                    mLoading.hide()
                }
                .doOnError {
                    longToast("读取失败")
                    error("读取失败", it)
                    mLoading.hide()
                }
                .subscribe()


    }

    private fun readUser() {
        mLoading.show()
        val tagInfo = checkEpc()
        if (tagInfo == null) {
            toast("请先选择标签")
            return
        }

        mReader.readUser(tagInfo.epc, getPwd()).subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatSingleEvent()
                .doOnNext {
                    find<EditText>(R.id.uhf_user).setText(it)
                    mLoading.hide()
                }
                .doOnError {
                    mLoading.hide()
                    toast("读取失败")
                    error("读取失败", it)
                }
                .subscribe()
    }

    private fun writeEPC() {

    }

    private fun writeToUser() {

    }

    private fun checkEpc(): TagInfo? {
        return find<EditText>(R.id.uhf_epc).tag as? TagInfo
    }

    private fun createLoadingDialog(): MaterialDialog {
        return MaterialDialog.Builder(this)
                .title("正在加载")
                .progress(true, -1)
                .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        mLoading.dismiss()
    }
}