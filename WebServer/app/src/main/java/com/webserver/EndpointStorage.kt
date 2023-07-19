package com.webserver

import android.content.Context
import java.io.File

class EndpointStorage(private val context: Context) {

    private val prefs = context.getSharedPreferences("endpoints", Context.MODE_PRIVATE)

    fun markViewed(endpoint: String) {
        val current = getViews(endpoint)

        prefs.edit().putInt(endpoint, current + 1).apply()
    }


    fun getViews(endpoint: String) : Int {
        return prefs.getInt(endpoint, 0)
    }

    fun getEndpointNames() : List <String> {
        return getEndpointsDir().listFiles()?.map {
            it.name
        } ?: emptyList()
    }

    fun getEndpointContent(name: String) : String? {
        val file = File(getEndpointsDir().absolutePath + File.separatorChar + name)

        if (file.exists()) {
            return file.readText()
        }

        return null
    }

    fun createEndpoint(endpoint: String, content: String) {
        val file = File(getEndpointsDir().absolutePath + File.separatorChar + endpoint)

        file.writeText(content)
        prefs.edit().putInt(endpoint, 0).apply()
    }

    fun deleteEndpoint(endpoint: String) {
        prefs.edit().putInt(endpoint, 0).apply()

        val file = File(getEndpointsDir().absolutePath + File.separatorChar + endpoint)

        if (file.exists()) {
            file.delete()
        }
    }

    private fun getEndpointsDir() : File {
        val path = context.cacheDir.absolutePath + File.separatorChar + "endpoints"

        val file = File(path)

        if (file.exists()) {
            return file
        }

        if (!file.mkdir()) {
            throw IllegalStateException()
        }

        return file
    }
}