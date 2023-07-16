package com.webserver

import com.webserver.server.ConnectionSnapshot

data class MainUiState(
    val isRunning: Boolean,
    val connections: ArrayDeque<ConnectionSnapshot>,
)