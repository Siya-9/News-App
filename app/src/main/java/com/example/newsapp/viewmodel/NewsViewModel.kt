package com.example.newsapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.newsapp.model.NewsResponse
import com.example.newsapp.util.Constants.Companion.QUERY_PAGE_SIZE
import com.example.newsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    private val application : Application,
    private val newsRepository : NewsRepository,
) : AndroidViewModel(application) {

    var categoryNewsPage = 1
    val categoryNews :  MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    private var categoryNewsResponse : NewsResponse? = null

    private var searchNewsPage = 1
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
        handleCategoryNews(category,countryCode,pageNumber)
    }

    fun getSearchNews(searchQuery : String, language: String, pageNumber: Int) = viewModelScope.launch {
        handleSearchNews(searchQuery,language,pageNumber)
    }

    private suspend fun handleSearchNews(searchQuery: String, language: String, pageNumber: Int){
        searchNews.postValue(Resource.Loading())
        try{
            if(isInternetConnected()){
                val response = newsRepository.getSearchNews(searchQuery,language, pageNumber)
                searchNews.postValue(paginateSearchNews(response))
            }else{
                    searchNews.postValue(Resource.Error("No internet connection"))
            }
        }catch (t : Throwable){
            when(t) {
                is IOException ->  searchNews.postValue(Resource.Error("Network failed"))
                else ->  searchNews.postValue(Resource.Error("Error loading news"))
            }
        }
    }

    private suspend fun handleCategoryNews(category : String,countryCode: String, pageNumber: Int){
        categoryNews.postValue(Resource.Loading())
        Log.d("Internet","${isInternetConnected()}")
        try{
            if(isInternetConnected()){
                if(category == "trending"){
                    val response = newsRepository.getTrendingNews(countryCode,pageNumber)
                    categoryNews.postValue(paginateCategoryNews(response))
                }else {
                    val response = newsRepository.getCategoryNews(category, countryCode, pageNumber)
                    categoryNews.postValue(paginateCategoryNews(response))
                }
            }else{
                categoryNews.postValue(Resource.Error("No internet connection"))
            }
        }catch (t : Throwable){
            when(t) {
                is IOException ->  categoryNews.postValue(Resource.Error("Network failed"))
                else ->  categoryNews.postValue(Resource.Error("Error loading news"))
            }
        }
    }

    fun initialiseCategory() {
        categoryNewsResponse = null
        categoryNewsPage = 1
    }

    fun initialiseSearch() {
        searchNewsResponse = null
        searchNewsPage = 1
    }

    private fun isInternetConnected(): Boolean{
        val context = getApplication<Application>().applicationContext
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetwork?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)?: return false
        return when{
            capabilities.hasTransport(TRANSPORT_WIFI) -> true
            capabilities.hasTransport(TRANSPORT_CELLULAR)-> true
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}