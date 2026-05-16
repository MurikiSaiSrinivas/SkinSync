package com.oo.skinsync.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepLinkBuilderTest {

    private val builder = DeepLinkBuilder()

    @Test
    fun `builds four retailer links`() {
        val links = builder.linksFor("red gown")
        assertEquals(4, links.size)
        assertEquals(setOf("Amazon", "Myntra", "Flipkart", "Google Shopping"), links.map { it.retailer }.toSet())
    }

    @Test
    fun `query is url-encoded`() {
        val url = builder.linksFor("dusty rose dress").first { it.retailer == "Amazon" }.url
        assertTrue(url, url.contains("dusty+rose+dress") || url.contains("dusty%20rose%20dress"))
        assertTrue(url.startsWith("https://"))
    }

    @Test
    fun `trims surrounding whitespace`() {
        val url = builder.linksFor("  blue  ").first().url
        assertTrue(url.endsWith("blue"))
    }
}
