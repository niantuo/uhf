package cn.gygxzc.uhf.uhf.model.impl

import cn.gygxzc.uhf.LogUtils
import cn.gygxzc.uhf.ble.BleInstance
import cn.gygxzc.uhf.kotlin.otherObservable
import cn.gygxzc.uhf.uhf.UHFException
import cn.gygxzc.uhf.uhf.cmd.Factory
import cn.gygxzc.uhf.uhf.enums.MemBank
import cn.gygxzc.uhf.uhf.enums.UHFExEnums
import cn.gygxzc.uhf.uhf.model.ISelectModel
import cn.gygxzc.uhf.uhf.model.IWriteModel
import cn.gygxzc.uhf.uhf.reader.UHFReader
import cn.gygxzc.uhf.uhf.util.Tools
import io.reactivex.*
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

    companion object {
        const val TAG = "WriteModel"
    }

    private val ble = BleInstance.BLE
    private val factory = Factory.CMD
    private val select = ISelectModel.create()

    /**
     * 写入数据到标签,
     * 这个地方有点玄学
     */
    override fun writeTo6C(password: ByteArray, memBank: Int, startAddr: Int, dataLen: Int,
                           originData: ByteArray, origin: String): Single<Boolean> {

        LogUtils.debug(TAG, "write to 6c  data: ${Tools.bytes2HexString(originData)}")
        return select.select(origin)
                .map { factory.createWriteCMD(password, memBank, startAddr, dataLen, originData) }
//                .otherObservable<ByteArray, Boolean> { bytes ->
//                    write(bytes)
//                }
                .flatMap { write(it) }
                .retry(2)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
    }


    private fun write(bytes: ByteArray): Single<Boolean> {

        return ble.writeDirect(bytes)
                .flatMap<Boolean> { resp ->
                    SingleSource<Boolean> {
                        val response = UHFReader.checkResponse(resp)
                        if (response == null) {
                            it.onError(UHFException(UHFExEnums.ERR_RESPONSE_DATA))
                        } else if (response[0] == 0x49.toByte()) {
                            val param = response[response.size - 1]
                            if (param == factory.RESPONSE_OK) {
                                it.onSuccess(true)
                            } else {
                                LogUtils.debug(TAG, "write err ->${Tools.bytesToInt(byteArrayOf(param))}")
                                it.onError(UHFException(UHFExEnums.ERR_WRITE_FAILED))
                            }
                        } else {
                            LogUtils.debug(TAG, "response ->${Tools.bytes2HexString(response)}")
                            it.onError(UHFException(UHFExEnums.ERR_WRITE_FAILED))
                        }
                    }
                }
                .retry(4)


//        return ble.write(bytes)
//                .flatMap<Boolean> { res ->
//                    ObservableSource {
//                        val response = UHFReader.checkResponse(res)
//                                ?: return@ObservableSource
//                        LogUtils.debug(TAG, "response ->${Tools.bytes2HexString(response)}")
//                        if (response[0] == 0x49.toByte()) {
//                            val param = response[response.size - 1]
//                            if (param == factory.RESPONSE_OK) {
//                                it.onNext(true)
//                                it.onComplete()
//                            } else {
//                                LogUtils.debug(TAG, "write err ->${Tools.bytesToInt(byteArrayOf(param))}")
//                            }
//                        }
//                    }
//                }
//                .timeout(1, TimeUnit.SECONDS)
//                .retry(4)
    }

    /**
     * 写epc，，这个EPC占 6个字节
     * 24 bit
     */
    override fun writeEPC(password: String, epc: String, origin: String): Single<Boolean> {
        return writeTo6C(Tools.HexString2Bytes(password), MemBank.EPC_BANK.code, 2, 12,
                Tools.HexString2Bytes(epc), origin)
    }

    override fun writeToUser(password: String, startAddr: Int, dataLen: Int,
                             data: String, origin: String): Single<Boolean> {
        return writeTo6C(Tools.HexString2Bytes(password),
                MemBank.USER_BANK.code, startAddr, dataLen, Tools.HexString2Bytes(data), origin)
    }

}