package cn.gygxzc.uhf.kotlin

import io.reactivex.Observer
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable

/**
 * @author niantuo
 * @createdTime 2018/5/14 21:53
 *
 *
 */
class SingleToObserver<T>(private val observer: Observer<T>) : SingleObserver<T> {

    override fun onSubscribe(d: Disposable) {
        observer.onSubscribe(d)
    }

    override fun onError(e: Throwable) {
        observer.onError(e)
    }

    override fun onSuccess(t: T) {
        observer.onNext(t)
        observer.onComplete()
    }
}