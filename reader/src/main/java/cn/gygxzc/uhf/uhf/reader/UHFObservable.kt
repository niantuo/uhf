package cn.gygxzc.uhf.uhf.reader

import cn.gygxzc.uhf.uhf.entity.TagInfo
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
 * 不改变错误抛出
 * 但是会将code 返回成封装的错误类型
 * 注意的是，这个只能处理特定类型的错误
 * @see cn.gygxzc.uhf.uhf.UHFException
 */
fun Observable<ByteArray>.errorHandler(): Observable<ByteArray> {


    return retryWhen { errors -> errors.flatMap<ByteArray> { Observable.error(it) } }


}