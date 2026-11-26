package app.myipwidget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import java.util.Locale

/** A small line chart of a list of numbers (the oldest first), with the highest and lowest value written on it. */
class LineChart(context: Context) : View(context) {
    var values: List<Double> = emptyList()
        set(value) { field = value; invalidate() }
    var lineColor: Int = 0xFF8AB4F8.toInt()
    var textColor: Int = 0xFF888888.toInt()

    private val density = resources.displayMetrics.density
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 2.5f * density; strokeJoin = Paint.Join.ROUND }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 11f * density }

    override fun onDraw(canvas: Canvas) {
        if (values.size < 2) return
        val left = 8f * density
        val right = width - 8f * density
        val top = 18f * density
        val bottom = height - 8f * density
        val low = values.min()
        val high = values.max()
        val span = if (high - low < 1e-9) 1.0 else high - low
        fun x(i: Int) = left + (right - left) * i / (values.size - 1)
        fun y(v: Double) = (bottom - (bottom - top) * ((v - low) / span)).toFloat()

        val path = Path()
        values.forEachIndexed { i, v -> if (i == 0) path.moveTo(x(i), y(v)) else path.lineTo(x(i), y(v)) }
        line.color = lineColor
        fill.color = (lineColor and 0x00FFFFFF) or 0x22000000
        val area = Path(path).apply { lineTo(x(values.size - 1), bottom); lineTo(x(0), bottom); close() }
        canvas.drawPath(area, fill)
        canvas.drawPath(path, line)
        dot.color = lineColor
        canvas.drawCircle(x(values.size - 1), y(values.last()), 4f * density, dot)

        label.color = textColor
        label.textAlign = Paint.Align.LEFT
        canvas.drawText(String.format(Locale.US, "%,d", high.toLong()), left, top - 5f * density, label)
        canvas.drawText(String.format(Locale.US, "%,d", low.toLong()), left, height - 1f * density, label)
    }
}
