package com.oo.skinsync.models

data class SerpApiResponse(
    val shopping_results: List<ShoppingResult>
)

data class ShoppingResult(
    val title: String,
    val product_link: String,
    val source: String,
    val price: String,
    val thumbnail: String
)
