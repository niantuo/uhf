package cn.gygxzc.uhf

import android.util.Log

object LogUtils {
    private var debug = false


    fun setIsDebug() {
        debug = true
    }

    fun debug(tag: String, message: String) {
        if (!debug) return
        Log.d(tag, message)
    }

    fun info(tag: String, message: String) {
        Log.i(tag, message)
    }

}