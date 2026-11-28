package app.myipwidget

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Calendar

/** The Persian (Shamsi) calendar of a month, with the holidays and occasions. */
object CalendarDialog {
    private val HOLIDAY = 0xFFE5484D.toInt()
    private val OCCASION = 0xFFE0A100.toInt()

    private fun dp(activity: Activity, v: Int) = (v * activity.resources.displayMetrics.density).toInt()

    fun show(activity: Activity) {
        val now = Calendar.getInstance()
        val today = PersianDate.fromGregorian(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH))
        var year = today.first
        var month = today.second
        var selected = today

        val body = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 16), dp(activity, 8), dp(activity, 16), dp(activity, 4))
        }
        val title = TextView(activity).apply { textSize = 18f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER }
        val subtitle = TextView(activity).apply { textSize = 12f; alpha = 0.75f; gravity = Gravity.CENTER; setPadding(0, 0, 0, dp(activity, 8)) }
        val legend = TextView(activity).apply {
            text = activity.getString(R.string.cal_legend)
            textSize = 11f
            alpha = 0.7f
            setPadding(0, dp(activity, 6), 0, 0)
        }
        val grid = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL; layoutDirection = View.LAYOUT_DIRECTION_RTL }
        val info = TextView(activity).apply { textSize = 14f; setPadding(0, dp(activity, 10), 0, dp(activity, 6)) }
        val list = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        val buttons = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; layoutDirection = View.LAYOUT_DIRECTION_RTL }

        fun cell(text: String, bold: Boolean = false): TextView = TextView(activity).apply {
            this.text = text
            gravity = Gravity.CENTER
            textSize = 15f
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }

        fun dayCell(d: Int, dayCal: Calendar, color: Int?, bold: Boolean): LinearLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            addView(cell(PersianDate.digits(d), bold).apply { if (color != null) setTextColor(color) })
            val small = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutDirection = View.LAYOUT_DIRECTION_LTR
                setPadding(dp(activity, 4), 0, dp(activity, 4), 0)
            }
            fun tiny(text: String, gravity: Int) = TextView(activity).apply {
                this.text = text
                textSize = 9f
                alpha = 0.65f
                this.gravity = gravity
                if (color != null) setTextColor(color)
            }
            small.addView(tiny(dayCal.get(Calendar.DAY_OF_MONTH).toString(), Gravity.START), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            small.addView(tiny(PersianDate.hijri(dayCal)?.let { PersianDate.digits(it.third) } ?: "", Gravity.END), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(small, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        fun infoText(y: Int, m: Int, d: Int): String {
            val cal = PersianDate.toGregorian(y, m, d)
            val weekday = PersianDate.weekdayName(cal.get(Calendar.DAY_OF_WEEK) % 7)
            val occasion = Occasions.today(cal)
            val lunar = PersianDate.hijri(cal)?.let { PersianDate.digits(it.third) + " " + PersianDate.hijriMonthName(it.second) + " " + PersianDate.digits(it.first) + " (" + activity.getString(R.string.cal_lunar) + ")" }
            return weekday + " " + PersianDate.digits(d) + " " + PersianDate.monthName(m) + " " + PersianDate.digits(y) + "\n" +
                PersianDate.gregorian(cal) + (if (lunar != null) "\n" + lunar else "") + (if (occasion != null) "\n" + Occasions.label(occasion) else "")
        }

        fun render() {
            title.text = PersianDate.monthName(month) + " " + PersianDate.digits(year)
            val monthDays = PersianDate.daysInMonth(year, month)
            val firstDay = PersianDate.toGregorian(year, month, 1)
            val lastDay = PersianDate.toGregorian(year, month, monthDays)
            val gMonths = java.text.DateFormatSymbols.getInstance(java.util.Locale.US).shortMonths
            val g1 = firstDay.get(Calendar.MONTH); val g2 = lastDay.get(Calendar.MONTH)
            val gy1 = firstDay.get(Calendar.YEAR); val gy2 = lastDay.get(Calendar.YEAR)
            val gregorianLine = when {
                gy1 != gy2 -> gMonths[g1] + " " + gy1 + " \u2013 " + gMonths[g2] + " " + gy2
                g1 != g2 -> gMonths[g1] + " \u2013 " + gMonths[g2] + " " + gy1
                else -> gMonths[g1] + " " + gy1
            }
            val h1 = PersianDate.hijri(firstDay); val h2 = PersianDate.hijri(lastDay)
            val lunarLine = if (h1 == null || h2 == null) "" else when {
                h1.first != h2.first -> PersianDate.hijriMonthName(h1.second) + " " + PersianDate.digits(h1.first) + " \u2013 " + PersianDate.hijriMonthName(h2.second) + " " + PersianDate.digits(h2.first)
                h1.second != h2.second -> PersianDate.hijriMonthName(h1.second) + " \u2013 " + PersianDate.hijriMonthName(h2.second) + " " + PersianDate.digits(h1.first)
                else -> PersianDate.hijriMonthName(h1.second) + " " + PersianDate.digits(h1.first)
            }
            subtitle.text = gregorianLine + (if (lunarLine.isNotEmpty()) "\n" + lunarLine else "")
            grid.removeAllViews()
            val head = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; layoutDirection = View.LAYOUT_DIRECTION_RTL }
            for (name in listOf("ش", "ی", "د", "س", "چ", "پ", "ج")) {
                head.addView(cell(name, true).apply { alpha = 0.7f }, LinearLayout.LayoutParams(0, dp(activity, 32), 1f))
            }
            grid.addView(head)

            val days = PersianDate.daysInMonth(year, month)
            val offset = PersianDate.toGregorian(year, month, 1).get(Calendar.DAY_OF_WEEK) % 7   // Saturday = 0
            var day = 1
            for (week in 0 until 6) {
                if (day > days) break
                val row = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; layoutDirection = View.LAYOUT_DIRECTION_RTL }
                for (column in 0..6) {
                    if ((week == 0 && column < offset) || day > days) {
                        row.addView(View(activity), LinearLayout.LayoutParams(0, dp(activity, 56), 1f))
                        continue
                    }
                    val d = day++
                    val dayCal = PersianDate.toGregorian(year, month, d)
                    val occasion = Occasions.today(dayCal)
                    val isToday = year == today.first && month == today.second && d == today.third
                    val isSelected = Triple(year, month, d) == selected
                    val color = when {
                        occasion != null && occasion.holiday -> HOLIDAY
                        occasion != null -> OCCASION
                        else -> null
                    }
                    row.addView(
                        dayCell(d, dayCal, color, isToday || isSelected).apply {
                            if (isSelected) setBackgroundResource(R.drawable.cal_selected)
                            else if (isToday) setBackgroundResource(R.drawable.cal_today)
                            setOnClickListener {
                                selected = Triple(year, month, d)
                                render()
                            }
                        },
                        LinearLayout.LayoutParams(0, dp(activity, 56), 1f).apply { setMargins(dp(activity, 2), dp(activity, 2), dp(activity, 2), dp(activity, 2)) },
                    )
                }
                grid.addView(row)
            }

            info.text = infoText(selected.first, selected.second, selected.third)

            list.removeAllViews()
            var any = false
            for (d in 1..days) {
                val occasion = Occasions.today(PersianDate.toGregorian(year, month, d)) ?: continue
                if (occasion.text == "جمعه") continue   // every Friday: just the red number
                if (!any) {
                    list.addView(TextView(activity).apply {
                        text = activity.getString(R.string.cal_month_occasions)
                        textSize = 14f
                        typeface = Typeface.DEFAULT_BOLD
                        setPadding(0, dp(activity, 10), 0, dp(activity, 4))
                    })
                    any = true
                }
                list.addView(TextView(activity).apply {
                    text = PersianDate.digits(d) + " " + PersianDate.monthName(month) + ": " + Occasions.label(occasion)
                    textSize = 13f
                    setPadding(0, dp(activity, 2), 0, dp(activity, 2))
                    setTextColor(if (occasion.holiday) HOLIDAY else OCCASION)
                })
            }
            if (!any) {
                list.addView(TextView(activity).apply {
                    text = activity.getString(R.string.cal_none)
                    textSize = 13f
                    alpha = 0.7f
                    setPadding(0, dp(activity, 10), 0, 0)
                })
            }
        }

        fun go(step: Int) {
            month += step
            if (month < 1) { month = 12; year-- }
            if (month > 12) { month = 1; year++ }
            render()
        }
        fun button(textRes: Int, onClick: () -> Unit) = Button(activity).apply {
            setText(textRes)
            textSize = 13f
            setOnClickListener { onClick() }
        }
        buttons.addView(button(R.string.cal_prev) { go(-1) }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        buttons.addView(button(R.string.cal_today) { year = today.first; month = today.second; selected = today; render() }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        buttons.addView(button(R.string.cal_next) { go(1) }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        body.addView(title)
        body.addView(subtitle)
        body.addView(grid)
        body.addView(legend)
        body.addView(info)
        body.addView(buttons)
        body.addView(list)
        render()
        AlertDialog.Builder(activity)
            .setTitle(R.string.calendar_title)
            .setView(ScrollView(activity).apply { addView(body) })
            .setPositiveButton(R.string.detail_close, null)
            .show()
    }
}
