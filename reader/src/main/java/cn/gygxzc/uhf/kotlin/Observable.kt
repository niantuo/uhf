package cn.gygxzc.uhf.kotlin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer

/**
 * @author niantuo
 * @createdTime 2018/5/12 23:10
 *
 *  转换一下，平铺的形式注册事件，这样看起来效果更好
 */

class ObservableFlatObserver<T>(val observable: Observable<T>) : FlatObserver<T>() {
    override fun subscribe() {
        observable.subscribe(this)
    }
}


fun <T> Observable<T>.flatEvent(): FlatObserver<T> {
    return ObservableFlatObserver(this)
}