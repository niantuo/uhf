package cn.gygxzc.uhf.uhf.model.impl

import cn.gygxzc.uhf.LogUtils
import cn.gygxzc.uhf.ble.BleInstance
import cn.gygxzc.uhf.exception.RFIDException
import cn.gygxzc.uhf.kotlin.otherObservable
import cn.gygxzc.uhf.uhf.cmd.Factory
import cn.gygxzc.uhf.uhf.enums.SENSITIVE
import cn.gygxzc.uhf.uhf.enums.UHFExEnums
import cn.gygxzc.uhf.uhf.model.ISettingModel
import cn.gygxzc.uhf.uhf.reader.UHFReader
import cn.gygxzc.uhf.uhf.util.Tools
import cn.gygxzc.uhf.uhf.util.Tools.checkSum
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * @author niantuo
 * @createdTime 2018/5/14 13:46
 *
 *
 */
class SettingModel : ISettingModel {

    companion object {
        private const val TAG = "SettingModel"
    }

    private val ble = BleInstance.BLE
    private val factory = Factory.CMD

    /**
     * 获取版本信息，
     * 只是版本号，这个实际上应该没有什么作用吧
     */
    override fun firmware(): Single<String> {
        return Single.create<Int> {
            it.onSuccess(1)
        }.otherObservable<Int, String> {
            ble.write(factory.firmware)
                    .flatMap<String> { bytes ->
                        ObservableSource {
                            val response = UHFReader.checkResponse(bytes) ?: return@ObservableSource
                            if (response[0] == 0x03.toByte()) {
                                val data = ByteArray(response.size - 4)
                                System.arraycopy(response, 4, data, 0, data.size)
                                it.onNext(Tools.bytes2HexString(data))
                                it.onComplete()
                            }
                        }
                    }
                    .timeout(1, TimeUnit.SECONDS)
                    .retry(2)
        }
                .subscribeOn(Schedulers.computation())
                .unsubscribeOn(Schedulers.computation())
    }

    /**
     * 设置灵敏度
     */
    override fun setSensitivity(value: Int): Single<Boolean> {
        return Single.create<SENSITIVE> {
            val sensitive = SENSITIVE.values().firstOrNull { it.code == value }
            if (sensitive == null) {
                it.onError(RFIDException(UHFExEnums.ERR_UN_SUPPORT_SENSITIVE_VALUE))
            } else {
                it.onSuccess(sensitive)
            }
        }
                .map<ByteArray> {
                    val len = factory.sensitive.size
                    val bytes = ByteArray(len)
                    System.arraycopy(factory.sensitive, 0, bytes, 0, len)
                    bytes[5] = value.toByte()
                    bytes[len - 2] = checkSum(bytes)
                    bytes
                }
                .otherObservable<ByteArray, Boolean> { bytes ->
                    ble.write(bytes)
                            .flatMap<Boolean> { resp ->
                                ObservableSource {
                                    val response = UHFReader.checkResponse(resp)
                                            ?: return@ObservableSource
                                    LogUtils.info(TAG, "set sensitive-> ${Tools.bytes2HexString(response)}")
                                    if (response[0] == 0xF0.toByte()) {
                                        val errCode = response[response.size - 1]
                                        if (errCode == 0x00.toByte()) {
                                            it.onNext(true)
                                            it.onComplete()
                                        } else {
                                            LogUtils.info(TAG, "error code -> ${Tools.bytesToInt(byteArrayOf(errCode))}")
                                        }
                                    } else {
                                        LogUtils.info(TAG, "command ->${Tools.bytes2HexString(response)}")
                                    }
                                }

                            }
                            .timeout(1, TimeUnit.SECONDS)
                            .retry(2)
                }
                .subscribeOn(Schedulers.computation())
                .unsubscribeOn(Schedulers.computation())
    }

    /**
     * 只需要展示一个设备的灵敏读即可
     * 其他的
     */
    override fun getSensitivity(): Single<SENSITIVE> {
        return Single.create<Int> {
            it.onSuccess(1)
        }.otherObservable { _ ->
            ble.write(factory.querySensitive)
                    .flatMap<SENSITIVE> { resp ->
                        ObservableSource {
                            val response = UHFReader.checkResponse(resp) ?: return@ObservableSource
                            LogUtils.info(TAG, "sensitive ->${Tools.bytes2HexString(response)}")
                            if (response[0] == 0xF1.toByte()) {
                                val sensitive = response[1]
                                val next = SENSITIVE.values().firstOrNull { it.code == sensitive.toInt() }
                                LogUtils.info(TAG, "sensitive:$next")
                                if (next != null) {
                                    it.onNext(next)
                                    it.onComplete()
                                } else {
                                    LogUtils.info(TAG, "sensitive->${sensitive.toInt()}")
                                }
                            } else {
                                LogUtils.info(TAG, Tools.bytes2HexString(response))
                            }
                        }
                    }
                    .timeout(1, TimeUnit.SECONDS)
                    .retry(2)
        }
                .subscribeOn(Schedulers.computation())
                .unsubscribeOn(Schedulers.computation())
    }

    override fun setBaudRate(): Observable<ByteArray> {
        return Observable.empty()
    }

    /**
     * 设置输出功率
     */
    override fun setOutputPower(value: Int): Single<Boolean> {
        return Single.create<ByteArray> {
            val len = factory.outputPower.size
            val bytes = ByteArray(len)
            System.arraycopy(factory.outputPower, 0, bytes, 0, len)
            bytes[5] = (0xff00 and value shr 8).toByte()
            bytes[6] = (0xff and value).toByte()
            bytes[len - 2] = checkSum(bytes)
            it.onSuccess(bytes)
        }.otherObservable { bytes ->
            realWriteOutPower(bytes)
        }
                .subscribeOn(Schedulers.computation())
                .unsubscribeOn(Schedulers.computation())
    }

    private fun realWriteOutPower(cmd: ByteArray): Observable<Boolean> {
        return ble.write(cmd)
                .flatMap { bytes ->
                    ObservableSource<Boolean> {
                        val response = UHFReader.checkResponse(bytes)
                                ?: return@ObservableSource
                        if (response[0] == 0xB6.toByte()) {
                            it.onNext(true)
                            it.onComplete()
                        } else {
                            LogUtils.info(TAG, "set out power->${Tools.bytes2HexString(response)}")
                        }
                    }
                }
                .timeout(1, TimeUnit.SECONDS)
    }

    /**
     * 获取输出功率
     */
    override fun getOutputPower(): Single<Int> {
        return Single.create<ByteArray> {
            it.onSuccess(factory.getOutputPower)
        }.otherObservable { bytes ->
            ble.write(bytes)
                    .flatMap { resp ->
                        ObservableSource<Int> {
                            val response = UHFReader.checkResponse(resp) ?: return@ObservableSource
                            LogUtils.info(TAG, "output power->${Tools.bytes2HexString(response)}")
                            if (response[0] == 0xB7.toByte()) {
                                LogUtils.info(TAG, "get output power->${response.size}")
                                try {
                                    val power = byteArrayOf(response[1], response[2])
                                    it.onNext(Tools.bytes2HexString(power).toInt(16))
                                    it.onComplete()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                } finally {
                                    LogUtils.info(TAG, "费解->${Tools.bytes2HexString(response)}")
                                }
                            } else {
                                LogUtils.info(TAG, "getOutputPower->${Tools.bytes2HexString(response)}")
                            }
                        }
                    }
                    .doOnError {
                        it.printStackTrace()
                    }
                    .timeout(1, TimeUnit.SECONDS)
                    .retry(2)
        }
                .unsubscribeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
    }

    override fun setFrequency(startFrequency: Int, freqSpace: Int, freqQuality: Int): Single<ByteArray> {
        return Single.error(RFIDException(UHFExEnums.ERR_NONE))
    }

}