package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val vendorId: String = "vendor_default",
    val productName: String,
    val category: String = "Raw Material",
    val quantity: Double,
    val unit: String = "units",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val minimumStock: Double = 5.0,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isOutOfStock: Boolean get() = quantity <= 0.0
    val isLowStock: Boolean get() = quantity > 0.0 && quantity <= minimumStock
}
