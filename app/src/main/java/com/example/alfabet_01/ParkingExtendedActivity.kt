package com.example.alfabet_01

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.alfabet_01.databinding.ActivityParkinExtendedBinding


class ParkingExtendedActivity : AppCompatActivity() {
    lateinit var binding: ActivityParkinExtendedBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParkinExtendedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val model = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("ParkingData", Model::class.java)
        } else {
            intent.getSerializableExtra("ParkingData") as Model
        }
        if (model != null) {
            fillData(model)
        }
    }

    private fun fillData(model: Model) {
        with (binding) {
            textViewBeginning.text = model.dateString
            textViewNote.text = model.text
            textViewLatitude.text = model.latitude.toString()
            textViewLongitude.text = model.longitude.toString()
        }
        if (!model.imgPath.isNullOrEmpty()) binding.imageView10.setImageURI(Uri.parse(model.imgPath))
        else {
            binding.cardViewPhoto.visibility = View.GONE
        }

    }

    fun onClickDeleteItem(view: View) {

    }

    fun onClickBack(view: View) {
        finish()
    }
}