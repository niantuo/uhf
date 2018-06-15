package cn.gygxzc.uhf.kotlin

import io.reactivex.functions.Consumer

/**
 * @author niantuo
 * @createdTime 2018/6/15 9:09
 *
 *
 */
class RxConsumer<T>(private val callback: (T) -> Unit) : Consumer<T> {

    override fun accept(t: T) {
        try {
            callback.invoke(t)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}