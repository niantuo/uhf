package cn.gygxzc.uhf.uhf.cmd

/**
 * @author niantuo
 * @createdTime 2018/5/14 10:06
 *
 *
 */

class Factory{

    companion object {
        /**
         * 固定的指令工厂
         * 懒加载，
         */
        val CMD:CMDFactory by lazy { CMDFactory.create() }
    }
}