package com.example.ai

import com.example.data.model.ExpenseEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class BusinessInsight(
    val title: String,
    val description: String,
    val category: InsightType,
    val metricValue: String? = null
)

enum class InsightType {
    REVENUE,
    EXPENSE,
    PAYMENT,
    EFFICIENCY
}

object BusinessInsightsEngine {

    fun generateInsights(
        transactions: List<TransactionEntity>,
        expenses: List<ExpenseEntity>
    ): List<BusinessInsight> {
        val list = mutableListOf<BusinessInsight>()

        val totalRevenue = transactions.sumOf { it.amount }
        val totalExpense = expenses.sumOf { it.amount }
        val netProfit = totalRevenue - totalExpense

        // 1. Profit Margin Insight
        if (totalRevenue > 0) {
            val margin = ((netProfit / totalRevenue) * 100).roundToInt()
            val text = if (margin > 30) {
                "Healthy profit margin of $margin%! Your cost-to-revenue ratio is well optimized."
            } else if (margin > 0) {
                "Positive margin of $margin%. Review raw material expenses to retain more profit."
            } else {
                "Current expenses exceed revenue. Focus on high-margin items to restore profitability."
            }
            list.add(
                BusinessInsight(
                    title = "Profit Margin",
                    description = text,
                    category = InsightType.EFFICIENCY,
                    metricValue = "$margin%"
                )
            )
        }

        // 2. Best-Performing Day
        if (transactions.isNotEmpty()) {
            val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            val revenueByDayOfWeek = transactions.groupBy { dayFormat.format(Date(it.date)) }
                .mapValues { (_, txs) -> txs.sumOf { it.amount } }

            val bestDay = revenueByDayOfWeek.maxByOrNull { it.value }
            if (bestDay != null) {
                list.add(
                    BusinessInsight(
                        title = "Best-Performing Day",
                        description = "Your sales peak on ${bestDay.key}s with total ₹${bestDay.value.roundToInt()} collected. Ensure extra stock on this day!",
                        category = InsightType.REVENUE,
                        metricValue = bestDay.key
                    )
                )
            }
        }

        // 3. Largest Expense Category
        if (expenses.isNotEmpty()) {
            val expenseByCategory = expenses.groupBy { it.category }
                .mapValues { (_, exp) -> exp.sumOf { it.amount } }

            val topCategory = expenseByCategory.maxByOrNull { it.value }
            if (topCategory != null && totalExpense > 0) {
                val percent = ((topCategory.value / totalExpense) * 100).roundToInt()
                list.add(
                    BusinessInsight(
                        title = "Largest Expense Category",
                        description = "${topCategory.key} constitutes $percent% of your recorded expenses (₹${topCategory.value.roundToInt()}). Negotiating bulk rates could save money.",
                        category = InsightType.EXPENSE,
                        metricValue = "${topCategory.key} ($percent%)"
                    )
                )
            }
        }

        // 4. Cash vs UPI Adoption
        if (transactions.isNotEmpty()) {
            val upiTotal = transactions.filter { it.paymentMethod == PaymentMethod.UPI.name }.sumOf { it.amount }
            val cashTotal = transactions.filter { it.paymentMethod == PaymentMethod.CASH.name }.sumOf { it.amount }
            val upiPercent = if (totalRevenue > 0) ((upiTotal / totalRevenue) * 100).roundToInt() else 0

            val desc = if (upiPercent > 50) {
                "Digital UPI payments dominate ($upiPercent% of sales). You maintain minimal cash handling friction."
            } else {
                "Cash payments account for ${100 - upiPercent}% of sales. Keep adequate change ready for peak hours."
            }

            list.add(
                BusinessInsight(
                    title = "Payment Mode Distribution",
                    description = desc,
                    category = InsightType.PAYMENT,
                    metricValue = "$upiPercent% UPI"
                )
            )
        }

        if (list.isEmpty()) {
            list.add(
                BusinessInsight(
                    title = "Welcome to Bazaar Saathi",
                    description = "Start logging your daily sales and expenses. AI will automatically analyze your sales patterns and provide business advice.",
                    category = InsightType.EFFICIENCY,
                    metricValue = "Ready"
                )
            )
        }

        return list
    }
}
