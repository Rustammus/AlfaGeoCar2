package com.example.alfabet_01

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class LoadingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
    }

    fun onClickLoading(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun onClickFirstLoading(view: View) {
        val intent = Intent(this, HelpActivity::class.java)
        intent.putExtra("key", true)
        startActivity(intent)
        finish()
    }
}