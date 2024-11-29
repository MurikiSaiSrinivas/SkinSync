package com.oo.skinsync.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface AmazonProductApi {
    @GET("/onca/xml")
    fun getProducts(@QueryMap params: Map<String, String>): Call<ResponseBody>
}