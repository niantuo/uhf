package cn.gygxzc.uhf.receiver

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import cn.gygxzc.uhf.RxBleBus
import cn.gygxzc.uhf.event.MyEvent

/**
 * @author niantuo
 * @createdTime 2018/6/14 18:30
 *
 *
 */
class BlueStateReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BlueStateReceiver"
        @Volatile
        var isConnected = false
        var connectedList = mutableListOf<BluetoothDevice>()

        fun isConnected(device: BluetoothDevice): Boolean {
            return isConnected && connectedList.contains(device)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
        Log.i(TAG, "action->$action  device->${device.address}")
        when (action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                isConnected = true
                if (device != null) connectedList.add(device)
                RxBleBus.post(MyEvent.CONNECTED)
            }
            BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED -> {
                isConnected = false
                if (device != null) connectedList.remove(device)
                RxBleBus.post(MyEvent.CLOSED)
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                isConnected = false
                if (device != null) connectedList.remove(device)
                RxBleBus.post(MyEvent.CLOSED)
            }
        }
    }
}