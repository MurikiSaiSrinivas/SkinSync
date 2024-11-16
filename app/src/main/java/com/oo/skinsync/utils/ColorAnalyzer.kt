package com.oo.skinsync.utils


import android.graphics.Color
import kotlin.math.pow

class ColorAnalyzer {
    companion object {
        private const val K_MEANS_ITERATIONS = 10
        private const val K_MEANS_CLUSTERS = 3

        fun findDominantColor(pixels: List<Int>): Int {
            if (pixels.isEmpty()) return Color.BLACK

            val centroids = pixels.shuffled().take(K_MEANS_CLUSTERS).map { pixel ->
                floatArrayOf(
                    Color.red(pixel).toFloat(),
                    Color.green(pixel).toFloat(),
                    Color.blue(pixel).toFloat()
                )
            }.toMutableList()

            repeat(K_MEANS_ITERATIONS) {
                val clusters = List(K_MEANS_CLUSTERS) { mutableListOf<Int>() }

                for (pixel in pixels) {
                    val pixelColor = floatArrayOf(
                        Color.red(pixel).toFloat(),
                        Color.green(pixel).toFloat(),
                        Color.blue(pixel).toFloat()
                    )
                    val closestCentroidIndex = centroids.indices.minByOrNull { centroidIndex ->
                        val centroid = centroids[centroidIndex]
                        (centroid[0] - pixelColor[0]).pow(2) +
                                (centroid[1] - pixelColor[1]).pow(2) +
                                (centroid[2] - pixelColor[2]).pow(2)
                    } ?: 0

                    clusters[closestCentroidIndex].add(pixel)
                }

                for (i in centroids.indices) {
                    val cluster = clusters[i]
                    if (cluster.isNotEmpty()) {
                        val r = cluster.sumOf { Color.red(it).toDouble() } / cluster.size
                        val g = cluster.sumOf { Color.green(it).toDouble() } / cluster.size
                        val b = cluster.sumOf { Color.blue(it).toDouble() } / cluster.size
                        centroids[i] = floatArrayOf(r.toFloat(), g.toFloat(), b.toFloat())
                    }
                }
            }

            val dominant = centroids.maxByOrNull { centroid ->
                pixels.count {
                    Color.red(it).toFloat() == centroid[0] &&
                            Color.green(it).toFloat() == centroid[1] &&
                            Color.blue(it).toFloat() == centroid[2]
                }
            } ?: centroids.first()

            return Color.rgb(
                dominant[0].toInt(),
                dominant[1].toInt(),
                dominant[2].toInt()
            )
        }
    }
}