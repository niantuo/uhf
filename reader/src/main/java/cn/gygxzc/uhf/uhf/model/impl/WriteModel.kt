package cn.gygxzc.uhf.uhf.model.impl

import cn.gygxzc.uhf.ble.BleInstance
import cn.gygxzc.uhf.kotlin.otherSingle
import cn.gygxzc.uhf.uhf.cmd.Factory
import cn.gygxzc.uhf.uhf.enums.MemBank
import cn.gygxzc.uhf.uhf.model.ISelectModel
import cn.gygxzc.uhf.uhf.model.IWriteModel
import cn.gygxzc.uhf.uhf.util.Tools
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * @author niantuo
 * @createdTime 2018/5/14 9:56
 *
 *  写入标签数据，暂时只读吧，写的话到时候再说
 *  便携式的写数据，出错概率很大，而且这个玖锐的设备有问题，
 *  所以这个功能暂时是不能用的，当然，是可以设置参数的
 *  所以简直了
 */
class WriteModel : IWriteModel {

    private val ble = BleInstance.BLE
    private val factory = Factory.CMD
    private val select = ISelectModel.create()

    /**
     * 写入数据到标签
     */
    override fun writeTo6C(password: ByteArray, memBank: Int, startAddr: Int, dataLen: Int,
                           originData: ByteArray, origin: String): Single<ByteArray> {

        return select.select(origin)
                .map { factory.createWriteCMD(password, memBank, startAddr, dataLen, originData) }
                .otherSingle<ByteArray, ByteArray> { bytes, singleObserver ->
                    SingleSource<ByteArray> {
                        ble.writeSingle(bytes)
                                .flatMap { bytes ->
                                    SingleSource<ByteArray> {
                                        if (bytes[0].toInt() == 0x49 && bytes[bytes.size - 1] == factory.RESPONSE_OK) {
                                            it.onSuccess(bytes)
                                        }
                                    }
                                }
                                .timeout(1, TimeUnit.SECONDS)
                                .retry()
                                .subscribe(singleObserver)
                    }
                }
                .timeout(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())


    }

    /**
     * 写epc，，这个EPC占 6个字节
     * 24 bit
     */
    override fun writeEPC(password: String, epc: String, origin: String): Single<ByteArray> {
        return writeTo6C(Tools.HexString2Bytes(password), MemBank.EPC_BANK.code, 2, 6,
                Tools.HexString2Bytes(epc), origin)
    }

    override fun writeToUser(password: String, data: String, origin: String): Single<ByteArray> {
        return writeTo6C(Tools.HexString2Bytes(password),
                MemBank.USER_BANK.code, 0, 12, Tools.HexString2Bytes(data), origin)
    }

}