package cn.gygxzc.uhf.uhf.model

import cn.gygxzc.uhf.uhf.model.impl.WriteModel
import io.reactivex.Single

/**
 * @author niantuo
 * @createdTime 2018/5/14 14:28
 *  写数据
 *  实际上还是可以写的，主要是写用户区，EPC两个数据
 *  密码和写保护最好是桌面式读写器，
 *  这个出错的概率太大，简直了
 *
 */
interface IWriteModel {

    /**
     * 写数据
     * byte[] password 访问密码
     * int memBank 数据区
     * int startAddr 起始地址（以WORD为单位）
     * int wordCnt 写入数据的长度（以WORD为单位 1word = 2bytes）
     * byte[] data 写入数据
     * 返回 boolean，true写入数据正确，false写入数据失败
     */
    fun writeTo6C(password: ByteArray, memBank: Int, startAddr: Int,
                  dataLen: Int, originData: ByteArray, origin: String): Single<ByteArray>

    /**
     * @param epc 要写入的新的PEC数据
     * @param origin  原有的EPC数据
     * 写数据需要先锁定，在写入，因此需要原有的EPC数据
     */
    fun writeEPC(password: String, epc: String, origin: String): Single<ByteArray>

    /**
     * 实际上这种操作，返回值无法就是成功或者失败，
     * 所以实际上调用了 onComplete 就能表示操作已成功了
     * 写用户数据不支持部分写，如果位数不够，将在前面补0
     * 如果超出，将不会提醒，将会省略掉最后的数据
     */
    fun writeToUser(password: String, data: String, origin: String): Single<ByteArray>


    companion object {
        fun create(): IWriteModel = WriteModel()
    }
}