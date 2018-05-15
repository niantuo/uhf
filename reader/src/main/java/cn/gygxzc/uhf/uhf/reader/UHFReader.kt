package cn.gygxzc.uhf.uhf.reader

import android.util.Log
import cn.gygxzc.uhf.LogUtils
import cn.gygxzc.uhf.uhf.entity.TagInfo
import cn.gygxzc.uhf.uhf.util.RespUtils
import cn.gygxzc.uhf.uhf.util.Tools
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.SingleEmitter
import java.io.InputStream
import java.util.*
import kotlin.experimental.and

object UHFReader {

    private const val TAG = "UHFReader"
    private const val MAX_RES_LEN = 512
    private const val MIN_RES_BYTE = 7
    private const val BYTE_ZERO: Byte = 0x00
    private const val HEAD: Byte = 0xAA.toByte()
    private const val END: Byte = 0x8E.toByte()


    /**
     * 有命令，则有返回，或者说是发送命令之后，在500ms内如果没有返回，
     * 则表示该次请求失败,只会发送一次数据
     * 有些实际上，只需要一次数据返回即可，所以这个地方，需要做一些工作
     * 实际上这个也是有超时计算的，
     * 所以实际上应该是这个
     */
    fun read(inputStream: InputStream): Observable<ByteArray> {
        return Observable.create<ByteArray> {
            val start = System.currentTimeMillis()
            LogUtils.debug(TAG, "start receive response->${System.currentTimeMillis()}")
            synchronized(inputStream) {
                innerRead(inputStream, it, null)
            }
            LogUtils.debug(TAG, "deal response end->${System.currentTimeMillis() - start}")
            it.onComplete()
        }
    }


    /**
     * 只发送一次成功的数据，200ms超时报错
     * 这个地方是不应该是Single，因为可能发出多个事件，
     * 最后哪一个事件想下传递，这个是由后面的决定的
     */
    fun readSingle(inputStream: InputStream): Single<ByteArray> {
        return Single.create<ByteArray> {
            synchronized(inputStream) {
                innerRead(inputStream, null, it)
            }
        }
    }


    /**
     * 读取正确的数据，这个地方很关键,
     * 这个地方应该只是读取到正确的数据即可，
     * 不做其他的处理最好
     */
    private fun innerRead(inputStream: InputStream,
                          emitter: ObservableEmitter<ByteArray>?,
                          singleEmitter: SingleEmitter<ByteArray>?) {
        val buffer = ByteArray(256)
        val bytes = ByteArray(MAX_RES_LEN)

        var eventCount = 0
        var size = 0
        var index = 0
        var count = 0
        while (!isDisposed(emitter, singleEmitter)) {
            val ts = inputStream.available()
            if (ts <= 0) {
                continue
            }
            size = inputStream.read(buffer, 0, 256)
            if (size <= 0) {
                continue
            }
            count += size
            if (count > MAX_RES_LEN) {
                count = 0
                Arrays.fill(bytes, BYTE_ZERO)
            }
            System.arraycopy(buffer, 0, bytes, index, size)
            index += size
            if (count <= MIN_RES_BYTE) {
                continue
            }
            if (bytes[0] == HEAD) {
                val len = bytes[4] and 0xff.toByte()
                if (count < len + 7) {
                    continue
                }
                if (bytes[len + 6] != END) {
                    continue
                }
                val res = ByteArray(len + 7)
                System.arraycopy(bytes, 0, res, 0, len + 7)
                Log.i(TAG, "correct  response ${eventCount++}  buffer->${Tools.bytes2HexString(res)}")
                emitter?.onNext(res)
                singleEmitter?.onSuccess(res)
            }
            count = 0;index = 0;Arrays.fill(bytes, BYTE_ZERO)
        }
    }

    /**
     * 这个地方不应该跑出异常，数据应该直接舍弃
     * 最后由上游抛出超时或者等待下一个数据,
     * 实际上这个返回的值 指令+数据
     * AA 01 28 00 01 00 2A 8E
     */
    fun checkResponse(bytes: ByteArray): ByteArray? {
        val len = bytes.size
        if (bytes[0] != HEAD) {
            return null
        }
        if (bytes[len - 1] != END) {
            return null
        }
        if (len < 7)
            return null
        val height = bytes[3] and 0xff.toByte()
        val low = bytes[4] and 0xff.toByte()
        val dataLen = height * 255 + low
        val crc = RespUtils.checkSum(bytes)
        if (crc != bytes[len - 2])
            return null
        if (dataLen != 0 && len == dataLen + 7) {
            val resp = ByteArray(dataLen + 1)
            resp[0] = bytes[2]
            System.arraycopy(bytes, 5, resp, 1, dataLen)
            return resp
        }
        return null
    }


    /**
     * 从数据中获tag EPC信息
     * 电子标签信息
     */
    fun takeTagInfo(bytes: ByteArray): TagInfo? {
        var bytesLen = bytes.size
        var start = 0
        if (bytesLen > 15) {
            while (bytesLen > 5) {
                val len = bytes[start + 4] and 0xff.toByte()
                val cardLen = len + 7
                if (cardLen > bytesLen)
                    break
                val cardBytes = ByteArray(cardLen)
                System.arraycopy(bytes, start, cardBytes, 0, cardLen)
                val resolve = checkResponse(cardBytes)
                if (resolve != null && len > 5) {
                    val info = TagInfo()
                    val rssi = resolve[1] and 0xff.toByte()
                    val pc = byteArrayOf(resolve[2], resolve[3])
                    val epcBytes = ByteArray(len - 5)
                    System.arraycopy(resolve, 4, epcBytes, 0, epcBytes.size)
                    info.epc = Tools.Bytes2HexString(epcBytes, epcBytes.size)
                    info.pc = Integer.valueOf(Tools.Bytes2HexString(pc, 2))
                    info.rssi = rssi.toInt()
                    if (info.rssi > 127) info.rssi = (rssi and 0xFF.toByte()) - 0xFF
                    return info
                }
                start += cardLen
                bytesLen -= cardLen
            }
        }
        return null
    }


    private fun isDisposed(emitter: ObservableEmitter<ByteArray>?, singleEmitter: SingleEmitter<ByteArray>?): Boolean {
        if (emitter != null)
            return emitter.isDisposed
        if (singleEmitter != null)
            return singleEmitter.isDisposed

        return true
    }

}