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
        style = Paint.Style.FILL // To fill the circle with color
    }

    private var borderPaint = Paint().apply {
        style = Paint.Style.STROKE // To draw a border
        strokeWidth = 5f // Set the border width
        color = Color.parseColor("#f584f5")
    }

    var color: Int = Color.WHITE
        set(value) {
            field = value
            paint.color = value
            invalidate()  // Redraw view with new color
        }

    var isActive: Boolean = false
        set(value) {
            field = value
            invalidate()  // Redraw view to show or hide the border
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val radius = Math.min(width, height) / 2f

        // Draw the circle with the selected color
        canvas.drawCircle(width / 2f, height / 2f, radius, paint)

        // If the view is active, draw the border around the circle
        if (isActive) {
            canvas.drawCircle(width / 2f, height / 2f, radius-3f, borderPaint)
        }
    }
}
