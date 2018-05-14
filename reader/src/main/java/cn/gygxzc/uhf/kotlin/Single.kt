package cn.gygxzc.uhf.kotlin

import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.SingleSource

/**
 * @author niantuo
 * @createdTime 2018/5/14 21:46
 *
 *
 */

/**
 * 中途插进来另一个Observable，两者有关系
 * 我是想
 */
fun <T, O> Single<T>.otherSingle(call: (T, SingleObserver<in O>) -> Unit): Single<O> {
    return flatMap<O> { data -> SingleSource<O> { call.invoke(data, it) } }
}

fun <T> Single<T>.flatSingleEvent(): FlatSingleObserver<T> {
    return FlatSingleObserver(this)

}
