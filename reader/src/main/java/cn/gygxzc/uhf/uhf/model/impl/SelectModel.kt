package cn.gygxzc.uhf.uhf.model.impl

import cn.gygxzc.uhf.ble.BleInstance
import cn.gygxzc.uhf.uhf.cmd.CMDFactory
import cn.gygxzc.uhf.uhf.model.ISelectModel
import cn.gygxzc.uhf.uhf.reader.errorHandler
import cn.gygxzc.uhf.uhf.util.RespUtils
import cn.gygxzc.uhf.uhf.util.Tools
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit

/**
 * @author niantuo
 * @createdTime 2018/5/14 13:13
 *  都是需要异步操作的，千万不要再主线程里执行
 *
 */
class SelectModel : ISelectModel {

    private val ble = BleInstance.BLE
    private val factory = CMDFactory.create()

    /**
     * 选择标签，有返回值就表示成功
     * 所以只需要返回值一次，这个,这个不能这么做，
     */
    override fun select(epc: String): Single<ByteArray> {
        return ble.writeSingle(selectCMD(epc))
                .timeout(200, TimeUnit.MILLISECONDS)
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

    override fun unSelect(): Single<ByteArray> {
        return ble.writeSingle(factory.unselected)
                .timeout(200,TimeUnit.MILLISECONDS)
    }
}