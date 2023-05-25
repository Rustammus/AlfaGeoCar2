package com.example.alfabet_01

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import com.example.alfabet_01.databinding.ActivityHelpBinding

class HelpActivity : AppCompatActivity() {
    lateinit var binding: ActivityHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val data = intent.getBooleanExtra("key", false)
        if (data) {
            binding.constraintLayout.visibility = View.GONE
            binding.button.visibility = View.VISIBLE
            binding.textView7.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Tag", "Help destroy")
    }

    fun onClickBack(view: View) {
        finish()
    }

    fun onClickNextStart(view: View) {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.CAMERA), 102)
        binding.button.setText(R.string.help_get_permission_start)
        binding.button.setOnClickListener(){
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("key", true)
            startActivity(intent)
            finish()
        }
    }
}