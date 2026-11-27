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

    private fun open(
        activity: Activity, title: String, body: LinearLayout,
        neutralText: String? = null, onNeutral: (() -> Unit)? = null,
    ): AlertDialog {
        val builder = AlertDialog.Builder(activity)
            .setTitle(title)
            .setView(ScrollView(activity).apply { addView(body) })
            .setPositiveButton(R.string.detail_close, null)
        if (neutralText != null && onNeutral != null) builder.setNeutralButton(neutralText) { _, _ -> onNeutral() }
        return builder.show()
    }

    // ------------------------------------------------------------ prices of currencies and gold
    fun showPrices(activity: Activity) {
        val body = page(activity)
        val dialog = open(activity, activity.getString(R.string.prices_title), body, activity.getString(R.string.prices_chart_button)) { showRate(activity) }
        fun render(p: Prices) {
            body.removeAllViews()
            if (p.isEmpty()) { body.addView(text(activity, activity.getString(R.string.prices_none), 14f)); return }
            p.updated?.let { body.addView(text(activity, activity.getString(R.string.prices_updated, it), 12f, alpha = 0.7f)) }
            for (group in p.groups) {
                if (group.lines.isEmpty()) continue
                body.addView(text(activity, activity.getString(group.titleRes), 15f, true).apply { setPadding(0, dp(activity, 14), 0, dp(activity, 4)) })
                for (line in group.lines) {
                    val row = LinearLayout(activity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, dp(activity, 5), 0, dp(activity, 5))
                    }
                    row.addView(text(activity, line.label, 14f), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.3f))
                    row.addView(
                        text(activity, line.value + (if (line.unit.isEmpty()) "" else " " + line.unit), 14f, true).apply { gravity = Gravity.END },
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f),
                    )
                    body.addView(row)
                }
            }
        }
        render(Rate.savedPrices(activity))
        Thread {
            val fresh = IpLookup.httpGet(Rate.URL, 600_000)?.let { Rate.prices(it) }
            if (fresh != null && !fresh.isEmpty()) Rate.savePrices(activity.applicationContext, fresh)
            activity.runOnUiThread {
                if (activity.isFinishing || !dialog.isShowing) return@runOnUiThread
                if (fresh != null && !fresh.isEmpty()) render(fresh)
            }
        }.start()
    }

    // ------------------------------------------------------------ the apps that used the most data today
    fun showTopApps(activity: Activity, onGrant: () -> Unit) {
        val body = page(activity)
        body.addView(text(activity, activity.getString(R.string.forecast_loading), 14f))
        val dialog = open(activity, activity.getString(R.string.section_top_apps), body)
        Thread {
            val list = Usage.topApps(activity.applicationContext, 10)
            val icons = list.map { app ->
                try { app.pkg?.let { activity.packageManager.getApplicationIcon(it) } } catch (e: Exception) { null }
            }
            activity.runOnUiThread {
                if (activity.isFinishing || !dialog.isShowing) return@runOnUiThread
                body.removeAllViews()
                if (list.isEmpty()) {
                    val access = Usage.hasAccess(activity)
                    body.addView(text(activity, activity.getString(if (access) R.string.top_apps_none else R.string.usage_no_access), 14f))
                    if (!access) {
                        body.addView(android.widget.Button(activity).apply {
                            text = activity.getString(R.string.week_grant)
                            setOnClickListener { dialog.dismiss(); onGrant() }
                        })
                    }
                    return@runOnUiThread
                }
                for ((i, app) in list.withIndex()) {
                    val row = LinearLayout(activity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        layoutDirection = View.LAYOUT_DIRECTION_LTR
                        setPadding(0, dp(activity, 8), 0, dp(activity, 8))
                    }
                    row.addView(android.widget.ImageView(activity).apply { setImageDrawable(icons[i]) }, LinearLayout.LayoutParams(dp(activity, 32), dp(activity, 32)))
                    row.addView(
                        text(activity, app.name, 14f).apply {
                            maxLines = 1
                            ellipsize = android.text.TextUtils.TruncateAt.END
                            setPadding(dp(activity, 12), 0, dp(activity, 8), 0)
                        },
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f),
                    )
                    row.addView(
                        text(activity, android.text.format.Formatter.formatShortFileSize(activity, app.bytes), 13f, true).apply { gravity = Gravity.END },
                        LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT),
                    )
                    body.addView(row)
                }
            }
        }.start()
    }

    // ------------------------------------------------------------ the last 7 days of data use
    fun showWeek(activity: Activity, onGrant: () -> Unit) {
        val body = page(activity)
        body.addView(text(activity, activity.getString(R.string.forecast_loading), 14f))
        val dialog = open(activity, activity.getString(R.string.week_title), body)
        Thread {
            val days = Usage.history(activity.applicationContext, 7)
            activity.runOnUiThread {
                if (activity.isFinishing || !dialog.isShowing) return@runOnUiThread
                body.removeAllViews()
                if (days.isEmpty()) {
                    body.addView(text(activity, activity.getString(R.string.usage_no_access), 14f))
                    body.addView(android.widget.Button(activity).apply {
                        text = activity.getString(R.string.week_grant)
                        setOnClickListener { dialog.dismiss(); onGrant() }
                    })
                    return@runOnUiThread
                }
                fun size(bytes: Long) = if (bytes < 0L) "-" else android.text.format.Formatter.formatShortFileSize(activity, bytes)
                fun cell(value: String, bold: Boolean = false, weight: Float = 1f) =
                    text(activity, value, 12f, bold).apply { gravity = Gravity.END; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight) }
                val names = activity.resources.getStringArray(R.array.day_short)
                val head = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; layoutDirection = View.LAYOUT_DIRECTION_LTR }
                head.addView(text(activity, "", 12f), LinearLayout.LayoutParams(dp(activity, 96), LinearLayout.LayoutParams.WRAP_CONTENT))
                head.addView(cell("Wi-Fi", true)); head.addView(cell("SIM", true)); head.addView(cell(activity.getString(R.string.week_total), true))
                body.addView(head)
                var wifiSum = 0L
                var simSum = 0L
                for (i in days.indices.reversed()) {           // today first
                    val day = days[i]
                    val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -(days.size - 1 - i)) }
                    val wifi = maxOf(0L, day.wifi)
                    val sim = maxOf(0L, day.mobile)
                    wifiSum += wifi
                    simSum += sim
                    val row = LinearLayout(activity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutDirection = View.LAYOUT_DIRECTION_LTR
                        setPadding(0, dp(activity, 6), 0, dp(activity, 6))
                    }
                    val label = names[cal.get(java.util.Calendar.DAY_OF_WEEK) % 7] + "  " +
                        String.format(Locale.US, "%02d/%02d", cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.DAY_OF_MONTH))
                    row.addView(text(activity, label, 12f, true), LinearLayout.LayoutParams(dp(activity, 96), LinearLayout.LayoutParams.WRAP_CONTENT))
                    row.addView(cell(size(wifi))); row.addView(cell(size(sim))); row.addView(cell(size(wifi + sim), true))
                    body.addView(row)
                }
                val sum = LinearLayout(activity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutDirection = View.LAYOUT_DIRECTION_LTR
                    setPadding(0, dp(activity, 10), 0, dp(activity, 4))
                }
                sum.addView(text(activity, activity.getString(R.string.week_total), 13f, true), LinearLayout.LayoutParams(dp(activity, 96), LinearLayout.LayoutParams.WRAP_CONTENT))
                sum.addView(cell(size(wifiSum), true)); sum.addView(cell(size(simSum), true)); sum.addView(cell(size(wifiSum + simSum), true))
                body.addView(sum)
            }
        }.start()
    }

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
