package cn.gygxzc.uhf.exception

import cn.gygxzc.uhf.event.ErrorCode
import cn.gygxzc.uhf.uhf.enums.UHFExEnums

/**
 * @author niantuo
 * @createdTime 2018/5/12 16:33
 *
 *
 */
class RFIDException(val code: Int = 0,
                    message: String = "success") : RuntimeException(message) {

    constructor(error: ErrorCode) : this(error.code, error.desc)

    constructor(error: UHFExEnums) : this(error.code, error.err)
}