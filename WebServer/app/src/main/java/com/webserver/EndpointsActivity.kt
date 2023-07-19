package com.webserver

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.webserver.databinding.ActivityEndpointsBinding
import com.webserver.databinding.ItemEndpointBinding


class EndpointsActivity : AppCompatActivity() {

    data class Item(
        val name: String, val requests: Int
    )

    class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {

        class ViewHolder(private val binding: ItemEndpointBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(item: Item) {
                binding.tvEndpoint.text = "/${item.name}"
                binding.tvRequests.text = "Requests: ${item.requests}"
            }
        }

        init {
            setHasStableIds(true)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)

            return ViewHolder(ItemEndpointBinding.inflate(inflater, parent, false))
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemId(position: Int): Long {
            return items[position].name.hashCode().toLong()
        }

        fun update(items: List<Item>) {
            this.items.clear()
            this.items.addAll(items)

            notifyDataSetChanged()
        }

        private val items = mutableListOf<Item>()
    }

    private val launcher =
        registerForActivityResult<String, Uri>(ActivityResultContracts.GetContent()) { uri ->
            val content = contentResolver.openInputStream(uri)!!.run {
                bufferedReader().readText()
            }

            val view = layoutInflater.inflate(R.layout.dialog_endpoint, null, false)
            val input = view.findViewById<TextInputEditText>(R.id.et_endpoint)

            val dialog = AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Create"
                ) { dialog, which ->
                    input.text?.toString()?.takeIf { it.isNotEmpty() }?.let { name ->
                        WebServerApp.endpointStorage.createEndpoint(name, content)
                    }

                    refresh()

                    dialog.dismiss()
                }
                .setNegativeButton("Cancel"
                ) { dialog, which -> dialog.dismiss() }
                .create()

            dialog.show()
        }

    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityEndpointsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = Adapter()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        refresh()

        binding.fabCreate.setOnClickListener {
            launcher.launch("text/html")
        }
    }

    private fun refresh() {
        val items = WebServerApp.endpointStorage.getEndpointNames().map {  endpoint ->
            Item(
                name = endpoint,
                requests = WebServerApp.endpointStorage.getViews(endpoint)
            )
        }

        adapter.update(items)
    }
}
