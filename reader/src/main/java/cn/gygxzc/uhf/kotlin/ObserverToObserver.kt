package cn.gygxzc.uhf.kotlin

import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * @author niantuo
 * @createdTime 2018/5/14 19:57
 *
 *
 */
class ObserverToObserver<T>(private val observer: Observer<in T>) : FlatSingleObserver<T>() {

    override fun onNext(t: T) {
        super.onNext(t)
        observer.onNext(t)
        observer.onComplete()
    }

    override fun onError(e: Throwable) {
        super.onError(e)
        observer.onError(e)
    }

    override fun onSubscribe(d: Disposable) {
        super.onSubscribe(d)
        observer.onSubscribe(d)
    }
}