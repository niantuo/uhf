package cn.gygxzc.uhf.uhf.model

import cn.gygxzc.uhf.uhf.enums.SENSITIVE
import cn.gygxzc.uhf.uhf.model.impl.SettingModel
import io.reactivex.Observable
import io.reactivex.Single

/**
 * @author niantuo
 * @createdTime 2018/5/14 13:45
 *
 *  读写器的参数设置或者获取
 */
interface ISettingModel {

    /**
     * 获取硬件版本
     */
    fun firmware(): Single<String>

    /**
     * 设置波特率,这个人家都没有实现
     * @return
     */
    fun setBaudRate(): Observable<ByteArray>

    /**
     * 设置输入输出功率
     * @param value
     * @return
     */
    fun setOutputPower(value: Int): Single<Boolean>

    /**
     * 获取输出功率
     */
    fun getOutputPower():Single<Int>

    /**
     * 设置灵敏度
     * @param value
     */
    fun setSensitivity(value: Int):Single<Boolean>

    /**
     * 获取灵敏度，只需要这一个参数就可以了
     * 其他的用默认的即可
     * 设置会将其他的参数设置为默认的
     */
    fun getSensitivity():Single<SENSITIVE>

    /**
     * 中国和国外其他地方不一样，设置频率，这个暂时不实现，没有什么实际作用
     * @param startFrequency 起始频点
     * @param freqSpace 频点间隔
     * @param freqQuality 频点数量
     * @return
     */
    fun setFrequency(startFrequency: Int, freqSpace: Int, freqQuality: Int): Single<ByteArray>

    companion object {
        fun create(): ISettingModel = SettingModel()
    }

}