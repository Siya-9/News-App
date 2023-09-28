package com.example.newsapp.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.NewsSavedBinding
import com.example.newsapp.databinding.NewsSearchBinding
import com.example.newsapp.ui.MainActivity
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.util.Constants
import com.example.newsapp.viewmodel.NewsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchSavedFragment  : Fragment(R.layout.news_search) {
    private lateinit var searchSavedAdapter: NewsAdapter
    private lateinit var viewModel: NewsViewModel
    private lateinit var binding : NewsSearchBinding
    private var keyword : String= ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NewsSearchBinding.inflate(inflater, container, false)
        searchSavedAdapter = NewsAdapter()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel

        binding.rvSearchNews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchNews.adapter = searchSavedAdapter

        var job : Job? = null

        binding.etSearch.addTextChangedListener {editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(Constants.SEARCH_TIME_DELAY)
                if(editable.toString().isNotEmpty()){
                    keyword = editable.toString()
                    Toast.makeText(requireContext(), keyword, Toast.LENGTH_SHORT).show()
                    searchSavedAdapter = NewsAdapter()
                    viewModel.searchSavedNews(keyword)
                    binding.rvSearchNews.apply {
                        adapter = searchSavedAdapter
                        layoutManager = LinearLayoutManager(activity)
                    }
                    Log.d("search", viewModel.searchSavedNews(keyword).toString())
                }
            }
        }

        viewModel.searchSavedNews(keyword).observe(viewLifecycleOwner) { searchSavedNewsList ->
            searchSavedAdapter.differ.submitList(searchSavedNewsList)

            // Show/hide the empty message based on the list size
//            if (savedNewsList.isEmpty()) {
//                binding.Empty.visibility = View.VISIBLE
//            } else {
//                binding.Empty.visibility = View.GONE
//            }

            searchSavedAdapter.setOnItemClickListener {
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


            //This is for deleting the news item on swiping left in Saved News Fragment
            val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    if (direction == ItemTouchHelper.LEFT) {
                        // Get the position of the swiped item
                        val position = viewHolder.adapterPosition
                        val swipedItem = searchSavedAdapter.getItemAtPosition(position)
                        // Call the deleteItem function in the adapter
                        lifecycleScope.launch {
                            viewModel.deleteSavedNews(swipedItem)
                        }
                    }
                }
            }
            val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
            itemTouchHelper.attachToRecyclerView(binding.rvSearchNews)
        }
    }
}