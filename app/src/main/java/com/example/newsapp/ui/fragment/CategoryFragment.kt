package com.example.newsapp.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.NewsCategoryBinding
import com.example.newsapp.ui.MainActivity
import com.example.newsapp.util.Resource
import com.example.newsapp.viewmodel.NewsViewModel
import java.util.Locale


class CategoryFragment(private var category: String) : Fragment() , NewsAdapter.TTSClickListener {


    private lateinit var viewModel: NewsViewModel
    private lateinit var binding : NewsCategoryBinding
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var textToSpeech: TextToSpeech
    private val autoScrollHandler = Handler()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NewsCategoryBinding.inflate(inflater, container, false)
        textToSpeech = TextToSpeech(activity){
                status -> if(status == TextToSpeech.SUCCESS){
            textToSpeech.language = Locale.ENGLISH
            }
        }
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).viewModel
        viewModel.getCategoryNews(category)
        setUpRecyclerView()

        viewModel.categoryNews.observe(viewLifecycleOwner, Observer {
                response -> when(response){
            is Resource.Success -> {
                hideProgressBar()
                response.data?.let {
                        newsResponse ->  newsAdapter.differ.submitList(newsResponse.articles)
                    Log.d("TAG", "Response loaded")
                }
            }

            is Resource.Error -> {
                hideProgressBar()
                response.message?.let {message ->
                    Log.e("TAG", "Error loading response: $message")
                }
            }
            is Resource.Loading -> {
                showProgressBar()
            }
        }
        }  )
    }

    private fun setUpRecyclerView(){
        newsAdapter = NewsAdapter()
        binding.rvSports.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            Log.d("Sports News" ,  "recycler applied")
        }
        autoRead()
    }

    private fun autoRead() {
        val layoutManager = binding.rvSports.layoutManager as LinearLayoutManager
        var firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
        autoScrollHandler.postDelayed(object : Runnable{
            override fun run() {
                Log.d("Scrollable", "Scrolling ....")

                Log.d("Scrollable", "$firstVisibleItem")
                binding.rvSports.smoothScrollToPosition(firstVisibleItem + 1)
                firstVisibleItem +=1
                autoScrollHandler.postDelayed(this, 10000)
            }
        }, 10000)

        binding.rvSports.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                val holder = binding.rvSports.findViewHolderForLayoutPosition(firstVisibleItem)
                val itemToRead = holder?.itemView?.findViewById<TextView>(R.id.tv_title)?.text.toString()
                onTTSClick(itemToRead, firstVisibleItem)
            }
        })
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun onTTSClick(text: String, position: Int) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onPause() {
        super.onPause()
        if(textToSpeech.isSpeaking){
            textToSpeech.stop()
        }
        autoScrollHandler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        if(!textToSpeech.isSpeaking){
            autoRead()
        }
        super.onResume()
    }

    override fun onDestroy() {
        if(textToSpeech.isSpeaking){
            textToSpeech.stop()
        }
        textToSpeech.shutdown()
        autoScrollHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

}