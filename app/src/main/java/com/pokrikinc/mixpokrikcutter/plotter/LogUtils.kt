package com.pokrikinc.mixpokrikcutter.plotter

object LogUtils {
    fun e(tag: String, message: String) {
        println("ERROR [$tag]: $message")
    }

    fun d(tag: String, message: String) {
        println("DEBUG [$tag]: $message")
    }
}