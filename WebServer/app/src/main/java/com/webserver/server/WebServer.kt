package com.webserver.server

import android.os.Handler
import android.os.Looper
import com.webserver.Logger
import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.Executors

class WebServer(
    private val clientWorker: ClientWorker,
    private val listener: ConnectionListener
) {

    companion object {
        const val TAG = "WebServer"
    }

    @Volatile
    private lateinit var serverSocket: ServerSocket

    @Volatile
    private var isServing = false

    @Volatile
    private var isStopping = false

    private val executor = Executors.newCachedThreadPool()

    private val handler = Handler(Looper.getMainLooper())

    fun start() {
        if (isServing || isStopping) {
            return
        }

        Logger.d(TAG, "Start requested")

        isServing = true

        serverSocketThread().start()
    }

    fun stop() {
        if (!isServing || isStopping) {
            return
        }

        Logger.d(TAG, "Stop requested")

        isServing = false
        isStopping = true

        serverSocket.close()
    }

    fun isRunning(): Boolean {
        return isServing && !isStopping
    }

    private fun serverSocketThread(): Thread {
        serverSocket = ServerSocket(8080)
        return Thread() {
            Logger.d(TAG, "Server socket thread started. Waiting for clients")
            while (isServing) {
                try {
                    val client = serverSocket.accept().apply {
                        soTimeout = 2000
                    }

                    Logger.d(TAG, "Client connected.")

                    executor.execute {
                        clientWorker.serve(client)?.let { snapshot ->
                            handler.post {
                                listener.onUpdate(snapshot)
                            }
                            Logger.d(TAG, "Client was served.")
                        }
                    }

                } catch (ioException: IOException) {
                    Logger.d(TAG, "ServerSocket received IOException")
                }
            }

            isStopping = false
            Logger.d(TAG, "ServerSocket thread shutting down...")

        }.apply { name = "ServerSocket Thread" }
    }
}