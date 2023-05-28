package com.example.alfabet_01

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

class LoadingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)



    }

    fun onClickLoading(view: View) {
        launchApp(false)
    }

    fun onClickFirstLoading(view: View) {
        launchApp(true)
    }

    private fun launchApp(isFirstLaunch: Boolean) {

        val pref = getSharedPreferences("pref", MODE_PRIVATE)
        val locale = pref.getString("locale", null)
        Log.d("Tag", "local is ${locale.toString()}")
        val theme = pref.getInt("theme", -1)
        if (theme != -1) AppCompatDelegate.setDefaultNightMode(theme)
        if (!locale.isNullOrEmpty()) this.resources.configuration.setLocale(Locale(locale))
        val intent = if (isFirstLaunch) Intent(this, HelpActivity::class.java) else Intent(this, MainActivity::class.java)
        intent.putExtra("key", isFirstLaunch)
        startActivity(intent)
        finish()
    }
}