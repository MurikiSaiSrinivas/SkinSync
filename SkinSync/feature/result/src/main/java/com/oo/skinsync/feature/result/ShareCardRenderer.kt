package com.oo.skinsync.feature.result

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.FileProvider
import com.oo.skinsync.domain.Suggestion
import java.io.File

/**
 * Draws the palette to a PNG in app cache and opens the system share sheet.
 * The caption text comes from the pure, tested [ShareTextBuilder].
 */
object ShareCardRenderer {

    fun share(context: Context, location: String, suggestion: Suggestion) {
        val bitmap = render(suggestion)
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "skinsync.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, ShareTextBuilder.build(location, suggestion))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share your palette"))
    }

    private fun render(suggestion: Suggestion): Bitmap {
        val w = 1080
        val rowH = 180
        val top = 220
        val h = top + suggestion.palette.size * rowH + 80
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.parseColor("#FFF7FA"))

        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A23BA0"); textSize = 64f; isFakeBoldText = true
        }
        val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4E444B"); textSize = 40f
        }
        val name = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1F1A1D"); textSize = 44f
        }

        c.drawText("SkinSync", 60f, 100f, title)
        c.drawText("Your type: ${suggestion.seasonalType}", 60f, 170f, sub)

        suggestion.palette.forEachIndexed { i, rec ->
            val y = top + i * rowH
            val swatch = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = runCatching { Color.parseColor(rec.hexColor) }.getOrDefault(Color.GRAY)
            }
            c.drawCircle(120f, y + 60f, 60f, swatch)
            c.drawText(rec.name, 220f, y + 55f, name)
            c.drawText(rec.reason.take(48), 220f, y + 105f, sub)
        }
        return bmp
    }
}
