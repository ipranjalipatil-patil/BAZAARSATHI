package com.example.ai

import com.example.data.model.ExpenseEntity
import com.example.data.model.TransactionEntity
import kotlin.math.roundToInt

data class FinancialReadinessScore(
    val totalScore: Int, // 0 to 100
    val ratingCategory: ReadinessCategory,
    val revenueConsistencyScore: Int, // 0 to 25
    val expenseStabilityScore: Int,   // 0 to 25
    val positiveCashflowScore: Int,   // 0 to 25
    val recordKeepingScore: Int,      // 0 to 25
    val factors: List<ScoreFactor>,
    val disclaimer: String = "This is an internal financial analytics score and is not an official credit score or loan approval."
)

enum class ReadinessCategory(val label: String, val minScore: Int, val maxScore: Int) {
    NEEDS_IMPROVEMENT("Needs Improvement", 0, 39),
    FAIR("Fair", 40, 59),
    GOOD("Good", 60, 79),
    STRONG("Strong", 80, 100)
}

data class ScoreFactor(
    val title: String,
    val score: Int,
    val maxScore: Int,
    val description: String,
    val isPositive: Boolean
)

object FinancialReadinessCalculator {

    fun calculate(
        transactions: List<TransactionEntity>,
        expenses: List<ExpenseEntity>
    ): FinancialReadinessScore {
        val totalRevenue = transactions.sumOf { it.amount }
        val totalExpense = expenses.sumOf { it.amount }
        val netProfit = totalRevenue - totalExpense

        val distinctTxDays = transactions.map { it.date / 86_400_000L }.distinct().size

        // 1. Revenue Consistency (0 to 25)
        val revenueScore = when {
            transactions.size >= 10 && totalRevenue >= 3000 -> 25
            transactions.size >= 5 && totalRevenue >= 1500 -> 20
            transactions.size >= 2 -> 14
            transactions.isNotEmpty() -> 8
            else -> 0
        }

        // 2. Expense Stability (0 to 25)
        val expenseScore = when {
            totalExpense in 1.0..(totalRevenue * 0.8) -> 25
            totalExpense > 0 && totalExpense <= totalRevenue -> 18
            totalExpense > totalRevenue && totalExpense > 0 -> 10
            expenses.isNotEmpty() -> 12
            else -> 5 // Untracked expenses
        }

        // 3. Positive Cash Flow (0 to 25)
        val cashflowScore = when {
            totalRevenue > 0 && (netProfit / totalRevenue) >= 0.25 -> 25
            totalRevenue > 0 && netProfit > 0 -> 19
            totalRevenue > 0 && netProfit == 0.0 -> 12
            netProfit < 0 -> 6
            else -> 0
        }

        // 4. Record Keeping & Activity Frequency (0 to 25)
        val recordScore = when {
            distinctTxDays >= 4 && (transactions.size + expenses.size) >= 8 -> 25
            distinctTxDays >= 2 && (transactions.size + expenses.size) >= 4 -> 19
            (transactions.size + expenses.size) >= 2 -> 12
            (transactions.size + expenses.size) == 1 -> 6
            else -> 0
        }

        val total = (revenueScore + expenseScore + cashflowScore + recordScore).coerceIn(0, 100)

        val category = when (total) {
            in 0..39 -> ReadinessCategory.NEEDS_IMPROVEMENT
            in 40..59 -> ReadinessCategory.FAIR
            in 60..79 -> ReadinessCategory.GOOD
            else -> ReadinessCategory.STRONG
        }

        val factors = listOf(
            ScoreFactor(
                title = "Revenue Consistency",
                score = revenueScore,
                maxScore = 25,
                description = "Evaluates regular sales generation and ticket size consistency.",
                isPositive = revenueScore >= 15
            ),
            ScoreFactor(
                title = "Expense Stability & Discipline",
                score = expenseScore,
                maxScore = 25,
                description = "Measures controlled supplier costs relative to gross inflow.",
                isPositive = expenseScore >= 18
            ),
            ScoreFactor(
                title = "Positive Cash Flow & Margins",
                score = cashflowScore,
                maxScore = 25,
                description = "Measures net profitability surplus after covering all operating costs.",
                isPositive = cashflowScore >= 18
            ),
            ScoreFactor(
                title = "Record Keeping Frequency",
                score = recordScore,
                maxScore = 25,
                description = "Reflects how diligently daily sales, expenses, and inventory are tracked.",
                isPositive = recordScore >= 15
            )
        )

        return FinancialReadinessScore(
            totalScore = total,
            ratingCategory = category,
            revenueConsistencyScore = revenueScore,
            expenseStabilityScore = expenseScore,
            positiveCashflowScore = cashflowScore,
            recordKeepingScore = recordScore,
            factors = factors
        )
    }
}
