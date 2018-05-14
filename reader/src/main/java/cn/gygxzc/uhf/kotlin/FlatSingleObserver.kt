package cn.gygxzc.uhf.kotlin

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable

/**
 * @author niantuo
 * @createdTime 2018/5/14 19:40
 *
 *
 */
open class FlatSingleObserver<T>(val observable: Single<T>? = null) : SingleObserver<T> {

    private var mDisposable: Disposable? = null

    private var errorHandler: ((Throwable) -> Unit)? = null
    private var nextHandler: ((T) -> Unit)? = null
    private var disposableHandler: ((Disposable) -> Unit)? = null


    fun doOnError(consumer: (Throwable) -> Unit): FlatSingleObserver<T> {
        this.errorHandler = consumer
        return this
    }


    fun doOnNext(consumer: (T) -> Unit): FlatSingleObserver<T> {
        this.nextHandler = consumer
        return this
    }

    fun onDisposable(consumer: (Disposable) -> Unit): FlatSingleObserver<T> {
        this.disposableHandler = consumer
        return this
    }


    override fun onError(e: Throwable) {
        mDisposable?.dispose()
        errorHandler?.invoke(e)
    }


    override fun onSuccess(t: T) {
        nextHandler?.invoke(t)
    }


    override fun onSubscribe(d: Disposable) {
        this.mDisposable = d
        disposableHandler?.invoke(d)
    }

    fun subscribe() {
        observable?.subscribe(this)
    }
}