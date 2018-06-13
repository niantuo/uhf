package cn.gygxzc.uhf.uhfreader

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.widget.EditText
import android.widget.LinearLayout
import cn.gygxzc.uhf.kotlin.flatEvent
import cn.gygxzc.uhf.kotlin.flatSingleEvent
import cn.gygxzc.uhf.uhf.entity.TagInfo
import cn.gygxzc.uhf.uhf.model.IReadModel
import cn.gygxzc.uhf.uhf.model.IWriteModel
import cn.tonyandmoney.anko.adapter.KAdapter
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView

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

    private val mAdapter = KAdapter<TagInfo>()
    private var mTagInfo = TagInfo()

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
            }.lparams(matchParent, wrapContent)
            editText {
                id = R.id.uhf_epc
                padding = dip(10)
                singleLine = true
                textSize = 14f
            }.lparams(matchParent, wrapContent)

            button {
                text = "读TID"
                padding = dip(10)
                setOnClickListener { readTID() }
            }.lparams(wrapContent, wrapContent) {
                leftMargin = dip(10)
            }
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

            button {
                text = "批量读取EPC"
                setOnClickListener { multiReadEPC() }
                padding = dip(10)
            }.lparams(wrapContent, wrapContent) {
                leftMargin = dip(10)
            }

            recyclerView {
                layoutManager = LinearLayoutManager(context)
                mAdapter.bindToRecyclerView(this)
            }.lparams(matchParent, matchParent)
        }

        mAdapter.setItemView {
            linearLayout {
                orientation = LinearLayout.HORIZONTAL
                padding = dip(10)
                textView {
                    id = R.id.uhf_multi_epc
                    textSize = 13f
                    minHeight = dip(80)
                }.lparams(0, wrapContent, 1f)
                button {
                    id = R.id.tag_select
                    text = "选定"
                    padding = dip(10)
                }.lparams(wrapContent, wrapContent)
                lparams(matchParent, wrapContent)
            }
        }
                .setDataBind { holder, tagInfo ->
                    holder.setText(R.id.uhf_multi_epc, tagInfo.epc)
                    holder.addOnClickListener(R.id.tag_select)
                }
                .setOnItemChildClickListener { _, view, position ->
                    val entity = mAdapter.getItem(position) ?: return@setOnItemChildClickListener
                    if (view.id == R.id.tag_select) {
                        mTagInfo = entity
                    }
                }
    }


    private fun getPwd(): String {
        return find<EditText>(R.id.uhf_pwd).text.toString()
    }


    private fun multiReadEPC() {
        val dialog = MaterialDialog.Builder(this)
                .title("正在扫描")
                .content("请稍后")
                .positiveText("停止")
                .cancelable(false)
                .build()

        dialog.show()
        var disposable: Disposable? = null
        mAdapter.setNewData(mutableListOf())
        mReader.inventoryMulti()
                .doOnNext {
                    info("checked->${it.epc}")
                }
                .filter { !mAdapter.data.contains(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .flatEvent()
                .doOnNext {
                    mAdapter.addData(it)
                }
                .doOnError {
                    it.printStackTrace()
                    dialog.dismiss()
                    toast("扫描完成")
                }
                .onDisposable {
                    disposable = it
                }
                .subscribe()


        dialog.setOnDismissListener {
            info("停止->$disposable")
            disposable!!.dispose()
            mReader.stopInventoryMulti()
                    .flatSingleEvent()
                    .subscribe()
        }


    }


    private fun readEPC() {
        mLoading.show()
        mReader.inventoryRealTime()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatSingleEvent()
                .doOnNext {
                    this.mTagInfo = it
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
                    find<EditText>(R.id.uhf_tid).setText(it.data)
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
                    find<EditText>(R.id.uhf_user).setText(it.data)
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
        val epc = find<EditText>(R.id.uhf_epc).text.toString()
        if (TextUtils.isEmpty(epc)) {
            toast("请输入标签")
            return
        }
        if (TextUtils.isEmpty(mTagInfo.epc)) {
            toast("请先读取EPC标签")
            return
        }
        mLoading.setCancelable(false)
        mLoading.show()
        mWriter.writeEPC(getPwd(), epc, mTagInfo.epc)
                .observeOn(AndroidSchedulers.mainThread())
                .flatSingleEvent()
                .doOnNext {
                    toast("写入成功？")
                    mLoading.hide()
                }
                .doOnError {
                    mLoading.hide()
                    toast("写入失败")
                }
                .subscribe()

    }

    private fun writeToUser() {
        val userData = find<EditText>(R.id.uhf_user).text.toString()
        if (TextUtils.isEmpty(userData)) {
            toast("请输入数据")
            return
        }
        if (TextUtils.isEmpty(mTagInfo.epc)) {
            toast("请先读取EPC标签")
            return
        }
        mLoading.setCancelable(false)
        mLoading.show()
        mWriter.writeToUser(getPwd(), 0, 12, userData, mTagInfo.epc)
                .observeOn(AndroidSchedulers.mainThread())
                .flatSingleEvent()
                .doOnNext {
                    mLoading.hide()
                    toast("写入成功")
                }
                .doOnError {
                    error("写入失败->", it)
                    toast("写入失败")
                    mLoading.hide()
                }
                .subscribe()

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