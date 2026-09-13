package com.example.ai

import com.example.data.model.InventoryItemEntity
import com.example.data.model.TransactionEntity
import kotlin.math.ceil
import kotlin.math.max

data class InventoryPredictionItem(
    val item: InventoryItemEntity,
    val estimatedDailyConsumption: Double,
    val daysUntilStockout: Double,
    val suggestedPurchaseQuantity: Double,
    val recommendationText: String,
    val urgencyLevel: UrgencyLevel
)

enum class UrgencyLevel {
    CRITICAL, // 0 days (out of stock)
    HIGH,     // <= 2 days
    MEDIUM,   // <= 4 days
    LOW       // > 4 days (healthy)
}

data class InventoryPredictionResult(
    val predictions: List<InventoryPredictionItem>,
    val itemsRequiringAttentionCount: Int,
    val overallSummary: String
)

object InventoryPredictor {

    fun predict(
        inventory: List<InventoryItemEntity>,
        transactions: List<TransactionEntity>
    ): InventoryPredictionResult {
        if (inventory.isEmpty()) {
            return InventoryPredictionResult(
                predictions = emptyList(),
                itemsRequiringAttentionCount = 0,
                overallSummary = "No products in inventory yet. Add your supplies to get AI predictions."
            )
        }

        val totalTransactions = transactions.size
        // Estimate daily transaction count to calculate consumption
        val distinctDays = transactions.map { it.date / (86_400_000L) }.distinct().size.coerceAtLeast(1)

        val predictions = inventory.map { item ->
            // Match mentions in sales or estimate proportional consumption based on item minStock
            val mentionsInSales = transactions.count { tx ->
                tx.product.contains(item.productName, ignoreCase = true) ||
                        tx.description.contains(item.productName, ignoreCase = true)
            }

            val estimatedDaily = if (mentionsInSales > 0) {
                max(0.5, (mentionsInSales.toDouble() / distinctDays) * 1.5)
            } else {
                // Heuristic baseline from minimum stock (assumed ~3 days buffer)
                max(0.4, item.minimumStock / 3.0)
            }

            val daysLeft = if (item.quantity <= 0) {
                0.0
            } else {
                item.quantity / estimatedDaily
            }

            val urgency = when {
                item.quantity <= 0 -> UrgencyLevel.CRITICAL
                daysLeft <= 2.0 -> UrgencyLevel.HIGH
                daysLeft <= 4.0 -> UrgencyLevel.MEDIUM
                else -> UrgencyLevel.LOW
            }

            // Target 7-day safety buffer
            val suggestedPurchase = if (daysLeft < 5.0) {
                val needed = (estimatedDaily * 7.0) - item.quantity
                ceil(max(needed, item.minimumStock * 1.5))
            } else {
                0.0
            }

            val recommendation = when (urgency) {
                UrgencyLevel.CRITICAL -> "${item.productName} is OUT OF STOCK. Restock approximately ${suggestedPurchase.toInt()} ${item.unit} immediately."
                UrgencyLevel.HIGH -> "${item.productName} may run out in ~${daysLeft.toInt()} days. Consider purchasing ~${suggestedPurchase.toInt()} ${item.unit}."
                UrgencyLevel.MEDIUM -> "${item.productName} stock is moderate (~${daysLeft.toInt()} days left). Reorder ~${suggestedPurchase.toInt()} ${item.unit} soon."
                UrgencyLevel.LOW -> "${item.productName} stock is adequate for ~${daysLeft.toInt()} days."
            }

            InventoryPredictionItem(
                item = item,
                estimatedDailyConsumption = estimatedDaily,
                daysUntilStockout = daysLeft,
                suggestedPurchaseQuantity = suggestedPurchase,
                recommendationText = recommendation,
                urgencyLevel = urgency
            )
        }.sortedBy { it.daysUntilStockout }

        val alertCount = predictions.count { it.urgencyLevel == UrgencyLevel.CRITICAL || it.urgencyLevel == UrgencyLevel.HIGH }

        val summary = if (alertCount > 0) {
            "$alertCount product(s) require urgent restocking to avoid running out during business hours."
        } else {
            "All inventory items have healthy stock buffers for upcoming sales."
        }

        return InventoryPredictionResult(
            predictions = predictions,
            itemsRequiringAttentionCount = alertCount,
            overallSummary = summary
        )
    }
}
