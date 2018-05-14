package cn.gygxzc.uhf.uhf

import cn.gygxzc.uhf.uhf.enums.UHFExEnums

/**
 * @author niantuo
 * @createdTime 2018/5/14 14:49
 *
 *
 */
class UHFException(val bytes: ByteArray?,
                   val code: Int = 0,
                   message: String) : RuntimeException(message) {

    constructor(ex: UHFExEnums, bytes: ByteArray? = null) : this(bytes, ex.code, ex.err)
}