package com.example.newsapp.ui.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.NewsCategoryBinding
import com.example.newsapp.ui.MainActivity
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.util.Constants
import com.example.newsapp.util.Constants.Companion.QUERY_PAGE_SIZE
import com.example.newsapp.util.Resource
import com.example.newsapp.viewmodel.NewsViewModel


class CategoryFragment(val category: String) : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var viewModel: NewsViewModel
    private lateinit var binding : NewsCategoryBinding
    private lateinit var newsAdapter: NewsAdapter


    private var countryCode : String = "in"
    private var currentPage = 1

    var isLastPage = false
    var isLoading = false
    var isScrolling=false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NewsCategoryBinding.inflate(inflater, container, false)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).viewModel
        setUpRecyclerView()

        newsAdapter.setOnItemClickListener {
            if(it.url != null) {
                val description = it.description ?: "description"
                val title = it.title ?: "title"
                val content = it.content ?: "content"
                Log.d("LogBefore", description)
                Log.d("LogBefore", content)
                val intent = Intent(context, NewsActivity::class.java)
                intent.putExtra("url", it.url)
                    .putExtra("title", title)
                    .putExtra("description", description)
                    .putExtra("content", content)
                context?.startActivity(intent)
            }

        }

        viewModel.categoryNews.observe(viewLifecycleOwner, Observer { response ->
            when(response){
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let {
                            newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.categoryNewsPage == totalPages
                        isLoading = false
                        Log.d("Category Fragment", "Response loaded")
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {message ->
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }  )
    }

    private fun setUpRecyclerView(){
        getSettings()
        viewModel.initialiseCategory()
        isLastPage = false
        isLoading = false
        isScrolling=false
        viewModel.getCategoryNews(category, countryCode, currentPage)

        newsAdapter = NewsAdapter ()
        binding.rvCategory.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@CategoryFragment.scrollListener)
        }
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
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE

            val shouldPaginate =
                isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                currentPage++
                Log.d("Pagination", "Loading page $currentPage")
                viewModel.getCategoryNews(category,countryCode,currentPage)
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
    private fun getSettings() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val setCountryCode = sharedPreferences.getString("country_code", "in")
        countryCode = setCountryCode ?: "in"
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key == "country_code" || key == "language"){
           setUpRecyclerView()
        }
    }

    override fun onDestroy() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }


}