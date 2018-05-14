package cn.gygxzc.uhf.uhf.enums

/**
 * @author niantuo
 * @createdTime 2018/5/14 13:50
 *
 *
 */
enum class SENSITIVE(val code:Int,val desc:String) {
    HIGH(3,"高灵敏度"),
    MIDDLE(2,"中等灵敏度"),
    LOW(1,"低灵敏度"),
    VERY_LOW(0,"极低灵敏度")
}