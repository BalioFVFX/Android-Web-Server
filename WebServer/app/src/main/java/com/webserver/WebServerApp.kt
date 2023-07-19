package com.webserver

import android.annotation.SuppressLint
import android.app.Application

class WebServerApp : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var endpointStorage: EndpointStorage
    }

    override fun onCreate() {
        super.onCreate()

        endpointStorage = EndpointStorage(this)
    }
}