package cn.gygxzc.uhf.uhf.model.impl

import cn.gygxzc.uhf.ble.BleInstance
import cn.gygxzc.uhf.event.ErrorCode
import cn.gygxzc.uhf.exception.RFIDException
import cn.gygxzc.uhf.uhf.cmd.Factory
import cn.gygxzc.uhf.uhf.enums.SENSITIVE
import cn.gygxzc.uhf.uhf.model.ISettingModel
import cn.gygxzc.uhf.uhf.reader.errorHandler
import cn.gygxzc.uhf.uhf.util.Tools
import cn.gygxzc.uhf.uhf.util.Tools.checkSum
import io.reactivex.Observable
import io.reactivex.ObservableSource
import kotlin.experimental.and

/**
 * @author niantuo
 * @createdTime 2018/5/14 13:46
 *
 *
 */
class SettingModel : ISettingModel {

    private val ble = BleInstance.BLE
    private val factory = Factory.CMD

    /**
     * 获取版本信息，
     * 只是版本号，这个实际上应该没有什么作用吧
     */
    override fun firmware(): Observable<String> {
        return ble.write(factory.firmware)
                .errorHandler()
                .map { Tools.bytes2HexString(it) }
    }

    /**
     * 设置灵敏度
     */
    override fun setSensitivity(value: Int): Observable<String> {
        return Observable.create<SENSITIVE> {
            val sensitive = SENSITIVE.values().firstOrNull { it.code == value }
            if (sensitive == null) {
                it.onError(RFIDException(ErrorCode.ERR_UN_SUPPORT_SENSITIVE_VALUE))
            } else {
                it.onNext(sensitive)
                it.onComplete()
            }
        }
                .flatMap<ByteArray> { _ ->
                    ObservableSource {
                        val len = factory.sensitive.size
                        val bytes = ByteArray(len)
                        System.arraycopy(factory.sensitive, 0, bytes, 0, len)
                        bytes[5] = value.toByte()
                        bytes[len - 2] = checkSum(bytes)
                        it.onNext(bytes)
                        it.onComplete()
                    }
                }
                .flatMap { ble.write(it) }
                .errorHandler()
                .map { Tools.bytes2HexString(it) }
    }

    override fun setBaudRate(): Observable<ByteArray> {
        return Observable.empty()
    }

    /**
     * 设置输出功率
     */
    override fun setOutputPower(value: Int): Observable<ByteArray> {
        return Observable.create<ByteArray> {
            val len = factory.outputPower.size
            val bytes = ByteArray(len)
            System.arraycopy(factory.outputPower, 0, bytes, 0, len)
            bytes[5] = (0xff00 and value shr 8).toByte()
            bytes[6] = (0xff and value).toByte()
            bytes[len - 2] = checkSum(bytes)
            it.onNext(bytes)
            it.onComplete()
        }
                .flatMap { ble.write(it) }
                .errorHandler()
    }

    override fun getOutputPower(): Observable<Int> {
        return ble.write(factory.getOutputPower)
                .errorHandler()
                .map { ((it[1] and 0xff.toByte()) * 256 + (it[2] and 0xff.toByte())) / 100 }
    }

    override fun setFrequency(startFrequency: Int, freqSpace: Int, freqQuality: Int): Observable<ByteArray> {
        return Observable.empty()
    }

}