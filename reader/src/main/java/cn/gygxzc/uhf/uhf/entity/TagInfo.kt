package cn.gygxzc.uhf.uhf.entity


/**
 * @author niantuo
 * @createdTime 2018/5/13 0:06
 *
 *  这个
 */
data class TagInfo(var epc: String = "",
                   var pc: Int = 0,
                   var rssi: Int = 0,
                   var data: String = "") {


    override fun equals(other: Any?): Boolean {
        if (other !is TagInfo) return false
        return epc == other.epc
    }

    override fun hashCode(): Int {
        return epc.hashCode()
    }

}