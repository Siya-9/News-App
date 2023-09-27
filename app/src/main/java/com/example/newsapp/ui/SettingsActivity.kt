package com.example.newsapp.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.example.newsapp.R

class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fr_settings, SettingsFragment())
            .commit()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if(key == "country_code"){
            when(sharedPreferences.getString("country_code", "in")){
                "in" -> sharedPreferences.edit().putString("language", "en").apply()
                "us" -> sharedPreferences.edit().putString("language", "en").apply()
                "uk" -> sharedPreferences.edit().putString("language", "en").apply()
                "fr" -> sharedPreferences.edit().putString("language", "fr").apply()
                "cn" -> sharedPreferences.edit().putString("language", "zh").apply()
                "it" -> sharedPreferences.edit().putString("language", "it").apply()
                "de" -> sharedPreferences.edit().putString("language", "de").apply()
            }
        } else if(key == "language"){
            when(sharedPreferences.getString("language", "in")){
                "fr" -> sharedPreferences.edit().putString("country_code", "fr").apply()
                "zh" -> sharedPreferences.edit().putString("country_code", "cn").apply()
                "it" -> sharedPreferences.edit().putString("country_code", "it").apply()
                "de" -> sharedPreferences.edit().putString("country_code", "de").apply()
            }
        }
    }
}