package com.example.mymoney.ui.addexpense

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.ui.graphics.vector.ImageVector
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

data class Member(val id: String, val name: String, val isYou: Boolean = false) {
    val displayName: String get() = if (isYou) "$name (You)" else name
    val shortName: String get() = if (isYou) "You" else name
}

data class Category(val emoji: String, val label: String)

val expenseCategories = listOf(
    Category("🍔", "Food"),
    Category("🥬", "Groceries"),
    Category("🧳", "Travel"),
    Category("🧾", "Bills"),
    Category("🛒", "Shopping"),
    Category("⛽", "Fuel"),
    Category("🍿", "Entertainment"),
)

/** Who paid. Amounts are user-entered text (rupees), parsed with [parseMoney]. */
sealed interface Payer {
    data class Single(val memberId: String) : Payer
    data class Multiple(val amounts: Map<String, String>) : Payer
}

/** [label] is for the mode selector; [shortLabel] fits the compact pill on the main screen. */
enum class SplitMode(val label: String, val shortLabel: String, val icon: ImageVector) {
    Equally("Equally", "Equally", Icons.Filled.DragHandle),
    Amounts("By amounts", "By amount", Icons.Filled.CurrencyRupee),
    Shares("By shares", "By shares", Icons.Filled.PieChart),
    Percentages("By percentages", "By %", Icons.Filled.Percent),
}

/**
 * Split configuration. Each mode keeps its own inputs so switching tabs doesn't lose
 * what was typed. Money is held as text while editing and parsed to paise on demand.
 */
data class SplitState(
    val mode: SplitMode = SplitMode.Equally,
    val included: Set<String>,
    val amounts: Map<String, String> = emptyMap(),
    val shares: Map<String, Int> = emptyMap(),
    val percents: Map<String, String> = emptyMap(),
) {
    fun isValid(total: Long): Boolean = total > 0 && when (mode) {
        SplitMode.Equally -> included.isNotEmpty()
        SplitMode.Amounts -> amounts.values.sumOf(::parseMoney) == total
        SplitMode.Shares -> shares.values.sum() > 0
        SplitMode.Percentages -> percents.values.sumOf(::parseMoney) == 100_00L
    }

    /** Amount (paise) each member owes; empty when the split can't be computed. */
    fun owed(total: Long, members: List<Member>): Map<String, Long> = when (mode) {
        SplitMode.Equally -> distribute(total, members.filter { it.id in included }.associate { it.id to 1L })
        SplitMode.Amounts -> members.associate { it.id to parseMoney(amounts[it.id].orEmpty()) }
        SplitMode.Shares -> distribute(total, members.associate { it.id to (shares[it.id] ?: 0).toLong() })
        SplitMode.Percentages -> {
            val pct = members.associate { it.id to parseMoney(percents[it.id].orEmpty()) }
            // Only spread rounding leftovers once the percentages are complete; before that,
            // show each person's share of the total, not of the partial sum.
            if (pct.values.sum() == 100_00L) distribute(total, pct)
            else pct.mapValues { total * it.value / 100_00L }
        }
    }
}

data class ExpenseDraft(
    val total: Long,
    val description: String,
    val category: Category?,
    val paidBy: Map<String, Long>,
    val owed: Map<String, Long>,
    val splitMode: SplitMode,
)

/** Up to 9 whole digits and 2 decimals; used for both rupees and percentages. */
val moneyInputRegex = Regex("""\d{0,9}(\.\d{0,2})?""")

/** "12.5" -> 1250. Works for rupees→paise and percent→hundredths of a percent. */
fun parseMoney(text: String): Long =
    text.toBigDecimalOrNull()?.movePointRight(2)?.toLong() ?: 0L

fun formatRupees(paise: Long): String {
    val f = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
    val digits = if (paise % 100 == 0L) 0 else 2
    f.minimumFractionDigits = digits
    f.maximumFractionDigits = digits
    return f.format(BigDecimal.valueOf(paise, 2))
}

fun formatPercent(hundredths: Long): String =
    BigDecimal.valueOf(hundredths, 2).stripTrailingZeros().toPlainString() + "%"

/** Splits [total] proportionally to [weights]; leftover paise go to the first entries. */
fun distribute(total: Long, weights: Map<String, Long>): Map<String, Long> {
    val sum = weights.values.sum()
    if (sum <= 0) return emptyMap()
    val result = LinkedHashMap<String, Long>()
    weights.forEach { (id, w) -> result[id] = total * w / sum }
    var leftover = total - result.values.sum()
    for (id in result.keys) {
        if (leftover <= 0) break
        if ((weights[id] ?: 0) > 0) { result[id] = result.getValue(id) + 1; leftover-- }
    }
    return result
}
