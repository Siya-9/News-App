package com.example.newsapp.ui

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.newsapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class NewsActivity : AppCompatActivity() , Read{

    private var languagePreference = "en"
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_fragment)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        languagePreference = sharedPreferences.getString("language", "en").toString()

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d("News", "Success")
                when(languagePreference){
                    "fr" -> textToSpeech.language = Locale.FRENCH
                    "it" -> textToSpeech.language = Locale.ITALIAN
                    "de" -> textToSpeech.language = Locale.GERMAN
                    "zh" -> textToSpeech.language = Locale.CHINESE
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

        var text : String = " "
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener() {
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

    override fun onTTSClick(text: String) {
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

interface Read {
    fun onTTSClick(text: String)
}