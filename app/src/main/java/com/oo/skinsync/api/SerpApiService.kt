package com.oo.skinsync.api

import com.oo.skinsync.models.SerpApiResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SerpApiService {
    @GET("search")
    fun getShoppingResults(
        @Query("api_key") apiKey: String,
        @Query("engine") engine: String = "google",
        @Query("q") query: String,
        @Query("location") location: String = "United States",
        @Query("google_domain") googleDomain: String = "google.com",
        @Query("gl") gl: String = "us",
        @Query("hl") hl: String = "en",
        @Query("tbm") tbm: String = "shop",
        @Query("device") device: String = "desktop"
    ): Call<SerpApiResponse>
}
