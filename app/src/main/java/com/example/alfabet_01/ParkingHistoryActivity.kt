package com.example.alfabet_01

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alfabet_01.databinding.ActivityParkinHistoryBinding
import java.io.File

class ParkingHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParkinHistoryBinding
    private val historyAdapter = DataHistoryAdapter()
    var modelListGL  : ArrayList<Model>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParkinHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var modelList  : ArrayList<Model>? = null

        modelList = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("qwerty", ArrayList<Model>()::class.java)
        } else {
            intent.getSerializableExtra("qwerty") as ArrayList<Model>?
        }
        modelListGL = modelList
        if (modelList.isNullOrEmpty()) {
            setEmptyInvis()
        } else {
            modelList.reverse()
            historyAdapter.addModel(modelList)
            binding.recyclerHistory.layoutManager = LinearLayoutManager(this)
            binding.recyclerHistory.adapter = historyAdapter
        }
    }

    /*private fun init() {
        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerHistory.adapter = historyAdapter
    }*/
    private fun setEmptyInvis() {
        binding.recyclerHistory.visibility = View.INVISIBLE
        binding.textView8.visibility = View.VISIBLE
    }
    fun onClickDeleteAll(view: View) {

        if (!modelListGL.isNullOrEmpty()) {
            historyAdapter.deleteAll()
            for (i in modelListGL!!.indices) {
                if (modelListGL!![i].imgPath != null) {
                    fileDelete(modelListGL!![i].imgPath!!)
                }
            }
            setEmptyInvis()
            modelListGL = null
        }
    }

    private fun fileDelete(uriString: String) {
        Log.d("Tag", uriString)
        val path = Uri.parse(uriString).path
        Log.d("Tag", path!!)
        val fdelete = File(path)
        if (fdelete.exists()) {
                if (fdelete.delete()) Log.d("Tag", "File delete")
                else Log.d("Tag", "File not delete")
        }
        else Log.d("Tag", "File not exist")

    }

    fun onClickBack(view: View) {
        val i = Intent()
        i.putExtra("modelListFromHistory", modelListGL)
        setResult(RESULT_OK, i)
        finish()
    }

}