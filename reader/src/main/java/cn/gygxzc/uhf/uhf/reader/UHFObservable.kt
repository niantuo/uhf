package cn.gygxzc.uhf.uhf.reader

import cn.gygxzc.uhf.LogUtils
import cn.gygxzc.uhf.uhf.entity.TagInfo
import cn.gygxzc.uhf.uhf.model.impl.ReadModel
import cn.gygxzc.uhf.uhf.util.Tools
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Single
import io.reactivex.SingleSource

/**
 * @author niantuo
 * @createdTime 2018/5/12 23:39
 *
 *
 */

/**
 * 这个是读取EPC标签,从获取的数据中取出来
 * 这个也是挺关键的
 */
fun Observable<ByteArray>.toEPCFlatMap(): Observable<TagInfo> {
    return flatMap { bytes ->
        ObservableSource<TagInfo> {
            val info = UHFReader.takeTagInfo(bytes)
            if (info != null)
                it.onNext(info)
        }
    }
}

/**
 * 获取标签数据
 */
fun Single<ByteArray>.toTagInfo(): Single<TagInfo> {
    return flatMap<TagInfo> { bytes ->
        SingleSource {
            val info = UHFReader.takeTagInfo(bytes)
            if (info != null)
                it.onSuccess(info)
        }
    }
}

/**
 * 停止标签多次轮询指令返回
 */
fun Single<ByteArray>.stopMultiInventorySuccess(): Single<Boolean> {
    return flatMap { bytes ->
        SingleSource<Boolean> {
            val resolve = UHFReader.checkResponse(bytes)
                    ?: return@SingleSource
            if (resolve[0] == 0x28.toByte()) {
                it.onSuccess(true)
            }
        }
    }
}


/**
 * 获取存储的数据，装换成实体类
 */
fun Observable<ByteArray>.toTakeMemData(epc: String): Observable<TagInfo> {
    return flatMap { response ->
        ObservableSource<TagInfo> {
            LogUtils.debug(ReadModel.TAG, "receive response，check CRC->${Tools.bytes2HexString(response)}")
            val resolve = UHFReader.checkResponse(response)
                    ?: return@ObservableSource
            if (resolve[0] == 0x39.toByte()) {
                val tagInfo = TagInfo(epc)
                val lenData = resolve.size - resolve[1].toInt() - 2
                val data = ByteArray(lenData)
                System.arraycopy(resolve, resolve[1] + 2, data, 0, lenData)
                tagInfo.data = Tools.bytes2HexString(data)
                LogUtils.debug(ReadModel.TAG, "correct response->$tagInfo")
                it.onNext(tagInfo)
                it.onComplete()
            } else {
                val errCode = ByteArray(resolve.size - 1)
                System.arraycopy(resolve, 1, errCode, 0, errCode.size)
                LogUtils.debug(ReadModel.TAG, "error code ->${Tools.bytes2HexString(errCode)}")
            }
        }
    }
}

/**
 * 不改变错误抛出
 * 但是会将code 返回成封装的错误类型
 * 注意的是，这个只能处理特定类型的错误
 * @see cn.gygxzc.uhf.uhf.UHFException
 */
fun Observable<ByteArray>.errorHandler(): Observable<ByteArray> {


    return retryWhen { errors -> errors.flatMap<ByteArray> { Observable.error(it) } }


}