package com.example.newsapp.ui.fragment

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
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.NewsSearchBinding
import com.example.newsapp.ui.MainActivity
import com.example.newsapp.util.Constants
import com.example.newsapp.util.Constants.Companion.SEARCH_TIME_DELAY
import com.example.newsapp.util.Resource
import com.example.newsapp.viewmodel.NewsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    private lateinit var viewModel: NewsViewModel
    private lateinit var binding : NewsSearchBinding
    private lateinit var newsAdapter: NewsAdapter

    private var language = "en"
    private var currentPage = 1
    private var keyword : String= ""
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
        getSettings()
        setUpRecyclerView()

        val navController = findNavController()
        newsAdapter.setOnItemClickListener {
            Log.d("Search Fragment", "Item clicked")
            val bundle = Bundle().apply {
                putSerializable("news", it)
            }
            navController.navigate(R.id.action_searchFragment_to_newsFragment, bundle)
            Log.d("Search Fragment", "Navigated")
        }
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
                    viewModel.getSearchNews(editable.toString(), language, currentPage)
                    keyword = editable.toString()
                }
            }
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

    }

    private fun getSettings() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val setLanguage = sharedPreferences.getString("language", "en")
        language = setLanguage ?: "en"
    }
    private fun setUpRecyclerView() {
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

    var isLastPage = false
    var isLoading = false
    var isScrolling=false

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
}