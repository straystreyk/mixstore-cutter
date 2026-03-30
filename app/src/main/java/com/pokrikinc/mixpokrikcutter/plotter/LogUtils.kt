package com.pokrikinc.mixpokrikcutter.plotter

import android.util.Log
import com.pokrikinc.mixpokrikcutter.BuildConfig

object LogUtils {
    fun e(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message)
        }
    }

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
}
