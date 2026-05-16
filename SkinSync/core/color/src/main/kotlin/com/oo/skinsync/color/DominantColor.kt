package com.oo.skinsync.color

import kotlin.random.Random

/**
 * Finds the dominant color of a set of ARGB pixels using K-means in OkLab
 * space. Deterministic given a seed (so it is unit-testable). Pure Kotlin —
 * the Android layer supplies the pixel ints.
 */
object DominantColor {

    private const val DEFAULT_K = 3
    private const val DEFAULT_ITERATIONS = 12

    /**
     * @param pixels ARGB ints. Fully/near transparent pixels are ignored.
     * @return the largest cluster's centroid as an opaque ARGB int, or null
     *         if there are no usable pixels.
     */
    fun of(
        pixels: IntArray,
        k: Int = DEFAULT_K,
        iterations: Int = DEFAULT_ITERATIONS,
        seed: Long = 42L,
    ): Int? {
        val opaque = pixels.filter { OkLab.a(it) >= 200 }
        if (opaque.isEmpty()) return null
        if (opaque.size <= k) return opaque.first() or (0xFF shl 24)

        val labs = opaque.map { OkLab.toLab(it) }
        val rng = Random(seed)
        var centroids = labs.shuffled(rng).take(k).toMutableList()
        val assignment = IntArray(labs.size)

        repeat(iterations) {
            // assign
            for (i in labs.indices) {
                var best = 0
                var bestD = Double.MAX_VALUE
                for (c in centroids.indices) {
                    val d = OkLab.distance2(labs[i], centroids[c])
                    if (d < bestD) { bestD = d; best = c }
                }
                assignment[i] = best
            }
            // recompute
            val sumL = DoubleArray(k)
            val sumA = DoubleArray(k)
            val sumB = DoubleArray(k)
            val count = IntArray(k)
            for (i in labs.indices) {
                val c = assignment[i]
                sumL[c] += labs[i].l; sumA[c] += labs[i].a; sumB[c] += labs[i].b; count[c]++
            }
            centroids = MutableList(k) { c ->
                if (count[c] == 0) labs[rng.nextInt(labs.size)]
                else OkLab.Lab(sumL[c] / count[c], sumA[c] / count[c], sumB[c] / count[c])
            }
        }

        val counts = IntArray(k)
        for (a in assignment) counts[a]++
        val largest = counts.indices.maxByOrNull { counts[it] } ?: 0
        return OkLab.toArgb(centroids[largest])
    }
}
