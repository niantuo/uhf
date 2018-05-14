package cn.gygxzc.uhf.uhfreader

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.gygxzc.uhf.ble.BleInstance
import org.jetbrains.anko.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout {

            textView {
                text="标签读写测试"
                setOnClickListener { toActivity<UHFDemoActivity>() }
                padding = dip(15)
                backgroundResource = R.color.colorAccent
                textSize = 15f
            }.lparams(matchParent, wrapContent)

            textView {
                text="读写器设置"
                setOnClickListener { toActivity<SettingActivity>() }
                padding = dip(15)
                textSize = 15f
                backgroundResource = R.color.colorAccent
            }.lparams(matchParent, wrapContent){
                topMargin = dip(15)
            }
        }
    }


    /**
     * 所以这个还是挺好的
     */
    private inline fun <reified T : Activity> toActivity() {
        if (!BleInstance.BLE.isConnected())
            startActivity<BleListActivity>()
        else startActivity<T>()
    }


    /**
     * 退出应用的时候关闭蓝牙
     */
    override fun onDestroy() {
        super.onDestroy()
        BleInstance.BLE.destroy()
    }
}
