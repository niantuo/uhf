package cn.gygxzc.uhf.kotlin

import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * @author niantuo
 * @createdTime 2018/5/12 23:13
 *
 *
 */
open class FlatObserver<T> : Observer<T> {

    private var complete: (() -> Unit)? = null
    private var errorHandler: ((Throwable) -> Unit)? = null
    private var nextHandler: ((T) -> Unit)? = null
    private var disposableHandler: ((Disposable) -> Unit)? = null


    fun doOnComplete(consumer: () -> Unit): FlatObserver<T> {
        this.complete = consumer
        return this
    }

    fun doOnError(consumer: (Throwable) -> Unit): FlatObserver<T> {
        this.errorHandler = consumer
        return this
    }


    fun doOnNext(consumer: (T) -> Unit): FlatObserver<T> {
        this.nextHandler = consumer
        return this
    }

    fun onDisposable(consumer: (Disposable) -> Unit): FlatObserver<T> {
        this.disposableHandler = consumer
        return this
    }

    open fun subscribe() {

    }


    override fun onComplete() {
        complete?.invoke()
    }

    override fun onError(e: Throwable) {
        errorHandler?.invoke(e)
    }

    override fun onNext(t: T) {
        nextHandler?.invoke(t)
    }

    override fun onSubscribe(d: Disposable) {
        disposableHandler?.invoke(d)
    }
}