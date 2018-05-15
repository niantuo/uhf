package cn.gygxzc.uhf.uhf.model

import cn.gygxzc.uhf.uhf.model.impl.SelectModel
import io.reactivex.Observable
import io.reactivex.Single

/**
 * @author niantuo
 * @createdTime 2018/5/14 13:13
 *
 *
 */
interface ISelectModel {

    /**
     * 这个选择就应该是Single，没有返回很多值的说法
     */
    fun select(epc: String): Single<Boolean>

    fun selectCMD(epc: String): ByteArray

    fun unSelect(): Single<Boolean>


    companion object {
        fun create(): ISelectModel = SelectModel()
    }
}