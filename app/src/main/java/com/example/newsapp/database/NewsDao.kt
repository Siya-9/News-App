package com.example.newsapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.newsapp.model.News

@Dao
interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(news: News): Long

    @Query("SELECT * from news")
    fun getAllArticles() : LiveData<List<News>>

    @Delete
    suspend fun deleteArticle(news: News)

    @Query("SELECT * from news where title like '%'||:keyword||'%' ")
    fun searchArticles(keyword:String) :  LiveData<List<News>>
}