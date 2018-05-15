package cn.gygxzc.uhf.uhf.enums

/**
 * @author niantuo
 * @createdTime 2018/5/14 21:40
 *
 *
 */
enum class UHFExEnums(val code:Int,val err:String) {

    ERR_NONE(0,"操作成功"),
    ERR_RESPONSE(1,"操作失败"),//这个要根据具体的RFID设备返回值才能确定最终的错误类型
    ERR_SELECT_EPC(2,"选定EPC标签失败"),

    ERR_RESPONSE_DATA(3,"数据校验失败"),

    ERR_NO_RESPONSE(4,"没有回复"),

    ERR_WRITE_FAILED(5,"数据写入失败")
}