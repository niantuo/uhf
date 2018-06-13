package cn.gygxzc.uhf.ble.model

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import android.util.Log
import cn.gygxzc.uhf.ble.IBleModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * Created by niantuo on 2017/10/18.
 * 蓝牙扫描
 * 调用扫描方法[startScan] 之后一定要调用取消扫描的方法[cancelScan]
 * 很简单的一个扫描功能就可以了
 */
@SuppressLint("MissingPermission")
class BlueScan(private val mContext: Context) : IBleModel {

    companion object {
        const val TAG = "BlueScan"
    }


    private val mAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val mPublish = PublishSubject.create<BluetoothDevice>()
    private val receiver = BlueScanReceiver()


    /**
     * 确保是否有 蓝牙可用
     * 模拟器无蓝牙适配器，会报错
     */
    override fun ble(): Boolean {
        return mAdapter != null
    }

    /**
     * 获取已绑定的设备，这个功能很重要，如果我们的APP扫描不到蓝牙设备
     * 那么可以跳转到系统的蓝牙设置界面，进行绑定，之后通过该方法，获取设备
     * 这样不用考虑蓝牙扫描的多样性，因为系统的实现方式应该是最好的
     */
    override fun getBondedDevices(): Set<BluetoothDevice> {
        return mAdapter?.bondedDevices?.toMutableSet() ?: emptySet()
    }


    /**
     * 跳转到系统的打开蓝牙或者切换界面
     * 用户能决定是否打开，一般来说可以监听蓝牙的开关来判断蓝牙是否可以
     * 但是实际上，你可以直接通过再次判断来实现了，
     * 每次扫描蓝牙之前，确保蓝牙已打开，如果没打开，提示用户，跳转到界面操作，
     *
     */
    override fun bleEnable(reqCode: Int) {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (mContext is Activity) {
            mContext.startActivityForResult(intent, reqCode)
        } else {
            mContext.startActivity(intent)
        }
    }

    /**
     * 跳转到蓝牙扫描界面
     */
    override fun jumpToBleSetting(reqCode: Int) {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (mContext is Activity)
            mContext.startActivityForResult(intent, reqCode)
        else mContext.startActivity(intent)
    }

    /**
     * 返回蓝牙是否可以使用
     */
    override fun isBleEnable(): Boolean {
        return mAdapter?.isEnabled ?: false
    }

    override fun getRemoteDevice(address: String): BluetoothDevice? {
        return mAdapter?.getRemoteDevice(address)
    }


    /**
     * 开始蓝牙扫描
     * 这个地方有几个操作
     * 1、定时，如果两分钟任然没有手动取消或者扫描完成取消,那么自动取消
     * 2、自动取消上一次的广播订阅操作
     */
    @SuppressLint("MissingPermission")
    override fun startScan(): Observable<BluetoothDevice> {
        return if (mAdapter != null) {
            if (receiver.registered) {
                cancelScan()
            } else {
                val filter = IntentFilter()
                filter.addAction(BluetoothDevice.ACTION_FOUND)
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                mContext.registerReceiver(receiver, filter)
                receiver.registered = true
            }
            mAdapter.startDiscovery()  //开始搜索
            mPublish.ofType(BluetoothDevice::class.java)
                    .timeout(2, TimeUnit.MINUTES)
                    .doOnError { cancelScan() }
                    .doOnComplete { cancelScan() }
                    .doOnDispose { cancelScan() }
        } else {
            Observable.empty()
        }
    }

    @SuppressLint("MissingPermission")
    override fun cancelScan() {
        if (receiver.registered) {
            mContext.unregisterReceiver(receiver)
            mAdapter?.cancelDiscovery()
            receiver.registered = false
        }
    }


    /**
     * 接收蓝牙信息的广播
     */
    inner class BlueScanReceiver : BroadcastReceiver() {
        var registered: Boolean = false
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "intent: $intent")
            val action = intent.action
            if (action == BluetoothDevice.ACTION_FOUND) {
                // 获取搜索到的蓝牙设备
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // 回调到前台界面
                mPublish.onNext(device)
            } else if (action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED) {
                // 搜索完成
                mPublish.onComplete()
            }
        }
    }

}