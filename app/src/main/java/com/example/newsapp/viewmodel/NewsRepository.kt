package com.example.newsapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.newsapp.api.RetrofitInstance
import com.example.newsapp.database.NewsDao
import com.example.newsapp.database.NewsDatabase
import com.example.newsapp.model.News

class NewsRepository(
    val db: NewsDatabase, private val newsDao: NewsDao
) {

    suspend fun getTrendingNews(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.getTrendingNews(countryCode, pageNumber)

    suspend fun getCategoryNews(category : String,countryCode: String,pageNumber: Int)=
        RetrofitInstance.api.getCategoryNews(category,countryCode, pageNumber)

    suspend fun getSearchNews(searchQuery : String, language : String,pageNumber: Int)=
        RetrofitInstance.api.getSearchNews(searchQuery,language, pageNumber)

    val allNews:LiveData<List<News>> =newsDao.getAllArticles()

    suspend fun insert(news:News){
        newsDao.upsert(news)
    }

    suspend fun delete(news:News){
        newsDao.deleteArticle(news)
    }

    fun search(keyword : String) :LiveData<List<News>> {
        Log.d("search", "repo")
        return newsDao.searchArticles(keyword)
    }
}