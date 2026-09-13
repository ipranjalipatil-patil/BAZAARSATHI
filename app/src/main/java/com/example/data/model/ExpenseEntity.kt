package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ExpenseCategory(val label: String) {
    RAW_MATERIALS("Raw Materials"),
    TRANSPORTATION("Transportation"),
    RENT("Rent"),
    ELECTRICITY("Electricity"),
    PACKAGING("Packaging"),
    WAGES("Wages"),
    MAINTENANCE("Maintenance"),
    OTHER("Other")
}

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val vendorId: String = "vendor_default",
    val amount: Double,
    val category: String = ExpenseCategory.RAW_MATERIALS.label,
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
