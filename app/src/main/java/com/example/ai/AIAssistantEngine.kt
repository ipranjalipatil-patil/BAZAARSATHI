package com.example.ai

import com.example.data.model.ExpenseEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object AIAssistantEngine {

    fun answerQuery(
        query: String,
        transactions: List<TransactionEntity>,
        expenses: List<ExpenseEntity>,
        inventory: List<InventoryItemEntity>
    ): String {
        val lower = query.trim().lowercase()

        val totalRevenue = transactions.sumOf { it.amount }
        val totalExpense = expenses.sumOf { it.amount }
        val netProfit = totalRevenue - totalExpense

        val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val todayTransactions = transactions.filter { dateFormat.format(Date(it.date)) == todayDateStr }
        val todayExpenses = expenses.filter { dateFormat.format(Date(it.date)) == todayDateStr }
        val todayRevenue = todayTransactions.sumOf { it.amount }
        val todayExpense = todayExpenses.sumOf { it.amount }
        val todayProfit = todayRevenue - todayExpense

        // 1. Questions about Profit
        if (lower.contains("profit") || lower.contains("मुनाफा") || lower.contains("नफा") || lower.contains("bacha")) {
            return if (lower.contains("today") || lower.contains("aaj") || lower.contains("आज")) {
                "Today's net profit is ₹${todayProfit.roundToInt()} (Total Sales: ₹${todayRevenue.roundToInt()} minus Expenses: ₹${todayExpense.roundToInt()})."
            } else {
                "Your overall recorded net profit is ₹${netProfit.roundToInt()}. Total revenue is ₹${totalRevenue.roundToInt()} against expenses of ₹${totalExpense.roundToInt()}."
            }
        }

        // 2. Questions about Earning / Revenue / Sales
        if (lower.contains("earn") || lower.contains("revenue") || lower.contains("kamai") ||
            lower.contains("कमाई") || lower.contains("बिक्री") || lower.contains("विक्री")
        ) {
            val upiSales = transactions.filter { it.paymentMethod == PaymentMethod.UPI.name }.sumOf { it.amount }
            val cashSales = transactions.filter { it.paymentMethod == PaymentMethod.CASH.name }.sumOf { it.amount }
            return "You have generated ₹${totalRevenue.roundToInt()} in total revenue across ${transactions.size} sales. ₹${upiSales.roundToInt()} via UPI and ₹${cashSales.roundToInt()} via Cash."
        }

        // 3. Questions about Expenses / Biggest Expense
        if (lower.contains("expense") || lower.contains("kharch") || lower.contains("खर्च")) {
            val expenseByCategory = expenses.groupBy { it.category }.mapValues { (_, v) -> v.sumOf { it.amount } }
            val biggestCategory = expenseByCategory.maxByOrNull { it.value }

            return if (biggestCategory != null) {
                "Your total recorded expenses are ₹${totalExpense.roundToInt()}. Your biggest expense category is '${biggestCategory.key}' at ₹${biggestCategory.value.roundToInt()} (${((biggestCategory.value / totalExpense) * 100).roundToInt()}% of total expenses)."
            } else {
                "No expenses have been recorded yet. You can log expenses by tapping + or using voice."
            }
        }

        // 4. Questions about Inventory / Stock
        if (lower.contains("stock") || lower.contains("inventory") || lower.contains("सामान") || lower.contains("माल")) {
            val lowStockItems = inventory.filter { it.isLowStock || it.isOutOfStock }
            return if (lowStockItems.isNotEmpty()) {
                val names = lowStockItems.joinToString(", ") { "${it.productName} (${it.quantity} ${it.unit})" }
                "Attention needed! The following item(s) are low or out of stock: $names. Consider restocking soon."
            } else if (inventory.isNotEmpty()) {
                "All ${inventory.size} items in your inventory currently have healthy stock levels."
            } else {
                "You haven't added any items to your inventory yet. Add your supplies to track stock levels!"
            }
        }

        // 5. Questions about Cash vs UPI
        if (lower.contains("upi") || lower.contains("cash") || lower.contains("पेमेंट")) {
            val upiSales = transactions.filter { it.paymentMethod == PaymentMethod.UPI.name }.sumOf { it.amount }
            val cashSales = transactions.filter { it.paymentMethod == PaymentMethod.CASH.name }.sumOf { it.amount }
            val upiShare = if (totalRevenue > 0) ((upiSales / totalRevenue) * 100).roundToInt() else 0
            return "UPI represents $upiShare% (₹${upiSales.roundToInt()}) and Cash represents ${100 - upiShare}% (₹${cashSales.roundToInt()}) of your total business revenue."
        }

        // Default smart response using real metrics
        return "Here is your quick financial status: Revenue ₹${totalRevenue.roundToInt()}, Expenses ₹${totalExpense.roundToInt()}, Net Profit ₹${netProfit.roundToInt()} across ${transactions.size} recorded sales. Ask me about your profit, biggest expense, UPI share, or low stock items!"
    }
}
