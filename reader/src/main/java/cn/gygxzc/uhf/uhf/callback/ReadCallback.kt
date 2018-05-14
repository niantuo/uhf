package cn.gygxzc.uhf.uhf.callback

import io.reactivex.disposables.Disposable

/**
 * @author niantuo
 * @createdTime 2018/5/14 21:32
 *
 *
 */
class ReadCallback : Disposable {

    private var mDisposed: Boolean = false

    override fun isDisposed(): Boolean {
        return mDisposed
    }

    override fun dispose() {
        mDisposed = true
    }
}