package cn.gygxzc.uhf.receiver

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * @author niantuo
 * @createdTime 2018/6/14 18:30
 *
 *
 */
class BlueStateReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG="BlueStateReceiver"
        var isConnected = false
        var connectedList = mutableListOf<BluetoothDevice>()

        fun isConnected(device: BluetoothDevice): Boolean {
            return isConnected && connectedList.contains(device)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
        if (BluetoothDevice.ACTION_ACL_CONNECTED == action) {
            isConnected = true
            if (device != null) connectedList.add(device)
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED == action) {
            isConnected = false
            if (device != null) connectedList.remove(device)
        }
    }
}