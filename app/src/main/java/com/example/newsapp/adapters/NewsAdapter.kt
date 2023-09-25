package com.example.newsapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.model.News


class NewsAdapter: RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {


    inner class NewsViewHolder( itemView : View) : RecyclerView.ViewHolder(itemView){
    }

    private val diffCallback = object : DiffUtil.ItemCallback<News>(){
        override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem == newItem
        }
    }

    interface TTSClickListener{
        fun onTTSClick(text: String, position: Int)
    }
    val differ = AsyncListDiffer(this, diffCallback)

    override fun getItemCount(): Int = differ.currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        return NewsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.news_item,
                parent,
                false
            ))
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        Log.d("Adapter", "on bind view holder set up")
        val article = differ.currentList[position]
        holder.itemView.apply {

            Glide.with(this).load(article.urlToImage).placeholder(R.drawable.placeholder_image).into(holder.itemView.findViewById(R.id.iv_image))
            holder.itemView.findViewById<TextView>(R.id.tv_title).text = article.title
            holder.itemView.findViewById<TextView>(R.id.tv_description).text = article.description
        }
    }

}