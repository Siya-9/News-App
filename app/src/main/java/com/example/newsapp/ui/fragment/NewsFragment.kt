package com.example.newsapp.ui.fragment

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.NewsFragmentBinding
import com.example.newsapp.model.News
import com.example.newsapp.ui.MainActivity
import com.example.newsapp.viewmodel.NewsViewModel
import java.util.Locale

class NewsFragment(val news: News): Fragment(R.layout.news_fragment) , NewsAdapter.TTSClickListener{
    private lateinit var viewModel: NewsViewModel
    private lateinit var binding: NewsFragmentBinding
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NewsFragmentBinding.inflate(inflater, container, false)

        textToSpeech = TextToSpeech(activity) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.ENGLISH
            }
        }

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).viewModel
        binding.wvNews.apply {
            webViewClient = WebViewClient()
            loadUrl(news.url)
        }
        binding.fab.setOnClickListener {
            Log.d("News Activity", "call read")
            onTTSClick(news.title)
            onTTSClick(news.description)
        }
    }

    override fun onTTSClick(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
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