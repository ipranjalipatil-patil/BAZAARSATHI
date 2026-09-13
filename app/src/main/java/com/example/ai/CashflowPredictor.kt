package com.example.ai

import com.example.data.model.ExpenseEntity
import com.example.data.model.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class CashflowPredictionResult(
    val hasEnoughData: Boolean,
    val nextDayCashflow: Double,
    val next7DaysCashflow: Double,
    val dailyForecast: List<DailyForecastPoint>,
    val averageDailyRevenue: Double,
    val averageDailyExpense: Double,
    val explanation: String,
    val confidenceScore: Int
)

data class DailyForecastPoint(
    val dayLabel: String,
    val expectedRevenue: Double,
    val expectedExpense: Double,
    val netCashflow: Double
)

object CashflowPredictor {

    fun predict(
        transactions: List<TransactionEntity>,
        expenses: List<ExpenseEntity>
    ): CashflowPredictionResult {
        if (transactions.isEmpty() && expenses.isEmpty()) {
            return CashflowPredictionResult(
                hasEnoughData = false,
                nextDayCashflow = 0.0,
                next7DaysCashflow = 0.0,
                dailyForecast = emptyList(),
                averageDailyRevenue = 0.0,
                averageDailyExpense = 0.0,
                explanation = "Not enough data to generate a reliable prediction. Continue recording transactions.",
                confidenceScore = 0
            )
        }

        // Group transactions and expenses by calendar date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayDisplayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        val revenueByDate = transactions.groupBy { dateFormat.format(Date(it.date)) }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }

        val expenseByDate = expenses.groupBy { dateFormat.format(Date(it.date)) }
            .mapValues { (_, exp) -> exp.sumOf { it.amount } }

        val allDates = (revenueByDate.keys + expenseByDate.keys).sorted()

        if (allDates.size < 2) {
            return CashflowPredictionResult(
                hasEnoughData = false,
                nextDayCashflow = 0.0,
                next7DaysCashflow = 0.0,
                dailyForecast = emptyList(),
                averageDailyRevenue = revenueByDate.values.average().takeIf { !it.isNaN() } ?: 0.0,
                averageDailyExpense = expenseByDate.values.average().takeIf { !it.isNaN() } ?: 0.0,
                explanation = "Not enough data to generate a reliable prediction. Record at least 2 distinct days of business data.",
                confidenceScore = 20
            )
        }

        // Calculate average daily revenue and expense using Moving Average (3-7 days window)
        val dailyRevenues = allDates.map { revenueByDate[it] ?: 0.0 }
        val dailyExpenses = allDates.map { expenseByDate[it] ?: 0.0 }

        val windowSize = minOf(7, dailyRevenues.size)
        val recentRevenues = dailyRevenues.takeLast(windowSize)
        val recentExpenses = dailyExpenses.takeLast(windowSize)

        val avgRevenue = recentRevenues.average()
        val avgExpense = recentExpenses.average()

        // Daily trend slope (simple linear regression slope over the window)
        var revenueTrendSlope = 0.0
        if (recentRevenues.size >= 3) {
            val n = recentRevenues.size.toDouble()
            val xMean = (n - 1) / 2.0
            val yMean = avgRevenue
            var numerator = 0.0
            var denominator = 0.0
            for (i in recentRevenues.indices) {
                numerator += (i - xMean) * (recentRevenues[i] - yMean)
                denominator += (i - xMean) * (i - xMean)
            }
            if (denominator != 0.0) {
                revenueTrendSlope = (numerator / denominator).coerceIn(-avgRevenue * 0.2, avgRevenue * 0.2)
            }
        }

        // Forecast next 7 days
        val forecastPoints = mutableListOf<DailyForecastPoint>()
        val calendar = java.util.Calendar.getInstance()
        var total7DaysNet = 0.0

        for (i in 1..7) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
            val dayName = dayDisplayFormat.format(calendar.time)

            // Weekend adjustment factor (+15% on Fri/Sat/Sun for street food/beverage vendors)
            val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
            val isWeekend = (dayOfWeek == java.util.Calendar.SATURDAY ||
                    dayOfWeek == java.util.Calendar.SUNDAY ||
                    dayOfWeek == java.util.Calendar.FRIDAY)
            val factor = if (isWeekend) 1.15 else 0.95

            val predRev = ((avgRevenue + (revenueTrendSlope * i)) * factor).coerceAtLeast(0.0)
            val predExp = (avgExpense * (if (isWeekend) 1.08 else 0.98)).coerceAtLeast(0.0)
            val net = predRev - predExp

            total7DaysNet += net
            forecastPoints.add(
                DailyForecastPoint(
                    dayLabel = dayName,
                    expectedRevenue = predRev,
                    expectedExpense = predExp,
                    netCashflow = net
                )
            )
        }

        val nextDayNet = forecastPoints.firstOrNull()?.netCashflow ?: (avgRevenue - avgExpense)

        val explanation = buildString {
            append("Based on a ${windowSize}-day moving average of ₹${avgRevenue.roundToInt()} daily revenue and ₹${avgExpense.roundToInt()} daily expenses. ")
            if (revenueTrendSlope > 5) {
                append("Your sales show positive upward momentum (+${revenueTrendSlope.roundToInt()}₹/day). ")
            } else if (revenueTrendSlope < -5) {
                append("Recent sales showed a slight dip; consider promoting specials to boost inflow. ")
            } else {
                append("Your daily financial velocity is stable with balanced cash flows. ")
            }
            append("Weekend factors and recent supply spend have been incorporated.")
        }

        val confidence = minOf(92, 50 + (allDates.size * 7))

        return CashflowPredictionResult(
            hasEnoughData = true,
            nextDayCashflow = nextDayNet,
            next7DaysCashflow = total7DaysNet,
            dailyForecast = forecastPoints,
            averageDailyRevenue = avgRevenue,
            averageDailyExpense = avgExpense,
            explanation = explanation,
            confidenceScore = confidence
        )
    }
}
