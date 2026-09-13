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
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.ui.localization.StringsDefinition
import com.example.ui.theme.RedExpense
import com.example.ui.theme.RedExpenseLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ExpensesScreen(
    expenses: List<ExpenseEntity>,
    strings: StringsDefinition,
    onAddExpense: (amount: Double, category: String, description: String) -> Unit,
    onUpdateExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    val filteredExpenses = expenses.filter {
        val matchesSearch = it.category.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "ALL" || it.category == selectedCategory
        matchesSearch && matchesCategory
    }

    val totalExpenseSum = filteredExpenses.sumOf { it.amount }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("expenses_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title & Total Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.expensesTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${filteredExpenses.size} recorded outflows",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RedExpenseLight
                ) {
                    Text(
                        text = "Total: ₹${totalExpenseSum.roundToInt()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RedExpense,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.searchExpenses) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("expense_search_input"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Horizontal Scroll Filter
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    CategoryPillChip(
                        label = "All (${expenses.size})",
                        isSelected = selectedCategory == "ALL",
                        onClick = { selectedCategory = "ALL" }
                    )
                }
                items(ExpenseCategory.values()) { cat ->
                    val count = expenses.count { it.category == cat.label }
                    CategoryPillChip(
                        label = "${cat.label} ($count)",
                        isSelected = selectedCategory == cat.label,
                        onClick = { selectedCategory = cat.label }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expenses List
            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No expenses found matching '$searchQuery'" else "No expenses recorded yet. Tap + to record expenses!",
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
                    items(filteredExpenses, key = { it.id }) { exp ->
                        ExpenseItemCard(
                            expense = exp,
                            formattedDate = dateFormat.format(Date(exp.date)),
                            onEdit = { editingExpense = exp },
                            onDelete = { onDeleteExpense(exp.id) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Expense
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
                .testTag("add_expense_fab"),
            containerColor = RedExpense,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = strings.addExpense)
        }
    }

    if (showAddDialog) {
        ExpenseEntryDialog(
            strings = strings,
            onDismiss = { showAddDialog = false },
            onSave = { amt, cat, desc ->
                onAddExpense(amt, cat, desc)
                showAddDialog = false
            }
        )
    }

    if (editingExpense != null) {
        val current = editingExpense!!
        ExpenseEntryDialog(
            strings = strings,
            initialAmount = current.amount.toInt().toString(),
            initialCategory = current.category,
            initialDescription = current.description,
            onDismiss = { editingExpense = null },
            onSave = { amt, cat, desc ->
                onUpdateExpense(
                    current.copy(
                        amount = amt,
                        category = cat,
                        description = desc
                    )
                )
                editingExpense = null
            }
        )
    }
}

@Composable
private fun CategoryPillChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) RedExpense else MaterialTheme.colorScheme.surfaceVariant
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
private fun ExpenseItemCard(
    expense: ExpenseEntity,
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
                        .background(RedExpenseLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₹",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = RedExpense
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = expense.category,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (expense.description.isNotBlank()) {
                        Text(
                            text = expense.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "-₹${expense.amount.roundToInt()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedExpense
                )

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
fun ExpenseEntryDialog(
    strings: StringsDefinition,
    initialAmount: String = "",
    initialCategory: String = ExpenseCategory.RAW_MATERIALS.label,
    initialDescription: String = "",
    onDismiss: () -> Unit,
    onSave: (amount: Double, category: String, description: String) -> Unit
) {
    var amount by remember { mutableStateOf(initialAmount) }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var description by remember { mutableStateOf(initialDescription) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialAmount.isBlank()) strings.addExpense else strings.editExpense,
                fontWeight = FontWeight.Bold
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (amt == null || amt <= 0.0) {
                        errorText = "Please enter a valid expense amount."
                    } else {
                        onSave(amt, selectedCategory, description)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RedExpense)
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
                    listOf("50", "100", "200", "500", "1000").forEach { quickAmt ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { amount = quickAmt },
                            shape = RoundedCornerShape(8.dp),
                            color = RedExpenseLight
                        ) {
                            Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                Text("₹$quickAmt", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RedExpense)
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
                    label = { Text(strings.expenseAmount) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("expense_amount_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Select Category:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Category selection grid
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ExpenseCategory.values()) { cat ->
                        Surface(
                            modifier = Modifier.clickable { selectedCategory = cat.label },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedCategory == cat.label) RedExpense else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = cat.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedCategory == cat.label) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(strings.description) },
                    placeholder = { Text("e.g. 5 Litres Buffalo Milk, Gas refill") },
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
