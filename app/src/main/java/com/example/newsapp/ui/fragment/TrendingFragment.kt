package com.example.newsapp.ui.fragment

import android.content.SharedPreferences
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
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.NewsTrendingBinding
import com.example.newsapp.ui.MainActivity
import com.example.newsapp.util.Resource
import com.example.newsapp.viewmodel.NewsViewModel
import java.util.Locale

class TrendingFragment : Fragment(), NewsAdapter.TTSClickListener{

    private lateinit var viewModel: NewsViewModel
    private lateinit var binding : NewsTrendingBinding
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var textToSpeech: TextToSpeech
    private val autoScrollHandler = Handler()

    private lateinit var  sharedPreferences : SharedPreferences
    //private lateinit var listener : OnSharedPreferenceChangeListener
    var isAutoReadEnabled : Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(requireActivity())
        //sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        isAutoReadEnabled = sharedPreferences.getBoolean("auto_read", false)

        textToSpeech = TextToSpeech(activity){
                status -> if(status == TextToSpeech.SUCCESS){
            textToSpeech.language = Locale.ENGLISH
            }
        }
        binding = NewsTrendingBinding.inflate(inflater, container, false)

//        listener = OnSharedPreferenceChangeListener{ sharedPreferences, key ->
//            if(key=="auto_read"){
//                isAutoReadEnabled = sharedPreferences.getBoolean("auto_read", false)
//                if(isAutoReadEnabled) {
//                    autoRead()
//                }else{
//                    stopAutoRead()
//                }
//            }
//        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).viewModel
        setUpRecyclerView()
        viewModel.breakingNews.observe(viewLifecycleOwner, Observer {
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
        binding.rvTrending.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            Log.d("Breaking News" ,  "recycler applied")
        }
    }

    private fun autoRead() {
        val layoutManager = binding.rvTrending.layoutManager as LinearLayoutManager
        var visibleItem = layoutManager.findFirstVisibleItemPosition()

        autoScrollHandler.postDelayed(object : Runnable {
            override fun run() {
                if(isAutoReadEnabled) {
                    binding.rvTrending.smoothScrollToPosition(visibleItem + 1)
                    visibleItem += 1
                    autoScrollHandler.postDelayed(this, 10000)
                }
            }
        }, 10000)

        binding.rvTrending.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                visibleItem +=1
                if(isAutoReadEnabled) {
                        val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                        val holder =
                            binding.rvTrending.findViewHolderForLayoutPosition(firstVisibleItem)
                        val itemToRead =
                            holder?.itemView?.findViewById<TextView>(R.id.tv_title)?.text.toString()
                        onTTSClick(itemToRead, firstVisibleItem)
                    }
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

    private fun stopAutoRead(){
        if(textToSpeech.isSpeaking){
            textToSpeech.stop()
        }
        textToSpeech.shutdown()
        autoScrollHandler.removeCallbacksAndMessages(null)
    }
    override fun onDestroy() {
        stopAutoRead()
        super.onDestroy()
    }

//    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
//        if(key=="auto_read"){
//            val prefs = sharedPreferences?.getBoolean("auto_read", false)
//            isAutoReadEnabled = prefs!!
//            Log.e("AR", "$isAutoReadEnabled")
//        }
//    }
}


