package com.webserver.server

interface ConnectionListener {
    fun onUpdate(snapshot: ConnectionSnapshot)
}