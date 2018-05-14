package cn.gygxzc.uhf.uhf.model.impl

import android.util.Log
import cn.gygxzc.uhf.ble.BleInstance
import cn.gygxzc.uhf.kotlin.otherSingle
import cn.gygxzc.uhf.uhf.cmd.Factory
import cn.gygxzc.uhf.uhf.entity.TagInfo
import cn.gygxzc.uhf.uhf.enums.MemBank
import cn.gygxzc.uhf.uhf.model.IReadModel
import cn.gygxzc.uhf.uhf.model.ISelectModel
import cn.gygxzc.uhf.uhf.reader.UHFReader
import cn.gygxzc.uhf.uhf.reader.toEPCFlatMap
import cn.gygxzc.uhf.uhf.reader.toTagInfo
import cn.gygxzc.uhf.uhf.util.Tools
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * @author niantuo
 * @createdTime 2018/5/14 9:55
 *
 *  从标签中读取数据
 *
 */
class ReadModel : IReadModel {

    companion object {
        const val TAG = "ReadModel"
    }

    private val ble = BleInstance.BLE
    private val factory = Factory.CMD
    private val select = ISelectModel.create()

    /**
     * 实际上这个只应该返回一张标签，
     * 但是看具体需求吧
     */
    override fun inventoryRealTime(): Single<TagInfo> {
        return ble.writeSingle(factory.unselected)
                .flatMap {
                    ble.writeSingle(factory.inventoryRealTime)
                            .timeout(500, TimeUnit.MILLISECONDS)
                            .retry()
                }
                .toTagInfo()
                .timeout(1, TimeUnit.SECONDS)
                .retry()
                .timeout(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
    }

    override fun inventoryMulti(): Observable<TagInfo> {
        return ble.write(factory.unselected, factory.inventoryMulti)
                .toEPCFlatMap()
    }

    /**
     * 最后应该进行统一的处理，这个最后再说
     */
    override fun stopInventoryMulti(): Observable<ByteArray> {
        return ble.write(factory.stopInventoryMylti)
    }


    /**
     * 实际上我们应该读取的是TID用户区
     * 不能把命令分开写，这个是什么意思？\
     * 如果2s内都没有结果，那么放弃本次查询吧
     */
    override fun readFrom6C(epc: String,
                            memBank: Int, startAddr: Int, length: Int,
                            accessPassword: ByteArray): Single<String> {
        return select.select(epc)
                .map { factory.createReadCMD(memBank, startAddr, length, accessPassword) }
                .otherSingle<ByteArray, ByteArray> { bytes, singleObserver ->
                    ble.writeSingle(bytes)
                            .timeout(600, TimeUnit.MILLISECONDS)
                            .retry()
                            .subscribe(singleObserver)
                }.flatMap<String> { response ->
                    SingleSource<String> {
                        Log.i(TAG, "返回TID结果 ${Tools.bytes2HexString(response)}")
                        val resolve = UHFReader.checkResponse(response)
                                ?: return@SingleSource
                        if (resolve[0] == 0x39.toByte()) {
                            val lenData = resolve.size - resolve[1].toInt() - 2
                            val data = ByteArray(lenData)
                            System.arraycopy(resolve, resolve[1] + 2, data, 0, lenData)
                            it.onSuccess(Tools.bytes2HexString(data))
                        }
                    }
                }
                .timeout(1, TimeUnit.SECONDS)
                .retry()
                .timeout(3, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())

    }

    /**
     * 用户的TID区长度也是12位16进制数
     * 48bit
     */
    override fun readTID(epc: String, password: String): Single<String> {
        return readFrom6C(epc, MemBank.TID_BANK.code, 0, 12, Tools.HexString2Bytes(password))

    }

    override fun readUser(epc: String, password: String): Single<String> {
        return readFrom6C(epc, MemBank.USER_BANK.code, 0, 12, Tools.HexString2Bytes(password))
    }
}