package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PaymentMethod {
    CASH,
    UPI
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val vendorId: String = "vendor_default",
    val amount: Double,
    val paymentMethod: String = PaymentMethod.CASH.name, // CASH or UPI
    val category: String = "General",
    val product: String = "Item",
    val description: String = "",
    val date: Long = System.currentTimeMillis(), // Timestamp ms
    val isSynced: Boolean = false
)
