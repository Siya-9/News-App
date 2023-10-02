package com.example.newsapp.ui.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.NewsSavedBinding
import com.example.newsapp.ui.MainActivity
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.util.Constants
import com.example.newsapp.viewmodel.NewsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SavedFragment : Fragment(R.layout.news_saved) {
    val savedAdapter=NewsAdapter()
    private lateinit var viewModel: NewsViewModel
    private lateinit var binding : NewsSavedBinding
    private var keyword : String= ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NewsSavedBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel
        binding.frSaved.lockMode = SlidingPaneLayout.LOCK_MODE_LOCKED
        binding.rvSavedNews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSavedNews.adapter = savedAdapter

        var job : Job? = null

        binding.etSearch.addTextChangedListener {editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(Constants.SEARCH_TIME_DELAY)
                keyword = editable.toString()
                setUpRecyclerView()
            }
        }

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        viewModel.getAllSavedNews(keyword).observe(viewLifecycleOwner) { savedNewsList ->
            savedAdapter.differ.submitList(savedNewsList)

            // Show/hide the empty message based on the list size
            if (savedNewsList.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
                binding.tvEmpty.visibility = View.GONE
            }

            savedAdapter.setOnItemClickListener {
                if(it.url != null) {
                    val description = it.description ?: "description"
                    val title = it.title ?: "title"
                    val content = it.content ?: "content"
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
                        val swipedItem = savedAdapter.getItemAtPosition(position)
                        // Call the deleteItem function in the adapter
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setTitle("Delete Item")
                        builder.setMessage("Do you want to delete this item?")
                        builder.setCancelable(false)
                        builder.setPositiveButton("Delete") { dialog, _ ->
                            lifecycleScope.launch {
                                viewModel.deleteSavedNews(swipedItem)
                            }
                            dialog.dismiss()
                        }
                        builder.setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                            savedAdapter.notifyItemChanged(position)
                        }
                        val dialog = builder.create()
                        dialog.show()
                    }
                }
            }
            val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
            itemTouchHelper.attachToRecyclerView(binding.rvSavedNews)
        }
    }
}


