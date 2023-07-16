package com.webserver

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.webserver.databinding.ItemMainBinding
import com.webserver.server.ConnectionSnapshot
import java.util.Locale

class MainRecyclerAdapter : RecyclerView.Adapter<MainRecyclerAdapter.ViewHolder>() {

    private var data = ArrayDeque<ConnectionSnapshot>()

    init {
        setHasStableIds(true)
    }


    class ViewHolder(private val binding: ItemMainBinding) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            val dateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss a", Locale.getDefault())
        }

        fun bind(snapshot: ConnectionSnapshot) {
            binding.tvReqEndpoint.text = snapshot.request.endpoint.trim()
            binding.tvReqMethod.text = snapshot.request.method.name.trim()
            binding.tvReqTimestamp.text =
                "Requested at: ${dateFormat.format(snapshot.request.timestamp)}"

            binding.tvResponseHeaders.text = snapshot.response.headers.trim()
            binding.tvResponseTime.text =
                "Responded at: ${dateFormat.format(snapshot.response.timestamp)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMainBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemId(position: Int): Long {
        return data[position].uuid.hashCode().toLong()
    }

    fun updateData(snapshots: ArrayDeque<ConnectionSnapshot>) {
        this.data = snapshots
        notifyDataSetChanged()
    }
}