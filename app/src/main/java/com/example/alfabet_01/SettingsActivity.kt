package com.example.alfabet_01

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

import java.util.Locale


class SettingsActivity : AppCompatActivity() {
    private var modelList :  ArrayList<Model>? = null
    private var launcherHistory : ActivityResultLauncher<Intent>? = null
    private var locale: String = "en_US"
    private var theme: Int = AppCompatDelegate.MODE_NIGHT_YES
    private var isConfChange = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        modelList = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("qwerty", ArrayList<Model>()::class.java)
        } else {
            intent.getSerializableExtra("qwerty") as ArrayList<Model>?
        }
        modelList?.reverse()

        launcherHistory = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {

                modelList = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getSerializableExtra("modelListFromHistory", ArrayList<Model>()::class.java)
                } else {
                    result.data?.getSerializableExtra("modelListFromHistory") as ArrayList<Model>?
                }

                Log.d("Tag", "Reverted from History")
            } else Log.d("TagAAAA", result.resultCode.toString())
        }
        Log.d("Tag", "Settings onCreate")
    }

    override fun finish() {
        val i = Intent()
        i.putExtra("modelFromSet", modelList)
        setResult(RESULT_OK, i)
        super.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Tag", "Settings destroyed")
    }

    fun onClickBack(view: View) {
        finish()
    }

    fun onClickHistory(view: View) {
        val intent = Intent(this, ParkingHistoryActivity::class.java)
        intent.putExtra("qwerty", modelList)
        launcherHistory!!.launch(intent)
    }


    fun onClickEn(view: View) {
        //this.resources.configuration.setLocale(Locale("en", "US"))
        //this.onConfigurationChanged(this.resources.configuration)
        //finish()
        //overridePendingTransition(0,0)
        //startActivity(intent)
        //overridePendingTransition(0, 0)
        //changeLocal("en", "US")
        locale = "en"
        saveSettings(locale, theme)
        showToast()
    }

    fun onClickRu(view: View) {
        //this.resources.configuration.setLocale(Locale("ru", "RU"))
        //this.onConfigurationChanged(this.resources.configuration)
        //finish()
        //overridePendingTransition(0,0)
        //startActivity(intent)
        //overridePendingTransition(0, 0)
        //changeLocal("ru", "RU")
        locale = "ru"
        saveSettings(locale, theme)
        showToast()
    }

    fun onClickThemeLight(view: View) {
        //this.resources.configuration.uiMode = Configuration.UI_MODE_NIGHT_NO
        //this.onConfigurationChanged(this.resources.configuration)
        //finish()
        //overridePendingTransition(0,0)
        //startActivity(intent)
        //overridePendingTransition(0, 0)
        theme = AppCompatDelegate.MODE_NIGHT_NO
        saveSettings(locale, theme)
        showToast()
    }

    fun onClickThemeDark(view: View) {
        //this.resources.configuration.uiMode = Configuration.UI_MODE_NIGHT_YES
        //this.onConfigurationChanged(this.resources.configuration)
        //finish()
        //overridePendingTransition(0,0)
        //startActivity(intent)
        //overridePendingTransition(0, 0)
        theme = AppCompatDelegate.MODE_NIGHT_YES
        saveSettings(locale, theme)
        showToast()
    }

    private fun saveSettings(locale: String, theme: Int) {
        val pref = getSharedPreferences("pref", MODE_PRIVATE).edit(commit = true) {
            this.putString("locale", locale)
            this.putInt("theme", theme)
        }
    }

    private fun showToast() {
        Toast.makeText(this, R.string.settings_toast, Toast.LENGTH_LONG).show()
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        Log.d("TagZ", "Conf update" + "${newConfig.locales[0]}")

    }


}