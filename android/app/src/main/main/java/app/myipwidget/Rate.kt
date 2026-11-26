package app.myipwidget

import android.content.Context
import java.util.Locale

/**
 * The dollar rate, read from the newest post of a public channel page that mentions "دلار"
 * (the page has no fixed layout we can rely on, so the page text is searched, not its tags).
 */
class PriceLine(val label: String, val value: String, val unit: String)
class PriceGroup(val titleRes: Int, val lines: List<PriceLine>)
class Prices(val updated: String?, val groups: List<PriceGroup>) {
    fun isEmpty() = groups.all { it.lines.isEmpty() }
}

object Rate {
    const val URL = "https://eitaa.com/talaclinic1"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences("rate", Context.MODE_PRIVATE)

    private fun latinDigits(text: String): String = buildString(text.length) {
        for (c in text) {
            append(
                when (c) {
                    in '۰'..'۹' -> '0' + (c - '۰')
                    in '٠'..'٩' -> '0' + (c - '٠')
                    '٬', '،' -> ','
                    else -> c
                },
            )
        }
    }

    /** Every rate written after the word "دلار" in the page (as digits only), the oldest post first. */
    fun history(html: String): List<String> {
        val text = latinDigits(
            html.replace(Regex("<[^>]*>"), " ").replace("&nbsp;", " ").replace(Regex("&[a-zA-Z#0-9]+;"), " "),
        )
        // the line looks like "دلار: 268,300 تومان"; a number after the word is the fallback
        var found = Regex("دلار\\s*[:：]\\s*([0-9][0-9,.]{3,})").findAll(text).toList()
        if (found.isEmpty()) found = Regex("دلار[^0-9]{0,40}([0-9][0-9,.]{3,})").findAll(text).toList()
        return found.mapNotNull { m -> m.groupValues[1].filter { it.isDigit() }.takeIf { it.length in 4..9 } }
    }

    /** The newest number written after the word "دلار" (as digits only), or null. */
    fun parse(html: String): String? = history(html).lastOrNull()

    fun cached(context: Context): String? = prefs(context).getString("digits", null)

    /** Reads the page again if the saved rate is older than [maxAgeMs]. Call from a background thread. */
    fun refreshIfStale(context: Context, maxAgeMs: Long) {
        if (!Prefs.showRate(context)) return
        val p = prefs(context)
        val age = System.currentTimeMillis() - p.getLong("time", 0L)
        if (p.contains("digits") && age in 0..maxAgeMs) return
        val body = IpLookup.httpGet(URL, 600_000) ?: return
        val list = history(body)
        val digits = list.lastOrNull() ?: return
        val before = p.getString("digits", null)
        val edit = p.edit().putString("digits", digits).putLong("time", System.currentTimeMillis())
            .putString("history", list.takeLast(40).joinToString(","))
        val freshPrices = prices(body)
        if (!freshPrices.isEmpty()) edit.putString("prices", encodePrices(freshPrices))
        if (before != null && before != digits) {
            edit.putString("prev", before)   // the arrow compares with this
        } else if (p.getString("prev", null) == null) {
            // first read: the page itself lists the older posts, so the arrow can show at once
            val earlier = list.dropLast(1).lastOrNull { it != digits }
            if (earlier != null) edit.putString("prev", earlier)
        }
        edit.apply()
    }

    private val PRICE_SPEC = listOf(
        R.string.prices_gold to listOf(
            "گرم 18 تهران", "خرید متفرقه", "انس طلا", "یک گرم طلای 24 عیار", "یک گرم طلای 20 عیار",
            "یک گرم طلای 21 عیار", "مظنه تهران", "انس نقره",
        ),
        R.string.prices_currency to listOf("دلار", "یورو", "درهم"),
        R.string.prices_coin to listOf("ربع سکه", "نیم سکه", "سکه طرح جدید", "سکه طرح قدیم", "سکه یک گرمی"),
    )

    /** The gold, currency and coin lines of the newest post ("label: number unit"). */
    fun prices(html: String): Prices {
        val text = latinDigits(
            html.replace(Regex("<[^>]*>"), " ").replace("&nbsp;", " ").replace(Regex("&[a-zA-Z#0-9]+;"), " "),
        )
        val time = Regex("آخرین\\s+بروزرسانی\\s*[:：]\\s*([0-9]{1,2})\\s*[:：]\\s*([0-9]{2})").findAll(text).lastOrNull()
            ?.let { it.groupValues[1] + ":" + it.groupValues[2] }
        val groups = PRICE_SPEC.map { (title, labels) ->
            PriceGroup(
                title,
                labels.mapNotNull { label ->
                    val words = label.split(" ").joinToString("[\\s\\u200c]+") { Regex.escape(it) }
                    val match = Regex(words + "\\s*[:：]\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*(تومان|دلار|ریال)?")
                        .findAll(text).lastOrNull() ?: return@mapNotNull null
                    val raw = match.groupValues[1].replace(",", "")
                    val shown = if (raw.contains('.')) raw else (raw.toLongOrNull()?.let { String.format(Locale.US, "%,d", it) } ?: return@mapNotNull null)
                    PriceLine(label, shown, match.groupValues[2])
                },
            )
        }
        return Prices(time, groups)
    }

    private fun encodePrices(p: Prices): String = buildString {
        append("U|").append(p.updated ?: "").append('\n')
        p.groups.forEachIndexed { gi, g ->
            for (l in g.lines) append(gi).append('|').append(l.label).append('|').append(l.value).append('|').append(l.unit).append('\n')
        }
    }

    private fun decodePrices(text: String): Prices {
        var updated: String? = null
        val lists = PRICE_SPEC.map { ArrayList<PriceLine>() }
        for (line in text.lines()) {
            val parts = line.split('|')
            if (parts.size >= 2 && parts[0] == "U") { updated = parts[1].ifEmpty { null }; continue }
            if (parts.size == 4) {
                val gi = parts[0].toIntOrNull() ?: continue
                if (gi in lists.indices) lists[gi].add(PriceLine(parts[1], parts[2], parts[3]))
            }
        }
        return Prices(updated, PRICE_SPEC.mapIndexed { i, spec -> PriceGroup(spec.first, lists[i]) })
    }

    fun savedPrices(context: Context): Prices = decodePrices(prefs(context).getString("prices", "") ?: "")

    fun savePrices(context: Context, p: Prices) {
        if (!p.isEmpty()) prefs(context).edit().putString("prices", encodePrices(p)).apply()
    }

    /** The rates of the last posts the page showed (the oldest first), kept for the chart when there is no network. */
    fun savedHistory(context: Context): List<Long> =
        (prefs(context).getString("history", "") ?: "").split(',').mapNotNull { it.toLongOrNull() }

    fun saveHistory(context: Context, list: List<String>) {
        prefs(context).edit().putString("history", list.takeLast(40).joinToString(",")).apply()
    }

    /** How many percent the last change was (positive: a rise), or null when no change is known. */
    fun changePercent(context: Context): Double? {
        val p = prefs(context)
        val now = p.getString("digits", null)?.toDoubleOrNull() ?: return null
        val before = p.getString("prev", null)?.toDoubleOrNull() ?: return null
        return if (before > 0) (now - before) * 100.0 / before else null
    }

    /** 1 when the last change was a rise, -1 a fall, 0 when no change has been seen yet. */
    fun trend(context: Context): Int {
        val p = prefs(context)
        val now = p.getString("digits", null)?.toLongOrNull() ?: return 0
        val before = p.getString("prev", null)?.toLongOrNull() ?: return 0
        return now.compareTo(before).coerceIn(-1, 1)
    }

    /** "$ 98,500" */
    fun format(digits: String?): String {
        val value = digits?.toLongOrNull() ?: return "$ …"
        return "$ " + "%,d".format(Locale.US, value)
    }
}
