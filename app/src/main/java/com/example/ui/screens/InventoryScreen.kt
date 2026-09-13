package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryItemEntity
import com.example.ui.localization.StringsDefinition
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.GreenSuccessLight
import com.example.ui.theme.RedExpense
import com.example.ui.theme.RedExpenseLight
import com.example.ui.theme.SaffronContainerLight
import com.example.ui.theme.SaffronSecondary
import com.example.ui.theme.TealContainerLight
import com.example.ui.theme.TealPrimary
import kotlin.math.roundToInt

@Composable
fun InventoryScreen(
    inventory: List<InventoryItemEntity>,
    strings: StringsDefinition,
    onAddItem: (name: String, category: String, qty: Double, unit: String, buyPrice: Double, sellPrice: Double, minStock: Double) -> Unit,
    onUpdateItem: (InventoryItemEntity) -> Unit,
    onAdjustStock: (id: Long, delta: Double) -> Unit,
    onDeleteItem: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, LOW, OUT
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<InventoryItemEntity?>(null) }

    val filteredInventory = inventory.filter {
        val matchesSearch = it.productName.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "LOW" -> it.isLowStock
            "OUT" -> it.isOutOfStock
            else -> true
        }
        matchesSearch && matchesFilter
    }

    val lowStockCount = inventory.count { it.isLowStock }
    val outStockCount = inventory.count { it.isOutOfStock }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("inventory_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title & Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.inventoryTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${inventory.size} managed products",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (lowStockCount > 0) {
                        Surface(shape = RoundedCornerShape(8.dp), color = SaffronContainerLight) {
                            Text(
                                text = "$lowStockCount Low",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (outStockCount > 0) {
                        Surface(shape = RoundedCornerShape(8.dp), color = RedExpenseLight) {
                            Text(
                                text = "$outStockCount Out",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RedExpense,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.searchProducts) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("inventory_search_input"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips: All, Low Stock, Out of Stock
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                InventoryFilterPill(
                    label = "All Items (${inventory.size})",
                    isSelected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" }
                )
                InventoryFilterPill(
                    label = "Low Stock ($lowStockCount)",
                    isSelected = selectedFilter == "LOW",
                    onClick = { selectedFilter = "LOW" }
                )
                InventoryFilterPill(
                    label = "Out of Stock ($outStockCount)",
                    isSelected = selectedFilter == "OUT",
                    onClick = { selectedFilter = "OUT" }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Inventory List
            if (filteredInventory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No products match '$searchQuery'" else "No inventory items added yet. Tap + to add stock!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredInventory, key = { it.id }) { item ->
                        InventoryItemCard(
                            item = item,
                            strings = strings,
                            onIncrement = { onAdjustStock(item.id, 1.0) },
                            onDecrement = {
                                if (item.quantity > 0) {
                                    onAdjustStock(item.id, -1.0)
                                }
                            },
                            onEdit = { editingItem = item },
                            onDelete = { onDeleteItem(item.id) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Product
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
                .testTag("add_inventory_fab"),
            containerColor = TealPrimary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = strings.addProduct)
        }
    }

    if (showAddDialog) {
        ProductEntryDialog(
            strings = strings,
            onDismiss = { showAddDialog = false },
            onSave = { name, cat, qty, unit, buy, sell, minS ->
                onAddItem(name, cat, qty, unit, buy, sell, minS)
                showAddDialog = false
            }
        )
    }

    if (editingItem != null) {
        val current = editingItem!!
        ProductEntryDialog(
            strings = strings,
            initialName = current.productName,
            initialCategory = current.category,
            initialQty = current.quantity.toString(),
            initialUnit = current.unit,
            initialBuyPrice = current.purchasePrice.toString(),
            initialSellPrice = current.sellingPrice.toString(),
            initialMinStock = current.minimumStock.toString(),
            onDismiss = { editingItem = null },
            onSave = { name, cat, qty, unit, buy, sell, minS ->
                onUpdateItem(
                    current.copy(
                        productName = name,
                        category = cat,
                        quantity = qty,
                        unit = unit,
                        purchasePrice = buy,
                        sellingPrice = sell,
                        minimumStock = minS
                    )
                )
                editingItem = null
            }
        )
    }
}

@Composable
private fun InventoryFilterPill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun InventoryItemCard(
    item: InventoryItemEntity,
    strings: StringsDefinition,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    item.isOutOfStock -> RedExpenseLight
                                    item.isLowStock -> SaffronContainerLight
                                    else -> GreenSuccessLight
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = when {
                                item.isOutOfStock -> RedExpense
                                item.isLowStock -> SaffronSecondary
                                else -> GreenSuccess
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = item.productName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${item.category} • Cost: ₹${item.purchasePrice.roundToInt()} | Sell: ₹${item.sellingPrice.roundToInt()}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        item.isOutOfStock -> RedExpenseLight
                        item.isLowStock -> SaffronContainerLight
                        else -> GreenSuccessLight
                    }
                ) {
                    Text(
                        text = when {
                            item.isOutOfStock -> strings.outOfStock
                            item.isLowStock -> strings.lowStock
                            else -> strings.inStock
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            item.isOutOfStock -> RedExpense
                            item.isLowStock -> SaffronSecondary
                            else -> GreenSuccess
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quantity Control Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stock: ${if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity} ${item.unit} (Min: ${item.minimumStock.toInt()})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Minus Button (with zero prevention)
                    IconButton(
                        onClick = onDecrement,
                        enabled = item.quantity > 0,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Plus Button
                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(TealContainerLight)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = TealPrimary, modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductEntryDialog(
    strings: StringsDefinition,
    initialName: String = "",
    initialCategory: String = "Raw Material",
    initialQty: String = "10",
    initialUnit: String = "kg",
    initialBuyPrice: String = "50",
    initialSellPrice: String = "0",
    initialMinStock: String = "5",
    onDismiss: () -> Unit,
    onSave: (name: String, cat: String, qty: Double, unit: String, buy: Double, sell: Double, minS: Double) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var category by remember { mutableStateOf(initialCategory) }
    var qty by remember { mutableStateOf(initialQty) }
    var unit by remember { mutableStateOf(initialUnit) }
    var buyPrice by remember { mutableStateOf(initialBuyPrice) }
    var sellPrice by remember { mutableStateOf(initialSellPrice) }
    var minStock by remember { mutableStateOf(initialMinStock) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialName.isBlank()) strings.addProduct else strings.editProduct,
                fontWeight = FontWeight.Bold
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = qty.toDoubleOrNull()
                    val buy = buyPrice.toDoubleOrNull() ?: 0.0
                    val sell = sellPrice.toDoubleOrNull() ?: 0.0
                    val minS = minStock.toDoubleOrNull() ?: 5.0
                    if (name.isBlank()) {
                        errorText = "Please enter product name."
                    } else if (q == null || q < 0.0) {
                        errorText = "Please enter valid quantity."
                    } else {
                        onSave(name, category, q, unit, buy, sell, minS)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorText = null
                    },
                    label = { Text(strings.productName) },
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input"),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qty,
                        onValueChange = { qty = it },
                        label = { Text("Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("product_qty_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (kg, L, pcs)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = buyPrice,
                        onValueChange = { buyPrice = it },
                        label = { Text("Cost (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minStock,
                        onValueChange = { minStock = it },
                        label = { Text("Min Alert") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                if (errorText != null) {
                    Text(text = errorText!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        }
    )
}
