package com.example.alfabet_01

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Switch
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

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
        val intent = Intent(this, ParkingHistoryActivity::class.java)
        if (!modelList.isNullOrEmpty()) intent.putExtra("qwerty", modelList)
        launcherHistory?.launch(intent)
    }

    fun onClickThemeSwitch(view: View) {
        val swith = findViewById<Switch>(R.id.switch1)
        if (swith.isActivated) swith.setText(R.string.settings_theme_switch_dark)
        else swith.setText(R.string.settings_theme_switch_light)
    }




}