package com.oo.skinsync.data

import com.oo.skinsync.domain.ShoppingLink
import com.oo.skinsync.domain.ShoppingLinkRepository
import java.net.URLEncoder
import javax.inject.Inject

/**
 * Free replacement for the old paid SerpApi: turns an outfit query into
 * retailer search URLs the UI opens externally. Pure + unit-testable.
 */
class DeepLinkBuilder @Inject constructor() : ShoppingLinkRepository {

    override fun linksFor(query: String): List<ShoppingLink> {
        val q = URLEncoder.encode(query.trim(), "UTF-8")
        return listOf(
            ShoppingLink("Amazon", query, "https://www.amazon.in/s?k=$q"),
            ShoppingLink("Myntra", query, "https://www.myntra.com/$q"),
            ShoppingLink("Flipkart", query, "https://www.flipkart.com/search?q=$q"),
            ShoppingLink("Google Shopping", query, "https://www.google.com/search?tbm=shop&q=$q"),
        )
    }
}
