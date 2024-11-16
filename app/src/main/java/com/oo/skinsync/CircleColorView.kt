package com.oo.skinsync
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CircleColorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var paint = Paint().apply {
        style = Paint.Style.FILL
    }

    var color: Int = Color.WHITE
        set(value) {
            field = value
            paint.color = value
            invalidate()  // Redraw view with new color
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = Math.min(width, height) / 2f
        canvas.drawCircle(width / 2f, height / 2f, radius, paint)
    }
}
