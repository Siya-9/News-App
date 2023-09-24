package com.example.newsapp.viewmodel

import com.example.newsapp.api.RetrofitInstance
import com.example.newsapp.database.NewsDatabase

class NewsRepository(
    val db: NewsDatabase,
) {


    suspend fun getBreakingNews(countryCode: String, language : String, pageNumber: Int) =
        RetrofitInstance.api.getBreakingNews(countryCode,language, pageNumber)

    suspend fun getCategoryNews(category : String, language : String,pageNumber: Int)=
        RetrofitInstance.api.getCategoryNews(category,language, pageNumber)

}