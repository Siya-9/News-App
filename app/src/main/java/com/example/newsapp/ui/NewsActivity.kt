package com.example.newsapp.ui

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import com.example.newsapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class NewsActivity : AppCompatActivity(){

    private var countryCode = "en"
    private lateinit var textToSpeech: TextToSpeech


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_fragment)


        val toolbar = findViewById<Toolbar>(R.id.tb_news)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        countryCode = sharedPreferences.getString("country_code", "in").toString()

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d("News", "Success")
                when(countryCode){
                    "fr" -> textToSpeech.language = Locale.FRENCH
                    "it" -> textToSpeech.language = Locale.ITALIAN
                    "de" -> textToSpeech.language = Locale.GERMAN
                    "cn" -> textToSpeech.language = Locale.CHINESE
                    else -> textToSpeech.language = Locale.ENGLISH
                }
            }
        }


        val url = intent.getStringExtra("url")
        findViewById<WebView>(R.id.wv_news).apply {
            webViewClient = WebViewClient()
            if (url != null) {
                loadUrl(url)
            }
        }

        var text = " "
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            val title = intent.getStringExtra("title")
            if (title != null) {
                text += title
                Log.d("Log", "$title")
            }
            val description = intent.getStringExtra("description")
            if(description != null) {
                text += description
                Log.d("Log", "$description")
            }
            val content = intent.getStringExtra("content")
            if(content != null) {
                text += content
                Log.d("Log", "$content")
            }
            Log.d("Log", text)
            onTTSClick(text)
        }
    }

    private fun onTTSClick(text: String) {
        Log.d("Log", "Reading")
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        Log.d("Log", "Read")
    }

    override fun onPause() {
        super.onPause()
        if(textToSpeech.isSpeaking){
            textToSpeech.stop()
        }
    }

    override fun onDestroy() {
        if(textToSpeech.isSpeaking){
            textToSpeech.stop()
        }
        textToSpeech.shutdown()
        super.onDestroy()
    }


}
