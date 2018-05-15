package cn.gygxzc.uhf.kotlin

import cn.gygxzc.uhf.LogUtils
import io.reactivex.Observer
import io.reactivex.SingleEmitter
import io.reactivex.disposables.Disposable

/**
 * @author niantuo
 * @createdTime 2018/5/15 9:18
 *
 *  必须得调用complete，才调用成功
 */
class ObserverToSingle<T> : Observer<T>, Disposable {
    companion object {
        const val TAG = "ObserverToSingle"
    }

    private var emitter: SingleEmitter<T>? = null
    private var mDisposable: Disposable? = null

    fun setSingleObserver(observer: SingleEmitter<T>): ObserverToSingle<T> {
        this.emitter = observer
        return this
    }

    override fun onComplete() {
        LogUtils.debug(TAG,"dispose")
    }

    override fun onError(e: Throwable) {
        if (emitter == null || emitter!!.isDisposed) return
        emitter?.onError(e)
    }

    override fun onNext(t: T) {
        mDisposable?.dispose()
        if (emitter == null || emitter!!.isDisposed) return
        emitter?.onSuccess(t)
    }

    override fun onSubscribe(d: Disposable) {
        this.mDisposable = d
    }

    override fun isDisposed(): Boolean {
        return mDisposable?.isDisposed == true
    }

    override fun dispose() {
        LogUtils.debug(TAG,"dispose")
        mDisposable?.dispose()
    }

}