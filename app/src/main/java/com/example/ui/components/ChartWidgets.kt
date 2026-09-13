package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseEntity
import com.example.data.model.TransactionEntity
import com.example.ui.localization.StringsDefinition
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedExpense
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

enum class TrendTimeframe {
    DAYS_7,
    DAYS_30,
    MONTHS_3
}

@Composable
fun FinancialTrendChartCard(
    transactions: List<TransactionEntity>,
    expenses: List<ExpenseEntity>,
    strings: StringsDefinition,
    modifier: Modifier = Modifier
) {
    var selectedTimeframe by remember { mutableStateOf(TrendTimeframe.DAYS_7) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header & Timeframe Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.trendChart,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Revenue vs Expenses",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Timeframe Selector Chips
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp)
                ) {
                    TimeframePill(
                        label = "7D",
                        isSelected = selectedTimeframe == TrendTimeframe.DAYS_7,
                        onClick = { selectedTimeframe = TrendTimeframe.DAYS_7 }
                    )
                    TimeframePill(
                        label = "30D",
                        isSelected = selectedTimeframe == TrendTimeframe.DAYS_30,
                        onClick = { selectedTimeframe = TrendTimeframe.DAYS_30 }
                    )
                    TimeframePill(
                        label = "3M",
                        isSelected = selectedTimeframe == TrendTimeframe.MONTHS_3,
                        onClick = { selectedTimeframe = TrendTimeframe.MONTHS_3 }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legends
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartLegendItem(color = GreenSuccess, label = strings.revenue)
                ChartLegendItem(color = RedExpense, label = strings.expenses)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Canvas
            val chartData = remember(transactions, expenses, selectedTimeframe) {
                aggregateChartData(transactions, expenses, selectedTimeframe)
            }

            if (chartData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No financial activity recorded in this period.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                CanvasChart(
                    data = chartData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                )
            }
        }
    }
}

@Composable
private fun TimeframePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) TealPrimary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChartLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class ChartBarData(
    val label: String,
    val revenue: Double,
    val expense: Double
)

private fun aggregateChartData(
    transactions: List<TransactionEntity>,
    expenses: List<ExpenseEntity>,
    timeframe: TrendTimeframe
): List<ChartBarData> {
    val count = when (timeframe) {
        TrendTimeframe.DAYS_7 -> 7
        TrendTimeframe.DAYS_30 -> 6 // 6 blocks of 5 days
        TrendTimeframe.MONTHS_3 -> 6 // 6 bi-weekly periods
    }

    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val shortDateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    val result = mutableListOf<ChartBarData>()

    val cal = Calendar.getInstance()

    if (timeframe == TrendTimeframe.DAYS_7) {
        for (i in (count - 1) downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            val dayStart = c.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val dayEnd = dayStart + 86_400_000L

            val rev = transactions.filter { it.date in dayStart until dayEnd }.sumOf { it.amount }
            val exp = expenses.filter { it.date in dayStart until dayEnd }.sumOf { it.amount }
            val label = dayFormat.format(Date(dayStart))

            result.add(ChartBarData(label, rev, exp))
        }
    } else {
        val daysPerBucket = if (timeframe == TrendTimeframe.DAYS_30) 5 else 15
        for (i in (count - 1) downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -(i * daysPerBucket))
            val bucketEnd = c.timeInMillis
            val bucketStart = bucketEnd - (daysPerBucket * 86_400_000L)

            val rev = transactions.filter { it.date in bucketStart..bucketEnd }.sumOf { it.amount }
            val exp = expenses.filter { it.date in bucketStart..bucketEnd }.sumOf { it.amount }
            val label = shortDateFormat.format(Date(bucketStart))

            result.add(ChartBarData(label, rev, exp))
        }
    }

    return result
}

@Composable
private fun CanvasChart(
    data: List<ChartBarData>,
    modifier: Modifier = Modifier
) {
    val primaryColor = GreenSuccess
    val expenseColor = RedExpense
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val maxVal = max(
                100.0,
                data.maxOfOrNull { max(it.revenue, it.expense) } ?: 100.0
            )

            val width = size.width
            val height = size.height
            val barSpacing = width / data.size
            val barWidth = barSpacing * 0.28f

            // Grid lines (3 horizontal guide lines)
            for (level in 1..3) {
                val y = height * (level / 4f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw bars
            data.forEachIndexed { index, item ->
                val centerX = (index * barSpacing) + (barSpacing / 2f)

                // Revenue bar (left)
                val revHeight = ((item.revenue / maxVal) * (height - 10f)).toFloat()
                if (revHeight > 2f) {
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(centerX - barWidth - 2f, height - revHeight),
                        size = Size(barWidth, revHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }

                // Expense bar (right)
                val expHeight = ((item.expense / maxVal) * (height - 10f)).toFloat()
                if (expHeight > 2f) {
                    drawRoundRect(
                        color = expenseColor,
                        topLeft = Offset(centerX + 2f, height - expHeight),
                        size = Size(barWidth, expHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }

        // X-Axis Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            data.forEach {
                Text(
                    text = it.label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
