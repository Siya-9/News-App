package com.example.newsapp.adapters

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
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
        Log.d("Adapter", "on create view holder set up")
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
            Log.d("Adapter", "setting values")
            Glide.with(this).load(article.urlToImage).into(holder.itemView.findViewById(R.id.iv_image))
            holder.itemView.findViewById<TextView>(R.id.tv_title).text = article.title
            holder.itemView.findViewById<TextView>(R.id.tv_description).text = article.description
            Log.d("Adapter", "set values")

            setOnClickListener{
                    v: View ->
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(article.url)
                ContextCompat.startActivity(context, intent, null)
            }

        }
    }

    private var onItemClickListener : ((News) -> Unit)?= null

//    fun setOnItemClickListener (listener : (News) -> Unit){
//        onItemClickListener = listener
//    }
}