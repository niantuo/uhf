package cn.gygxzc.uhf.uhf.model

import cn.gygxzc.uhf.kotlin.FlatSingleObserver
import cn.gygxzc.uhf.uhf.entity.TagInfo
import cn.gygxzc.uhf.uhf.model.impl.ReadModel
import io.reactivex.Observable
import io.reactivex.Single

/**
 * @author niantuo
 * @createdTime 2018/5/14 9:57
 * 读取到的数据还是要经过处理才行
 *
 */
interface IReadModel {

    /**
     * 实时盘存,
     * @return
     */
    fun inventoryRealTime(): Single<TagInfo>

    /**
     * 多标签存盘，这个实际上我不知道什么意思
     * 到时候测试一下
     */
    fun inventoryMulti(): Observable<TagInfo>

    /**
     * 停止多标签存盘
     */
    fun stopInventoryMulti(): Single<Boolean>

    /**
     * 读数据
     * int memBank数据区
     * int startAddr数据区起始地址（以字为单位）
     * int length要读取的数据长度(以字为单位)
     * byte[] accessPassword 访问密码
     * 返回的byte[] 为  EPC + 读取的数据
     */
    fun readFrom6C(epc: String, memBank: Int, startAddr: Int, length: Int, accessPassword: ByteArray): Single<TagInfo>


    /**
     * 读取标签的唯一标号TID
     */
    fun readTID(epc: String, password: String): Single<TagInfo>

    /**
     * 读取用户区数据
     * 暂时只读取前几位，不会
     */
    fun readUser(epc: String, password: String): Single<TagInfo>

    companion object {
        fun create(): IReadModel = ReadModel()
    }

}