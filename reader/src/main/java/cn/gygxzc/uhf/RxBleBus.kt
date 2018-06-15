package cn.gygxzc.uhf

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * @author niantuo
 * @createdTime 2018/6/15 9:05
 * 实现一个很简单的消息发送功能
 *
 */
class RxBleBus {

    companion object {
        val bus = RxBleBus()

        inline fun <reified T : Any> register() = bus.register<T>()
        fun post(event: Any) = bus.post(event)
    }


    val mPublish = PublishSubject.create<Any>()!!

    /**
     * 不能发送错误事件
     */
    inline fun <reified T : Any> register(): Observable<T> {
        return mPublish.ofType(T::class.java)
    }

    fun post(event: Any) {
        mPublish.onNext(event)
    }


}