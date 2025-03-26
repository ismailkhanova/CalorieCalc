package com.example.caloriecalc.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import com.example.caloriecalc.data.ApiResponse


interface CaloriesNinjasApi{
    //Api key
    @Headers("X-Api-Key: tSBuIQMCHMs40zF1jdVZrQ==DGpBhoJVnXdF2DUe")
    @GET("v1/nutrition")
    suspend fun searchProducts(@Query("query") query: String): ApiResponse

    companion object {
        //метод для создания экземляра api
        fun create(): CaloriesNinjasApi {
            return Retrofit.Builder()
                .baseUrl("https://api.calorieninjas.com/")
                .addConverterFactory(GsonConverterFactory.create()) //конвертация json в kotlin
                .build()
                .create(CaloriesNinjasApi::class.java)
        }
    }
}