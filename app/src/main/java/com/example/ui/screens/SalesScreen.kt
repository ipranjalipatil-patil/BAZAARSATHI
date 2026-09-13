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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
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
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.ui.localization.StringsDefinition
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.IndigoContainerLight
import com.example.ui.theme.IndigoUpi
import com.example.ui.theme.SaffronContainerLight
import com.example.ui.theme.SaffronSecondary
import com.example.ui.theme.TealContainerLight
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SalesScreen(
    transactions: List<TransactionEntity>,
    strings: StringsDefinition,
    onAddSale: (amount: Double, method: PaymentMethod, product: String, category: String, desc: String) -> Unit,
    onUpdateSale: (TransactionEntity) -> Unit,
    onDeleteSale: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, CASH, UPI
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSale by remember { mutableStateOf<TransactionEntity?>(null) }

    val filteredTransactions = transactions.filter {
        val matchesSearch = it.product.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "CASH" -> it.paymentMethod == PaymentMethod.CASH.name
            "UPI" -> it.paymentMethod == PaymentMethod.UPI.name
            else -> true
        }
        matchesSearch && matchesFilter
    }

    val totalFilteredRevenue = filteredTransactions.sumOf { it.amount }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("sales_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title & Total Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.salesTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${filteredTransactions.size} recorded sales",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TealContainerLight
                ) {
                    Text(
                        text = "Total: ₹${totalFilteredRevenue.roundToInt()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.searchSales) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sales_search_input"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips: All, Cash, UPI
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SalesFilterChip(
                    label = "All Sales (${transactions.size})",
                    isSelected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" }
                )
                SalesFilterChip(
                    label = "Cash (${transactions.count { it.paymentMethod == PaymentMethod.CASH.name }})",
                    isSelected = selectedFilter == "CASH",
                    onClick = { selectedFilter = "CASH" }
                )
                SalesFilterChip(
                    label = "UPI (${transactions.count { it.paymentMethod == PaymentMethod.UPI.name }})",
                    isSelected = selectedFilter == "UPI",
                    onClick = { selectedFilter = "UPI" }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transactions List
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No sales matching '$searchQuery'" else "No sales recorded yet. Tap + to record sale!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        SaleItemCard(
                            transaction = tx,
                            formattedDate = dateFormat.format(Date(tx.date)),
                            onEdit = { editingSale = tx },
                            onDelete = { onDeleteSale(tx.id) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Sale
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
                .testTag("add_sale_fab"),
            containerColor = TealPrimary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = strings.addSale)
        }
    }

    // Add Sale Dialog
    if (showAddDialog) {
        SaleEntryDialog(
            strings = strings,
            onDismiss = { showAddDialog = false },
            onSave = { amt, method, prod, cat, desc ->
                onAddSale(amt, method, prod, cat, desc)
                showAddDialog = false
            }
        )
    }

    // Edit Sale Dialog
    if (editingSale != null) {
        val current = editingSale!!
        SaleEntryDialog(
            strings = strings,
            initialAmount = current.amount.toInt().toString(),
            initialProduct = current.product,
            initialCategory = current.category,
            initialDescription = current.description,
            initialMethod = if (current.paymentMethod == PaymentMethod.UPI.name) PaymentMethod.UPI else PaymentMethod.CASH,
            onDismiss = { editingSale = null },
            onSave = { amt, method, prod, cat, desc ->
                onUpdateSale(
                    current.copy(
                        amount = amt,
                        paymentMethod = method.name,
                        product = prod,
                        category = cat,
                        description = desc
                    )
                )
                editingSale = null
            }
        )
    }
}

@Composable
private fun SalesFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
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
private fun SaleItemCard(
    transaction: TransactionEntity,
    formattedDate: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(TealContainerLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₹",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.product,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${transaction.category} • $formattedDate",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (transaction.description.isNotBlank()) {
                        Text(
                            text = transaction.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+₹${transaction.amount.roundToInt()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenSuccess
                )
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (transaction.paymentMethod == PaymentMethod.UPI.name) IndigoContainerLight else SaffronContainerLight
                ) {
                    Text(
                        text = transaction.paymentMethod,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.paymentMethod == PaymentMethod.UPI.name) IndigoUpi else SaffronSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row {
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
fun SaleEntryDialog(
    strings: StringsDefinition,
    initialAmount: String = "",
    initialProduct: String = "",
    initialCategory: String = "Counter Sale",
    initialDescription: String = "",
    initialMethod: PaymentMethod = PaymentMethod.CASH,
    onDismiss: () -> Unit,
    onSave: (amount: Double, method: PaymentMethod, product: String, category: String, desc: String) -> Unit
) {
    var amount by remember { mutableStateOf(initialAmount) }
    var product by remember { mutableStateOf(initialProduct) }
    var category by remember { mutableStateOf(initialCategory) }
    var description by remember { mutableStateOf(initialDescription) }
    var paymentMethod by remember { mutableStateOf(initialMethod) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialAmount.isBlank()) strings.addSale else strings.editSale,
                fontWeight = FontWeight.Bold
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (amt == null || amt <= 0.0) {
                        errorText = "Please enter a valid sale amount."
                    } else if (product.isBlank()) {
                        errorText = "Please specify the item sold."
                    } else {
                        onSave(amt, paymentMethod, product, category, description)
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Amount Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("20", "50", "100", "200", "500").forEach { quickAmt ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { amount = quickAmt },
                            shape = RoundedCornerShape(8.dp),
                            color = TealContainerLight
                        ) {
                            Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                Text("₹$quickAmt", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        errorText = null
                    },
                    label = { Text(strings.saleAmount) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("sale_amount_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = product,
                    onValueChange = {
                        product = it
                        errorText = null
                    },
                    label = { Text(strings.productItem) },
                    placeholder = { Text("e.g. Masala Chai (5 cups), Samosa") },
                    modifier = Modifier.fillMaxWidth().testTag("sale_product_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Payment Method Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { paymentMethod = PaymentMethod.CASH },
                        shape = RoundedCornerShape(10.dp),
                        color = if (paymentMethod == PaymentMethod.CASH) SaffronSecondary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "💵 Cash",
                                fontWeight = FontWeight.Bold,
                                color = if (paymentMethod == PaymentMethod.CASH) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { paymentMethod = PaymentMethod.UPI },
                        shape = RoundedCornerShape(10.dp),
                        color = if (paymentMethod == PaymentMethod.UPI) IndigoUpi else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "📱 UPI",
                                fontWeight = FontWeight.Bold,
                                color = if (paymentMethod == PaymentMethod.UPI) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(strings.description) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorText != null) {
                    Text(text = errorText!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        }
    )
}
