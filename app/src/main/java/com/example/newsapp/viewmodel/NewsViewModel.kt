package com.example.newsapp.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.model.NewsResponse
import com.example.newsapp.util.Constants.Companion.QUERY_PAGE_SIZE
import com.example.newsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(
    private val newsRepository : NewsRepository,
) : ViewModel() {


    val trendingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var trendingNewsPage = 1
    var trendingNewsResponse : NewsResponse? = null


    var categoryNewsPage = 1
    val categoryNews :  MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var categoryNewsResponse : NewsResponse? = null

    var searchNewsPage = 1
    val searchNews :  MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    private var searchNewsResponse : NewsResponse? = null

    private fun paginateCategoryNews(response : Response<NewsResponse>) : Resource<NewsResponse>{
        if(response.isSuccessful){
            response.body()?. let{
                    resultResponse ->
                val totalResults=resultResponse.totalResults
                val totalPages=totalResults/ QUERY_PAGE_SIZE+1
                Log.d("API_RESPONSE","Total Results:$totalResults,Total Pages: $totalPages")
                categoryNewsPage++
                if(categoryNewsResponse==null){
                    categoryNewsResponse=resultResponse
                }else{
                    val oldArticles=categoryNewsResponse?.articles
                    val newArticles=resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(categoryNewsResponse?:resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun paginateSearchNews(response : Response<NewsResponse>) : Resource<NewsResponse>{
        if(response.isSuccessful){
            response.body()?. let{
                    resultResponse ->
                val totalResults=resultResponse.totalResults
                val totalPages=totalResults/ QUERY_PAGE_SIZE+1
                Log.d("API_RESPONSE","Total Results:$totalResults,Total Pages: $totalPages")
                searchNewsPage++
                if(searchNewsResponse==null){
                    searchNewsResponse=resultResponse
                }else{
                    val oldArticles=searchNewsResponse?.articles
                    val newArticles=resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse?:resultResponse)
            }
        }
        return Resource.Error(response.message())
    }


    fun getCategoryNews(category : String,countryCode: String, pageNumber: Int) = viewModelScope.launch {
        categoryNews.postValue(Resource.Loading())
        if(category == "trending"){
            val response = newsRepository.getTrendingNews(countryCode,pageNumber)
            categoryNews.postValue(paginateCategoryNews(response))
        }else {
            val response = newsRepository.getCategoryNews(category, countryCode, pageNumber)
            categoryNews.postValue(paginateCategoryNews(response))
        }
    }

    fun getSearchNews(searchQuery : String, language: String, pageNumber: Int) = viewModelScope.launch {
        searchNews.postValue(Resource.Loading())
        val response = newsRepository.getSearchNews(searchQuery,language, pageNumber)
        searchNews.postValue(paginateSearchNews(response))
    }

    fun initialiseCategory() {
        categoryNewsResponse = null
        categoryNewsPage = 1
    }

    fun initialiseSearch() {
        searchNewsResponse = null
        searchNewsPage = 1
    }
}