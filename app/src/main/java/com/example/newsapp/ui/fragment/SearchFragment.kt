package com.example.newsapp.ui.fragment

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.NewsSearchBinding
import com.example.newsapp.model.News
import com.example.newsapp.ui.MainActivity
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.util.Constants
import com.example.newsapp.util.Constants.Companion.SEARCH_TIME_DELAY
import com.example.newsapp.util.Resource
import com.example.newsapp.viewmodel.NewsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment(private val searchText: String?) : Fragment(),  SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var viewModel: NewsViewModel
    private lateinit var binding : NewsSearchBinding
    private lateinit var newsAdapter: NewsAdapter

    private var language = "en"
    private var currentPage = 1
    private var keyword : String= ""

    var isLastPage = false
    var isLoading = false
    var isScrolling=false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NewsSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).viewModel

        setUpRecyclerView()


        // coroutine to delay search request on api
        var job : Job? = null

        binding.etSearch.addTextChangedListener {editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_TIME_DELAY)
                if(editable.toString().isNotEmpty()){
                    if(editable.toString() != keyword){
                        viewModel.initialiseSearch()
                    }
                    keyword = editable.toString()
                    viewModel.getSearchNews(keyword, language, currentPage)
                }
            }
        }
        if(!searchText.isNullOrBlank()){
            binding.etSearch.setText(searchText)
        }

        newsAdapter.setOnItemClickListener {
            val intent = Intent(context, NewsActivity::class.java)
            intent.putExtra("url", it.url)
                .putExtra("title", it.title)
                .putExtra("description", it.description)
                .putExtra("content", it.content)
            context?.startActivity(intent)
        }

        viewModel.searchNews.observe(viewLifecycleOwner, Observer {
                response -> when(response){
            is Resource.Success -> {
                hideProgressBar()
                response.data?.let {
                        newsResponse ->  newsAdapter.differ.submitList(newsResponse.articles.toList())
                    val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                    isLastPage = viewModel.categoryNewsPage == totalPages
                    isLoading = false
                    Log.d("Search Fragment", "Response loaded")
                }
            }

            is Resource.Error -> {
                hideProgressBar()
                response.message?.let {message ->
                    Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                }
            }
            is Resource.Loading -> {
                showProgressBar()
            }
        }
        }  )
        // Define your ItemTouchHelperCallback
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.RIGHT||direction== ItemTouchHelper.LEFT) {
                    val pos=viewHolder.adapterPosition
                    val swipedNews=newsAdapter.getItemAtPosition(pos)
                    showSaveItemDialog(swipedNews,pos)
                }
            }
            private fun showSaveItemDialog(swipedNews: News, position: Int) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Save Item")
                builder.setMessage("Do you want to save this item?")
                builder.setCancelable(false)
                builder.setPositiveButton("Save") { dialog, _ ->
                    lifecycleScope.launch {
                        viewModel.insertSavedNews(swipedNews)
                    }
                    dialog.dismiss()
                    newsAdapter.notifyItemChanged(position)
                }
                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    newsAdapter.notifyItemChanged(position)
                }
                val dialog = builder.create()
                dialog.show()
            }
        }
        // Create an ItemTouchHelper instance
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        // Attach the ItemTouchHelper to the RecyclerView
        itemTouchHelper.attachToRecyclerView(binding.rvSearchNews)

    }

    private fun getSettings() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val setLanguage = sharedPreferences.getString("language", "en")
        language = setLanguage ?: "en"
    }
    private fun setUpRecyclerView() {
        getSettings()
        viewModel.initialiseSearch()
        isLastPage = false
        isLoading = false
        isScrolling=false

        newsAdapter = NewsAdapter()
        binding.rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            Log.d("Search News", "recycler applied")
        }

    }
    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }



    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE

            val shouldPaginate =
                isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                currentPage++
                Log.d("Pagination", "Loading page $currentPage")
                viewModel.getSearchNews(keyword, language,currentPage)
                isScrolling=false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling=true
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key == "country_code" || key == "language"){
            setUpRecyclerView()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("keyword", keyword)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            keyword = savedInstanceState.getString("keyword").toString()
        }
    }
}