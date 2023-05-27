package com.example.alfabet_01

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

import java.util.Locale


class SettingsActivity : AppCompatActivity() {
    private var modelList :  ArrayList<Model>? = null
    private var launcherHistory : ActivityResultLauncher<Intent>? = null

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
            }
        }
        Log.d("Tag", "Settings onCreate")
    }



    override fun onDestroy() {
        super.onDestroy()
        Log.d("Tag", "Settings destroyed")
    }

    fun onClickBack(view: View) {
        val i = Intent()
        i.putExtra("modelFromSet", modelList)
        setResult(RESULT_OK, i)
        finish()
    }

    fun onClickHistory(view: View) {
        val dsd = ParkingHistoryActivity::class.java
        val intent = Intent(this, ParkingHistoryActivity::class.java)
        if (!modelList.isNullOrEmpty()) intent.putExtra("qwerty", modelList)
        launcherHistory?.launch(intent)
    }


    fun onClickEn(view: View) {
        this.resources.configuration.setLocale(Locale("en", "US"))
        this.onConfigurationChanged(this.resources.configuration)
    }

    fun onClickRu(view: View) {
        this.resources.configuration.setLocale(Locale("ru", "RU"))
        this.onConfigurationChanged(this.resources.configuration)
        //this.resources.getQuantityString()
        findViewById<TextView>(R.id.textView20).setText(R.string.language)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d("TagZ", "Conf update")

        findViewById<TextView>(R.id.textView20).text = resources.getString(R.string.language)


    }


}