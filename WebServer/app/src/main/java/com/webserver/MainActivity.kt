package com.webserver

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.webserver.databinding.ActivityMainBinding
import com.webserver.server.ClientWorker
import com.webserver.server.ConnectionListener
import com.webserver.server.ConnectionSnapshot
import com.webserver.server.EndpointHandler
import com.webserver.server.WebServer


class MainActivity : AppCompatActivity(), ConnectionListener {

    private lateinit var webServer: WebServer

    private lateinit var state: MainUiState

    private lateinit var binding: ActivityMainBinding

    private val adapter = MainRecyclerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webServer = WebServer(
            clientWorker = ClientWorker(
                endpointHandler = EndpointHandler(
                    deviceManager = DeviceManager(this.applicationContext)
                )
            ),
            listener = this
        )

        state = MainUiState(
            isRunning = false,
            ArrayDeque(128),
        )

        update()

        binding.btnStatus.setOnClickListener {
            if (state.isRunning) {
                webServer.stop()
            } else {
                webServer.start()
            }

            state = state.copy(
                isRunning = webServer.isRunning()
            )

            update()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

    }

    override fun onDestroy() {
        super.onDestroy()
        webServer.stop()
    }

    private fun update() {
        binding.btnStatus.text = if (state.isRunning) {
            "Stop"
        } else {
            "Start"
        }

        binding.tvStatus.text = if (state.isRunning) {
            "Server is running on port: 8080"
        } else {
            "Server stopped"
        }

        binding.tvTotalRequests.text = "Requests: ${state.connections.size}"
        adapter.updateData(state.connections)
    }

    override fun onUpdate(snapshot: ConnectionSnapshot) {
        state.connections.addFirst(snapshot)

        update()
    }
}