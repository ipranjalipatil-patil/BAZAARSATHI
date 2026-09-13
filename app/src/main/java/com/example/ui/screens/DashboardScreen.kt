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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.InventoryPredictionResult
import com.example.data.model.ExpenseEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.VendorProfile
import com.example.ui.components.CashVsUpiSection
import com.example.ui.components.FinancialTrendChartCard
import com.example.ui.components.TodaySummaryHeroCard
import com.example.ui.localization.StringsDefinition
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.IndigoContainerLight
import com.example.ui.theme.IndigoUpi
import com.example.ui.theme.RedExpense
import com.example.ui.theme.SaffronContainerLight
import com.example.ui.theme.SaffronSecondary
import com.example.ui.theme.TealContainerLight
import com.example.ui.theme.TealPrimary
import com.example.viewmodel.TodaySummary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    vendorProfile: VendorProfile,
    todaySummary: TodaySummary,
    transactions: List<TransactionEntity>,
    expenses: List<ExpenseEntity>,
    inventoryPrediction: InventoryPredictionResult,
    strings: StringsDefinition,
    onOpenVoiceDialog: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> strings.greetingMorning
        hour < 17 -> strings.greetingAfternoon
        else -> strings.greetingEvening
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$greeting, ${vendorProfile.fullName.split(" ").firstOrNull() ?: "Merchant"}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${vendorProfile.businessName} • ${vendorProfile.businessType}",
                        fontSize = 13.sp,
                        color = TealPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateToAssistant,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(TealContainerLight)
                            .testTag("dashboard_ai_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant",
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("dashboard_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Low Stock Alert Banner (if any)
        if (inventoryPrediction.itemsRequiringAttentionCount > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToInventory),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SaffronContainerLight
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = SaffronSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Inventory Stock Alert",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronSecondary
                            )
                            Text(
                                text = inventoryPrediction.overallSummary,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Today's Hero Summary Card
        item {
            TodaySummaryHeroCard(
                summary = todaySummary,
                strings = strings
            )
        }

        // Cash vs UPI Split
        item {
            CashVsUpiSection(
                cashRevenue = todaySummary.cashRevenue,
                upiRevenue = todaySummary.upiRevenue,
                strings = strings
            )
        }

        // Quick Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    label = strings.addSale,
                    icon = Icons.Default.Add,
                    color = TealPrimary,
                    container = TealContainerLight,
                    onClick = onNavigateToSales,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    label = strings.addExpense,
                    icon = Icons.Default.Add,
                    color = RedExpense,
                    container = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    onClick = onNavigateToExpenses,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    label = "Voice Entry",
                    icon = Icons.Default.Mic,
                    color = SaffronSecondary,
                    container = SaffronContainerLight,
                    onClick = onOpenVoiceDialog,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Financial Trend Chart (7D, 30D, 3M)
        item {
            FinancialTrendChartCard(
                transactions = transactions,
                expenses = expenses,
                strings = strings
            )
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.recentTransactions,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onNavigateToSales) {
                    Text(text = strings.viewAll, color = TealPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Recent Transactions (Up to 5)
        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.noTransactionsYet,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            items(transactions.take(5)) { tx ->
                TransactionRowCard(
                    item = tx.product,
                    amount = "₹${tx.amount.roundToInt()}",
                    paymentMethod = tx.paymentMethod,
                    category = tx.category,
                    time = timeFormat.format(Date(tx.date)),
                    isSale = true
                )
            }
        }
    }
}

@Composable
fun TransactionRowCard(
    item: String,
    amount: String,
    paymentMethod: String,
    category: String,
    time: String,
    isSale: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSale) TealContainerLight else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSale) "₹+" else "₹-",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSale) TealPrimary else RedExpense
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "$category • $time",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isSale) "+" else "-") + amount,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSale) GreenSuccess else RedExpense
                )
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (paymentMethod == PaymentMethod.UPI.name) IndigoContainerLight else SaffronContainerLight
                ) {
                    Text(
                        text = paymentMethod,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (paymentMethod == PaymentMethod.UPI.name) IndigoUpi else SaffronSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    container: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = container
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
                maxLines = 1
            )
        }
    }
}
