package com.example.newsapp.viewmodel

import com.example.newsapp.api.RetrofitInstance
import com.example.newsapp.database.NewsDatabase

class NewsRepository(
    val db: NewsDatabase,
) {

    suspend fun getTrendingNews(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.getTrendingNews(countryCode, pageNumber)

    suspend fun getCategoryNews(category : String,countryCode: String,pageNumber: Int)=
        RetrofitInstance.api.getCategoryNews(category,countryCode, pageNumber)

    suspend fun getSearchNews(searchQuery : String, language : String,pageNumber: Int)=
        RetrofitInstance.api.getSearchNews(searchQuery,language, pageNumber)

}