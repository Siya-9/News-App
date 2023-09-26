package com.example.newsapp.ui

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NewsActivity(): AppCompatActivity() {
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_fragment)

        val bundle = intent.extras?.getBundle("news")

        findViewById<WebView>(R.id.wv_news).apply {

        }
        findViewById<FloatingActionButton>(R.id.fab)
    }

}