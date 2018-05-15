package cn.gygxzc.uhf.uhf.model.impl

import cn.gygxzc.uhf.LogUtils
import cn.gygxzc.uhf.ble.BleInstance
import cn.gygxzc.uhf.uhf.cmd.CMDFactory
import cn.gygxzc.uhf.uhf.model.ISelectModel
import cn.gygxzc.uhf.uhf.reader.UHFReader
import cn.gygxzc.uhf.uhf.util.RespUtils
import cn.gygxzc.uhf.uhf.util.Tools
import io.reactivex.Single
import io.reactivex.SingleSource
import java.util.concurrent.TimeUnit

/**
 * @author niantuo
 * @createdTime 2018/5/14 13:13
 *  都是需要异步操作的，千万不要再主线程里执行
 *
 */
class SelectModel : ISelectModel {
    companion object {
        const val TAG = "SelectModel"
    }

    private val ble = BleInstance.BLE
    private val factory = CMDFactory.create()


    /**
     * 选择标签，有返回值就表示成功
     * 所以只需要返回值一次，这个,这个不能这么做，
     */
    override fun select(epc: String): Single<Boolean> {
        return ble.writeSingle(selectCMD(epc))
                .flatMap<Boolean> { bytes ->
                    SingleSource {
                        val response = UHFReader.checkResponse(bytes) ?: return@SingleSource
                        LogUtils.debug(TAG,"select ${Tools.bytes2HexString(response)}")
                        if (response[0] != 0x0C.toByte()) return@SingleSource
                        if (response[1] == 0x00.toByte()) {
                            LogUtils.debug(TAG, "select $epc  success")
                            it.onSuccess(true)
                        }
                    }
                }
                .timeout(400, TimeUnit.MILLISECONDS)
    }

    override fun selectCMD(epc: String): ByteArray {
        val len = factory.select.size
        val select = ByteArray(len)
        System.arraycopy(factory.select, 0, select, 0, len)
        val epcBytes = Tools.HexString2Bytes(epc)
        System.arraycopy(epcBytes, 0, select, 12, epcBytes.size)
        select[len - 2] = RespUtils.checkSum(select)
        return select
    }

    /**
     * 取消选择标签
     */
    override fun unSelect(): Single<Boolean> {
        return ble.writeSingle(factory.unselected)
                .flatMap { bytes ->
                    SingleSource<Boolean> {
                        val response = UHFReader.checkResponse(bytes) ?: return@SingleSource
                        LogUtils.debug(TAG,"un select ${Tools.bytes2HexString(response)}")
                        if (response[0] != 0x12.toByte()) return@SingleSource
                        if (response[1] == 0x00.toByte()) {
                            LogUtils.debug(TAG, "cancel select success")
                            it.onSuccess(true)
                        }
                    }
                }
                .timeout(400, TimeUnit.MILLISECONDS)
    }
}