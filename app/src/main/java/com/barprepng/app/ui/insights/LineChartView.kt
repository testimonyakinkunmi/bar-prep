package com.barprepng.app.ui.insights

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.barprepng.app.data.ScorePoint
import kotlin.math.max

class LineChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var points: List<ScorePoint> = emptyList()

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F0A500")
        strokeWidth = 3f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F0A500")
        style = Paint.Style.FILL
    }

    private val dotBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0A0F1E")
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1F3A54")
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4A6380")
        textSize = 28f
        textAlign = Paint.Align.RIGHT
    }

    private val avgLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4FC3F7")
        strokeWidth = 1.5f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(12f, 8f), 0f)
    }

    fun setData(data: List<ScorePoint>) {
        points = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (points.isEmpty()) return

        val padL = 60f
        val padR = 20f
        val padT = 20f
        val padB = 36f
        val w = width.toFloat() - padL - padR
        val h = height.toFloat() - padT - padB

        // Grid lines at 25, 50, 75, 100
        for (pct in listOf(0, 25, 50, 75, 100)) {
            val y = padT + h - (pct / 100f) * h
            canvas.drawLine(padL, y, padL + w, y, gridPaint)
            canvas.drawText("$pct%", padL - 8f, y + 10f, labelPaint)
        }

        // Build coords
        val n = points.size
        val coords = points.mapIndexed { i, p ->
            val x = padL + (i.toFloat() / max(n - 1, 1)) * w
            val y = padT + h - (p.score / 100f) * h
            PointF(x, y)
        }

        // Fill gradient
        if (coords.size >= 2) {
            val shader = LinearGradient(0f, padT, 0f, padT + h,
                Color.parseColor("#33F0A500"), Color.TRANSPARENT, Shader.TileMode.CLAMP)
            fillPaint.shader = shader
            val fillPath = Path()
            fillPath.moveTo(coords.first().x, padT + h)
            coords.forEach { fillPath.lineTo(it.x, it.y) }
            fillPath.lineTo(coords.last().x, padT + h)
            fillPath.close()
            canvas.drawPath(fillPath, fillPaint)

            // Line
            val linePath = Path()
            linePath.moveTo(coords.first().x, coords.first().y)
            for (i in 1 until coords.size) {
                val cp1x = (coords[i - 1].x + coords[i].x) / 2f
                linePath.cubicTo(cp1x, coords[i - 1].y, cp1x, coords[i].y, coords[i].x, coords[i].y)
            }
            canvas.drawPath(linePath, linePaint)
        }

        // Average line
        if (points.isNotEmpty()) {
            val avg = points.map { it.score }.average().toFloat()
            val avgY = padT + h - (avg / 100f) * h
            canvas.drawLine(padL, avgY, padL + w, avgY, avgLinePaint)
        }

        // Dots
        coords.forEachIndexed { i, pt ->
            canvas.drawCircle(pt.x, pt.y, 8f, dotBorderPaint)
            canvas.drawCircle(pt.x, pt.y, 5f, dotPaint)
        }
    }
}
