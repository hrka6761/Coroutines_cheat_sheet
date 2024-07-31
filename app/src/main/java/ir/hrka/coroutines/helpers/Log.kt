package ir.hrka.coroutines.helpers

import android.util.Log

object Log {

    private const val TAG = "HAMIDREZA"

    fun printYellow(msg: String, showThreadInfo: Boolean = true) {

        var totalMsg: String = msg

        if (showThreadInfo)
            totalMsg += "          ====>  ${Thread.currentThread().id}:${Thread.currentThread().name}"

        Log.i(TAG, totalMsg)
    }

    fun printRed(msg: String, showThreadInfo: Boolean = true) {

        var totalMsg: String = msg

        if (showThreadInfo)
            totalMsg += "          ====>  ${Thread.currentThread().id}:${Thread.currentThread().name}"

        Log.e(TAG, totalMsg)
    }

    fun printBlue(msg: String, showThreadInfo: Boolean = true) {

        var totalMsg: String = msg

        if (showThreadInfo)
            totalMsg += "          ====>  ${Thread.currentThread().id}:${Thread.currentThread().name}"

        Log.d(TAG, totalMsg)
    }

    fun printWhite(msg: String, showThreadInfo: Boolean = true) {

        var totalMsg: String = msg

        if (showThreadInfo)
            totalMsg += "          ====>  ${Thread.currentThread().id}:${Thread.currentThread().name}"

        Log.v(TAG, totalMsg)
    }
}