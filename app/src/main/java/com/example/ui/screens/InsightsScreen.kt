package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.BusinessInsight
import com.example.ai.CashflowPredictionResult
import com.example.ai.FinancialReadinessScore
import com.example.ai.InventoryPredictionItem
import com.example.ai.InventoryPredictionResult
import com.example.ai.ReadinessCategory
import com.example.ai.UrgencyLevel
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
import kotlin.math.roundToInt

@Composable
fun InsightsScreen(
    cashflowPrediction: CashflowPredictionResult,
    inventoryPrediction: InventoryPredictionResult,
    businessInsights: List<BusinessInsight>,
    financialReadiness: FinancialReadinessScore,
    strings: StringsDefinition,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("insights_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(TealContainerLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.insightsTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Automated predictive forecasting & financial health analytics",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. AI CASH-FLOW PREDICTION CARD
        item {
            CashflowPredictionCard(
                prediction = cashflowPrediction,
                strings = strings
            )
        }

        // 2. FINANCIAL READINESS SCORE CARD (0-100)
        item {
            FinancialReadinessCard(
                score = financialReadiness,
                strings = strings
            )
        }

        // 3. INVENTORY STOCKOUT PREDICTION CARD
        item {
            InventoryPredictionCard(
                result = inventoryPrediction,
                strings = strings
            )
        }

        // 4. ACTIONABLE BUSINESS INSIGHTS
        item {
            Text(
                text = "Key Business Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(businessInsights) { insight ->
            BusinessInsightItemCard(insight = insight)
        }

        // 5. MANDATORY FINANCIAL DISCLAIMER
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Disclaimer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.disclaimerText,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CashflowPredictionCard(
    prediction: CashflowPredictionResult,
    strings: StringsDefinition
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.cashflowPredictionTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (prediction.hasEnoughData) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TealContainerLight
                    ) {
                        Text(
                            text = "${prediction.confidenceScore}% Confidence",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!prediction.hasEnoughData) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = prediction.explanation,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Next 7 Days Net
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(TealContainerLight)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = strings.expectedCashflowNextWeek,
                                fontSize = 11.sp,
                                color = TealPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${prediction.next7DaysCashflow.roundToInt()}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                        }
                    }

                    // Next Day Net
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(IndigoContainerLight)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = strings.expectedNextDay,
                                fontSize = 11.sp,
                                color = IndigoUpi,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${prediction.nextDayCashflow.roundToInt()}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoUpi
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Explainable Rationale Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Model Rationale:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = prediction.explanation,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialReadinessCard(
    score: FinancialReadinessScore,
    strings: StringsDefinition
) {
    val tierColor = when (score.ratingCategory) {
        ReadinessCategory.NEEDS_IMPROVEMENT -> RedExpense
        ReadinessCategory.FAIR -> SaffronSecondary
        ReadinessCategory.GOOD -> TealPrimary
        ReadinessCategory.STRONG -> GreenSuccess
    }

    val tierContainer = when (score.ratingCategory) {
        ReadinessCategory.NEEDS_IMPROVEMENT -> RedExpenseLight
        ReadinessCategory.FAIR -> SaffronContainerLight
        ReadinessCategory.GOOD -> TealContainerLight
        ReadinessCategory.STRONG -> GreenSuccessLight
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.financialReadinessTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.financialReadinessSubtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = tierContainer
                ) {
                    Text(
                        text = score.ratingCategory.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = tierColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Big Score Display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${score.totalScore}",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = tierColor
                )
                Text(
                    text = " / 100",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))
                LinearProgressIndicator(
                    progress = { score.totalScore / 100f },
                    modifier = Modifier
                        .width(140.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = tierColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Factor Pillars
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                score.factors.forEach { factor ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (factor.isPositive) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (factor.isPositive) GreenSuccess else SaffronSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = factor.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "${factor.score}/${factor.maxScore}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryPredictionCard(
    result: InventoryPredictionResult,
    strings: StringsDefinition
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        tint = SaffronSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.inventoryPredictionTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (result.itemsRequiringAttentionCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SaffronContainerLight
                    ) {
                        Text(
                            text = "${result.itemsRequiringAttentionCount} Alert(s)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result.overallSummary,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                result.predictions.take(4).forEach { item ->
                    StockoutItemRow(item = item, strings = strings)
                }
            }
        }
    }
}

@Composable
private fun StockoutItemRow(
    item: InventoryPredictionItem,
    strings: StringsDefinition
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.item.productName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.recommendationText,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = when (item.urgencyLevel) {
                    UrgencyLevel.CRITICAL -> RedExpenseLight
                    UrgencyLevel.HIGH -> SaffronContainerLight
                    UrgencyLevel.MEDIUM -> IndigoContainerLight
                    UrgencyLevel.LOW -> GreenSuccessLight
                }
            ) {
                Text(
                    text = "${item.daysUntilStockout.toInt()} days left",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (item.urgencyLevel) {
                        UrgencyLevel.CRITICAL -> RedExpense
                        UrgencyLevel.HIGH -> SaffronSecondary
                        UrgencyLevel.MEDIUM -> IndigoUpi
                        UrgencyLevel.LOW -> GreenSuccess
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun BusinessInsightItemCard(insight: BusinessInsight) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(TealContainerLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = insight.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (insight.metricValue != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TealContainerLight
                ) {
                    Text(
                        text = insight.metricValue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
