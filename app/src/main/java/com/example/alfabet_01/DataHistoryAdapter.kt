package com.example.alfabet_01

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.alfabet_01.databinding.ParkinHistoryItemBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class DataHistoryAdapter: RecyclerView.Adapter<DataHistoryAdapter.DataHistoryHolder>() {
    private val modelList = ArrayList<Model>()
    class DataHistoryHolder(item: View): RecyclerView.ViewHolder(item){
        private val binding = ParkinHistoryItemBinding.bind(item)
        fun bind(DataHistory: Model) {
            val viewText = if (DataHistory.text.length > 30) DataHistory.text.substring(0, 30) + "..."
                            else DataHistory.text
            binding.textView9.text = " $viewText \n" + DataHistory.dateString

            binding.cardView1.setOnClickListener {
                val intent = Intent(it.context, ParkingExtendedActivity::class.java)
                intent.putExtra("ParkingData", DataHistory)
                it.context.startActivity(intent)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataHistoryHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.parkin_history_item, parent, false)
        return DataHistoryHolder(view)
    }

    override fun getItemCount(): Int {
        return modelList.size
    }

    override fun onBindViewHolder(holder: DataHistoryHolder, position: Int) {
        holder.bind(modelList[position])
    }

    fun addModel (modArray: ArrayList<Model>) {
        modelList.addAll(modArray)
        notifyDataSetChanged()
    }

    fun deleteAll () {
        modelList.clear()
        notifyDataSetChanged()
    }
}