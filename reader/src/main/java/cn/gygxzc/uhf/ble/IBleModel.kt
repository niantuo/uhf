package cn.gygxzc.uhf.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import cn.gygxzc.uhf.ble.model.BlueScan
import io.reactivex.Observable

/**
 * @author niantuo
 * @createdTime 2018/5/12 17:49
 *
 *
 */
interface IBleModel {

    /**
     * 判断蓝牙是否可用
     */
    fun isBleEnable(): Boolean

    /**
     * 直接打开蓝牙
     */
    fun bleEnable(reqCode: Int)

    /**
     * 获取到手机已绑定的蓝牙
     */
    fun getBondedDevices(): Set<BluetoothDevice>

    fun startScan(): Observable<BluetoothDevice>

    /**
     * 跳转到蓝牙设置或者扫描界面
     */
    fun jumpToBleSetting(reqCode:Int)

    /**
     * 取消扫描，一般情况下，需要取消扫描
     * 但是同时如果扫描结束，也会自动取消
     */
    fun cancelScan()

    fun getRemoteDevice(address:String):BluetoothDevice?

    companion object {
        fun create(context: Context): IBleModel = BlueScan(context)
    }
}