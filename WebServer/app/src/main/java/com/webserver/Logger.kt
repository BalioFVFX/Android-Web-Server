package com.webserver

import android.util.Log

object Logger {
    fun d(tag: String, message: String) {
        Log.d(tag, message)
    }
}