package com.barprepng.app.ui.insights

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.barprepng.app.data.WeekStat
import kotlin.math.ceil

class HeatmapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var weekStats: List<WeekStat> = emptyList()
    private val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F0F4FF")
        textSize = 26f
        textAlign = Paint.Align.CENTER
    }
    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8FA8C8")
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }
    private val cellRect = RectF()
    private val cols = 4
    private val cellSpacing = 10f

    fun setData(stats: List<WeekStat>) {
        weekStats = stats
        val rows = ceil(stats.size.toFloat() / cols).toInt()
        val cellH = 72f
        minimumHeight = (rows * (cellH + cellSpacing) + cellSpacing).toInt()
        requestLayout()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (weekStats.isEmpty()) return

        val totalW = width.toFloat()
        val cellW = (totalW - cellSpacing * (cols + 1)) / cols
        val cellH = 72f

        weekStats.forEachIndexed { i, stat ->
            val col = i % cols
            val row = i / cols
            val x = cellSpacing + col * (cellW + cellSpacing)
            val y = cellSpacing + row * (cellH + cellSpacing)

            val pct = stat.accuracyPercent / 100f
            val color = interpolateColor(pct)
            cellPaint.color = color
            cellRect.set(x, y, x + cellW, y + cellH)
            canvas.drawRoundRect(cellRect, 10f, 10f, cellPaint)

            val cx = x + cellW / 2f
            val cy = y + cellH / 2f
            canvas.drawText("W${stat.weekNumber}", cx, cy - 4f, textPaint)
            canvas.drawText("${stat.accuracyPercent.toInt()}%", cx, cy + 22f, subPaint)
        }
    }

    private fun interpolateColor(fraction: Float): Int {
        val low = Color.parseColor("#3D0010")   // dark red
        val mid = Color.parseColor("#1A1400")   // dark gold
        val high = Color.parseColor("#002010")  // dark green
        return if (fraction < 0.5f) {
            val f = fraction * 2f
            blendColors(low, mid, f)
        } else {
            val f = (fraction - 0.5f) * 2f
            blendColors(mid, high, f)
        }
    }

    private fun blendColors(from: Int, to: Int, fraction: Float): Int {
        val r = (Color.red(from) + (Color.red(to) - Color.red(from)) * fraction).toInt()
        val g = (Color.green(from) + (Color.green(to) - Color.green(from)) * fraction).toInt()
        val b = (Color.blue(from) + (Color.blue(to) - Color.blue(from)) * fraction).toInt()
        return Color.rgb(r, g, b)
    }
}
