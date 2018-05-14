package cn.gygxzc.uhf.uhf.enums

/**
 * @author niantuo
 * @createdTime 2018/5/14 14:56
 *
 *
 */
enum class MemBank(val code:Int,
                   val desc:String) {

     RESEVER_BANK(0,"保留区，密码区"),
     EPC_BANK(1,"EPC区"),
     TID_BANK(2,"TID区"),
     USER_BANK(3,"用户区")
}