package app.myipwidget

import android.app.Activity
import android.app.AlertDialog
import android.view.Gravity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale

/** The two pop-ups behind the cards of the first page: the dollar chart and the weather forecast. */
object DetailDialogs {
    private fun dp(activity: Activity, value: Int) = (value * activity.resources.displayMetrics.density).toInt()

    private fun text(activity: Activity, value: String, size: Float, bold: Boolean = false, alpha: Float = 1f) =
        TextView(activity).apply {
            text = value
            textSize = size
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            this.alpha = alpha
        }

    private fun page(activity: Activity): LinearLayout = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(activity, 20), dp(activity, 8), dp(activity, 20), dp(activity, 4))
    }

    private fun open(activity: Activity, title: String, body: LinearLayout): AlertDialog =
        AlertDialog.Builder(activity)
            .setTitle(title)
            .setView(ScrollView(activity).apply { addView(body) })
            .setPositiveButton(R.string.detail_close, null)
            .show()

    // ------------------------------------------------------------ dollar
    fun showRate(activity: Activity) {
        val body = page(activity)
        val chart = LineChart(activity).apply { textColor = 0xFF888888.toInt() }
        val stats = text(activity, "", 14f, true)
        val note = text(activity, activity.getString(R.string.rate_dialog_note), 12f, alpha = 0.7f)
        body.addView(chart, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(activity, 170)))
        stats.layoutDirection = View.LAYOUT_DIRECTION_LTR
        stats.gravity = Gravity.CENTER
        stats.setPadding(0, dp(activity, 10), 0, dp(activity, 6))
        body.addView(stats)
        note.gravity = Gravity.CENTER
        body.addView(note)
        val dialog = open(activity, activity.getString(R.string.rate_dialog_title), body)

        fun render(list: List<Long>) {
            val values = list.takeLast(30)
            if (values.size < 2) {
                chart.visibility = View.GONE
                stats.text = activity.getString(R.string.rate_none)
                return
            }
            chart.visibility = View.VISIBLE
            chart.values = values.map { it.toDouble() }
            stats.text = String.format(Locale.US, "\u25BC %,d    \u25B2 %,d    $ %,d", values.min(), values.max(), values.last())
        }
        render(Rate.savedHistory(activity))
        Thread {
            val fresh = IpLookup.httpGet(Rate.URL, 600_000)?.let { Rate.history(it) }.orEmpty()
            if (fresh.isNotEmpty()) Rate.saveHistory(activity.applicationContext, fresh)
            activity.runOnUiThread {
                if (activity.isFinishing || !dialog.isShowing) return@runOnUiThread
                if (fresh.isNotEmpty()) render(fresh.mapNotNull { it.toLongOrNull() })
            }
        }.start()
    }

    // ------------------------------------------------------------ weather
    fun showForecast(activity: Activity) {
        val body = page(activity)
        val status = text(activity, activity.getString(R.string.forecast_loading), 14f)
        body.addView(status)
        val dialog = open(activity, activity.getString(R.string.forecast_title) + " \u00B7 " + Prefs.cityName(activity), body)
        val lat = Prefs.cityLat(activity)
        val lon = Prefs.cityLon(activity)
        if (lat == null || lon == null) { status.text = activity.getString(R.string.forecast_failed); return }
        Thread {
            val forecast = Weather.fetchForecast(lat, lon)
            activity.runOnUiThread {
                if (activity.isFinishing || !dialog.isShowing) return@runOnUiThread
                if (forecast == null) { status.text = activity.getString(R.string.forecast_failed); return@runOnUiThread }
                body.removeView(status)
                fillForecast(activity, body, forecast)
            }
        }.start()
    }

    private fun fillForecast(activity: Activity, body: LinearLayout, forecast: Forecast) {
        if (forecast.hours.isNotEmpty()) {
            body.addView(text(activity, activity.getString(R.string.forecast_hourly), 14f, true))
            val strip = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutDirection = View.LAYOUT_DIRECTION_LTR
                setPadding(0, dp(activity, 6), 0, dp(activity, 10))
            }
            for (hour in forecast.hours) {
                val column = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL }
                column.addView(text(activity, hour.label, 11f, alpha = 0.7f).apply { gravity = Gravity.CENTER })
                column.addView(text(activity, Weather.symbol(hour.code), 22f).apply { gravity = Gravity.CENTER })
                column.addView(text(activity, hour.temp.toString() + "\u00B0", 14f, true).apply { gravity = Gravity.CENTER })
                strip.addView(column, LinearLayout.LayoutParams(dp(activity, 54), LinearLayout.LayoutParams.WRAP_CONTENT))
            }
            body.addView(HorizontalScrollView(activity).apply { isHorizontalScrollBarEnabled = false; addView(strip) })
        }
        if (forecast.days.isNotEmpty()) {
            body.addView(text(activity, activity.getString(R.string.forecast_daily), 14f, true))
            val names = activity.resources.getStringArray(R.array.day_short)
            for (day in forecast.days) {
                val row = LinearLayout(activity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    layoutDirection = View.LAYOUT_DIRECTION_LTR
                    setPadding(0, dp(activity, 6), 0, dp(activity, 6))
                }
                row.addView(text(activity, names[day.weekday % 7], 14f, true), LinearLayout.LayoutParams(dp(activity, 56), LinearLayout.LayoutParams.WRAP_CONTENT))
                row.addView(text(activity, day.label, 13f, alpha = 0.7f), LinearLayout.LayoutParams(dp(activity, 56), LinearLayout.LayoutParams.WRAP_CONTENT))
                row.addView(text(activity, Weather.symbol(day.code), 20f), LinearLayout.LayoutParams(dp(activity, 40), LinearLayout.LayoutParams.WRAP_CONTENT))
                row.addView(
                    text(activity, "\u25B2" + day.max + "\u00B0   \u25BC" + day.min + "\u00B0", 14f, true).apply { gravity = Gravity.END },
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f),
                )
                body.addView(row)
            }
        }
    }
}
