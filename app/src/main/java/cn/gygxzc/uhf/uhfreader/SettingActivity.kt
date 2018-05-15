package cn.gygxzc.uhf.uhfreader

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import cn.gygxzc.uhf.kotlin.flatEvent
import cn.gygxzc.uhf.kotlin.flatSingleEvent
import cn.gygxzc.uhf.uhf.enums.SENSITIVE
import cn.gygxzc.uhf.uhf.model.ISettingModel
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.*

/**
 * @author niantuo
 * @createdTime 2018/5/14 15:09
 *
 *
 */
class SettingActivity : AppCompatActivity() ,AnkoLogger{

    private lateinit var mFirmwareView: TextView
    private lateinit var mOutPowerView: TextView
    private lateinit var mSensitiveView: TextView
    private val mSetting = ISettingModel.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verticalLayout {
            padding = dip(10)
            linearLayout {
                orientation = LinearLayout.HORIZONTAL
                button {
                    text = "获取版本信息"
                    padding = dip(10)
                    setOnClickListener { getFirmware() }
                }.lparams(wrapContent, wrapContent)
            }.lparams(matchParent, wrapContent)
            textView {
                id = R.id.version_name
                mFirmwareView = this
                textSize = 15f
                padding = dip(10)
            }.lparams(matchParent, wrapContent)
            linearLayout {
                orientation = LinearLayout.HORIZONTAL
                button {
                    text = "获取输出功率"
                    padding = dip(10)
                    setOnClickListener { getOutPower() }
                }.lparams(wrapContent, wrapContent)
                button {
                    text = "设置输出功率"
                    padding = dip(10)
                    setOnClickListener { setOutPower() }
                }.lparams(wrapContent, wrapContent)
            }.lparams(matchParent, wrapContent)
            textView {
                id = R.id.out_power
                mOutPowerView = this
                textSize = 15f
                padding = dip(10)
            }.lparams(matchParent, wrapContent)
            linearLayout {
                orientation = LinearLayout.HORIZONTAL
                button {
                    text = "设备灵敏度"
                    padding = dip(10)
                    setOnClickListener { getSensitive() }
                }.lparams(wrapContent, wrapContent)
                button {
                    text = "设置灵敏度"
                    padding = dip(10)
                    setOnClickListener { setSensitive() }
                }.lparams(wrapContent, wrapContent)
            }.lparams(matchParent, wrapContent)
            textView {
                id = R.id.sensitive_btn
                mSensitiveView = this
                textSize = 15f
                padding = dip(10)
            }.lparams(matchParent, wrapContent)
        }
    }

    private fun getFirmware() {
        mSetting.firmware()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatSingleEvent()
                .doOnNext {
                    mFirmwareView.text = it
                }
                .doOnError {
                    it.printStackTrace()
                }
                .subscribe()

    }

    private fun getOutPower() {
        mSetting.getOutputPower()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatSingleEvent()
                .doOnNext {
                    mOutPowerView.text = it.toString()
                    toast("获取成功")
                }
                .doOnError {
                    it.printStackTrace()
                }
                .subscribe()
    }

    private fun setOutPower() {


    }

    /**
     * 灵敏度设置
     */
    private fun getSensitive() {
        mSetting.getSensitivity()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatSingleEvent()
                .doOnNext {
                    mSensitiveView.text = it.desc
                    toast("获取灵敏度成功")
                }
                .doOnError {
                    it.printStackTrace()
                }
                .subscribe()

    }

    private fun setSensitive() {

        val items = SENSITIVE.values()
        MaterialDialog.Builder(this)
                .title("设置灵敏度")
                .items(items.map { it.desc }.toMutableList())
                .itemsCallbackSingleChoice(0) { _, _, which, _ ->
                    val value = items[which].code
                    realSetSensitive(value)
                    return@itemsCallbackSingleChoice true
                }
                .negativeText("取消")
                .show()


    }

    private fun realSetSensitive(value:Int){
        mSetting.setSensitivity(value)
                .observeOn(AndroidSchedulers.mainThread())
                .flatSingleEvent()
                .doOnNext {
                    toast("设置成功")
                }
                .doOnError {
                    it.printStackTrace()
                    toast("设置失败")
                }
                .subscribe()

    }
}