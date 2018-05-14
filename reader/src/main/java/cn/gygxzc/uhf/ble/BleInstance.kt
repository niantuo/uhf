package cn.gygxzc.uhf.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import cn.gygxzc.uhf.event.ErrorCode
import cn.gygxzc.uhf.exception.RFIDException
import cn.gygxzc.uhf.uhf.reader.UHFReader
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author niantuo
 * @createdTime 2018/5/12 18:04
 *
 *  这个需要作为一个全局变量来维护，所以一个APP只能连接一个
 *  基于这个的操作，这个是获取蓝牙的输入输出流
 *  并维护一个全局的，实际上是方便系统的调用，以及在Activity之间的传递，减少难度
 *  通过它自己的一个生命周期的控制，可以避免代码泄漏这个问题
 */
class BleInstance {
    companion object {
        private const val MY_UUID = "00001101-0000-1000-8000-00805F9B34FB" // SPP服务UUID号
        private const val TAG = "BleInstance"
        val BLE = BleInstance()
    }


    var connectedDevice: BluetoothDevice? = null
    var bluetoothSocket: BluetoothSocket? = null

    fun isConnected(): Boolean {
        return connectedDevice != null && bluetoothSocket != null
    }

    fun connect(device: BluetoothDevice): Observable<BluetoothSocket> {
        this.connectedDevice = device
        return Observable.create<BluetoothSocket> {
            ensureDevice()
            val socket = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID))
            try {
                socket.connect()
                bluetoothSocket = socket
                it.onNext(socket)
                it.onComplete()
            } catch (e: IOException) {
                it.onError(e)
            }
        }
                .timeout(10, TimeUnit.SECONDS)
                .retry(2)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
    }


    /**
     * 保证写数据的单线程
     * 这个地方还要考虑一下，实际上应该是输入输出流保证同时只有一个线程访问
     * 如果是自定义的错误，即表示是代码主动抛出的错误
     * 不需要重试，这个是输入指令，返回结果
     * 一个指令对应的是一个结果，蓝牙的输入输出流是同步的
     * 也就是说，不可能同时对一个蓝牙socket进行多个写的操作，总是写-读，，写-读
     * 实际上这儿的写法是特地针对RFID设备的，因为RFID设备并不会主动给APP端发送消息
     * 流程是接收命令，返回结果，所以可以这样操作
     * 如果使用蓝牙通信，这种操作应该是不可取的，因为双方都可能是消息的发起者
     */
    fun write(vararg bytes: ByteArray): Observable<ByteArray> {
        return Observable.fromIterable(bytes.toList())
                .map {
                    ensureSocket()
                    synchronized(bluetoothSocket!!) {
                        bluetoothSocket!!.outputStream.write(it)
                        bluetoothSocket!!.outputStream.flush()
                    }
                    it
                }
                .toList()
                .retry(2, { it !is RFIDException })
                .flatMapObservable<ByteArray> { UHFReader.read(bluetoothSocket!!.inputStream) }
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
    }

    /**
     * 把读写当成一个整体
     */
    fun writeSingle(bytes: ByteArray): Single<ByteArray> {
        return Single.create<ByteArray> {
            ensureSocket()
            synchronized(bluetoothSocket!!) {
                bluetoothSocket!!.outputStream.write(bytes)
                bluetoothSocket!!.outputStream.flush()
            }
            it.onSuccess(bytes)
        }
                .flatMap { UHFReader.readSingle(bluetoothSocket!!.inputStream) }
    }

    /**
     * 关闭连接,把这参数置空，防止发生内存泄漏
     */
    fun disconnect() {
        bluetoothSocket?.close()
        bluetoothSocket = null
        connectedDevice = null
    }


    /**
     * 退出的时候一定更要调用该方法，防止出现内存泄漏
     */
    fun destroy() {
        disconnect()
    }


    @Throws(Exception::class)
    private fun ensureDevice() {
        if (connectedDevice == null)
            throw RFIDException(ErrorCode.ERROR_BLE_NO_DEVICE)
    }

    @Throws(RFIDException::class)
    private fun ensureSocket() {
        if (bluetoothSocket == null)
            throw RFIDException(ErrorCode.ERROR_BLE_NO_CONNECTED)
    }


}