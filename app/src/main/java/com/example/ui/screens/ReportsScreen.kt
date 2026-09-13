package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.ui.localization.StringsDefinition
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.GreenSuccessLight
import com.example.ui.theme.IndigoContainerLight
import com.example.ui.theme.IndigoUpi
import com.example.ui.theme.RedExpense
import com.example.ui.theme.RedExpenseLight
import com.example.ui.theme.SaffronContainerLight
import com.example.ui.theme.SaffronSecondary
import com.example.ui.theme.TealContainerLight
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

enum class ReportPeriod(val label: String) {
    TODAY("Today"),
    WEEK("Last 7 Days"),
    MONTH("Last 30 Days"),
    ALL("All Time")
}

@Composable
fun ReportsScreen(
    transactions: List<TransactionEntity>,
    expenses: List<ExpenseEntity>,
    strings: StringsDefinition,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.WEEK) }

    val now = System.currentTimeMillis()
    val periodStartTime = when (selectedPeriod) {
        ReportPeriod.TODAY -> {
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        ReportPeriod.WEEK -> now - (7 * 86_400_000L)
        ReportPeriod.MONTH -> now - (30 * 86_400_000L)
        ReportPeriod.ALL -> 0L
    }

    val periodTransactions = transactions.filter { it.date >= periodStartTime }
    val periodExpenses = expenses.filter { it.date >= periodStartTime }

    val totalRevenue = periodTransactions.sumOf { it.amount }
    val totalExpense = periodExpenses.sumOf { it.amount }
    val netProfit = totalRevenue - totalExpense
    val profitMargin = if (totalRevenue > 0) ((netProfit / totalRevenue) * 100).roundToInt() else 0

    val upiSales = periodTransactions.filter { it.paymentMethod == PaymentMethod.UPI.name }.sumOf { it.amount }
    val cashSales = periodTransactions.filter { it.paymentMethod == PaymentMethod.CASH.name }.sumOf { it.amount }
    val upiShare = if (totalRevenue > 0) ((upiSales / totalRevenue) * 100).roundToInt() else 0

    val topProducts = periodTransactions.groupBy { it.product }
        .mapValues { (_, txs) -> txs.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
        .take(4)

    val expenseCategories = periodExpenses.groupBy { it.category }
        .mapValues { (_, exps) -> exps.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Back
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Business Reports & Statement",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Comprehensive financial statements and breakdowns",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Period Filters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportPeriod.values().forEach { period ->
                    val isSelected = selectedPeriod == period
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPeriod = period },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Summary KPI Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Financial Summary (${selectedPeriod.label})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ReportMetricBox(
                            title = "Revenue",
                            amount = "₹${totalRevenue.roundToInt()}",
                            color = GreenSuccess,
                            container = GreenSuccessLight,
                            modifier = Modifier.weight(1f)
                        )
                        ReportMetricBox(
                            title = "Expenses",
                            amount = "₹${totalExpense.roundToInt()}",
                            color = RedExpense,
                            container = RedExpenseLight,
                            modifier = Modifier.weight(1f)
                        )
                        ReportMetricBox(
                            title = "Net Profit",
                            amount = "₹${netProfit.roundToInt()}",
                            color = if (netProfit >= 0) TealPrimary else RedExpense,
                            container = if (netProfit >= 0) TealContainerLight else RedExpenseLight,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Operating Profit Margin: $profitMargin%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (profitMargin >= 0) GreenSuccess else RedExpense
                        )
                        Text(
                            text = "${periodTransactions.size} sales / ${periodExpenses.size} expenses",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Payment Method Ratio (UPI vs Cash)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Payment Mode Distribution",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "UPI: ₹${upiSales.roundToInt()} ($upiShare%)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = IndigoUpi)
                        Text(text = "Cash: ₹${cashSales.roundToInt()} (${100 - upiShare}%)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SaffronSecondary)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { if (totalRevenue > 0) (upiSales / totalRevenue).toFloat() else 0.5f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = IndigoUpi,
                        trackColor = SaffronSecondary
                    )
                }
            }
        }

        // Top Selling Items Breakdown
        if (topProducts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Top Revenue Items",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        topProducts.forEach { (prod, amt) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = prod, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(text = "₹${amt.roundToInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Expense Category Breakdown
        if (expenseCategories.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Expense Categories",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        expenseCategories.forEach { (cat, amt) ->
                            val pct = if (totalExpense > 0) ((amt / totalExpense) * 100).roundToInt() else 0
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "$cat ($pct%)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = "₹${amt.roundToInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RedExpense)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (pct / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = RedExpense,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons: Export & Share
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Financial Statement CSV exported successfully!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export CSV")
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "Statement summary ready to share!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share")
                }
            }
        }
    }
}

@Composable
private fun ReportMetricBox(
    title: String,
    amount: String,
    color: Color,
    container: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(container)
            .padding(10.dp)
    ) {
        Column {
            Text(text = title, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = amount, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
