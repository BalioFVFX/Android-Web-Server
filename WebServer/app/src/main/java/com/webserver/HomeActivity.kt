package com.webserver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.webserver.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnServer.setOnClickListener {
            startActivity(Intent(this, ServerActivity::class.java))
        }

        binding.btnEndpoints.setOnClickListener {
            startActivity(Intent(this, EndpointsActivity::class.java))
        }
    }
}