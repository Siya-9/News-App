package com.example.newsapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.model.NewsResponse
import com.example.newsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(
    private val newsRepository : NewsRepository,
) : ViewModel() {

    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    private var breakingNewsPage = 1
    //var breakingNewsResponse : NewsResponse? = null
    private val page = 1


    val categoryNews :  MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    init{
        getBreakingNews("in", "en")
       // Log.d("Auto Read", "$autoRead")
    }


    private fun handleBreakingNews(response : Response<NewsResponse>) : Resource<NewsResponse>{
        if(response.isSuccessful){
            response.body()?. let{
                resultResponse ->
//                breakingNewsPage+=1
//                if(breakingNewsResponse == null){
//                    breakingNewsResponse = resultResponse
//                }else{
//                    val oldArticles = breakingNewsResponse?.articles?.toMutableList()
//                    var newArticles = resultResponse.articles
//                    if (oldArticles != null) {
//                        oldArticles += newArticles
//                    }
//                }
           //     return Resource.Success(breakingNewsResponse ?: resultResponse)
                return Resource.Success( resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleCategoryNews(response : Response<NewsResponse>) : Resource<NewsResponse>{
        if(response.isSuccessful){
            response.body()?. let{
                    resultResponse ->
//                breakingNewsPage+=1
//                if(breakingNewsResponse == null){
//                    breakingNewsResponse = resultResponse
//                }else{
//                    val oldArticles = breakingNewsResponse?.articles?.toMutableList()
//                    var newArticles = resultResponse.articles
//                    if (oldArticles != null) {
//                        oldArticles += newArticles
//                    }
//                }
                //     return Resource.Success(breakingNewsResponse ?: resultResponse)
                return Resource.Success( resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
    private fun getBreakingNews(countryCode: String, language : String) = viewModelScope.launch {
        breakingNews.postValue(Resource.Loading())
        val response = newsRepository.getBreakingNews(countryCode, language,breakingNewsPage)
        breakingNews.postValue(handleBreakingNews(response))
    }

    fun getCategoryNews(category : String) = viewModelScope.launch {
        categoryNews.postValue(Resource.Loading())
        val response = newsRepository.getCategoryNews(category,"en", page)
        categoryNews.postValue(handleCategoryNews(response))
    }

}