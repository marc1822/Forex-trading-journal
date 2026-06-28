package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TradeEntry
import com.example.data.UserProfile
import com.example.ui.TradingViewModel
import com.example.ui.components.BalanceChart
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsTab(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val tradeEntries by viewModel.allTradeEntries.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val profile = userProfile ?: UserProfile()
    val initialBalance = profile.initialBalance

    // Dynamic calculations
    val totalTrades = tradeEntries.size
    val winningTrades = tradeEntries.filter { it.profitOrLoss > 0 }
    val losingTrades = tradeEntries.filter { it.profitOrLoss < 0 }

    val winsCount = winningTrades.size
    val lossesCount = losingTrades.size
    val winRate = if (totalTrades > 0) (winsCount.toDouble() / totalTrades * 100) else 0.0

    val totalProfit = winningTrades.sumOf { it.profitOrLoss }
    val totalLoss = losingTrades.sumOf { it.profitOrLoss }
    val netProfit = totalProfit + totalLoss

    val profitFactor = remember(totalProfit, totalLoss) {
        if (Math.abs(totalLoss) > 0) totalProfit / Math.abs(totalLoss) else totalProfit
    }

    // Monthly analytics grouping
    val monthlyStats = remember(tradeEntries) {
        calculateMonthlyStats(tradeEntries)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("stats_tab_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Text(
                text = "Performance Analytics",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Live calculations of your account growth and monthly analytics.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // 1. Balance Line Chart
        item {
            BalanceChart(
                initialBalance = initialBalance,
                tradeEntries = tradeEntries
            )
        }

        // 2. High level metrics grids
        item {
            Text(
                text = "Key Performance Indicators (KPIs)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "Win Rate",
                    value = "${String.format(Locale.getDefault(), "%.1f", winRate)}%",
                    subValue = "$winsCount Wins / $lossesCount Losses",
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "Profit Factor",
                    value = String.format(Locale.getDefault(), "%.2f", profitFactor),
                    subValue = "Ratio of Gross Gains",
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val profitColor = if (netProfit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                val formatSign = if (netProfit >= 0) "+" else ""
                KpiCard(
                    title = "Net Profit",
                    value = "$formatSign$${String.format(Locale.getDefault(), "%.2f", netProfit)}",
                    subValue = "Total Net Return",
                    textColor = profitColor,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "Total Trades",
                    value = "$totalTrades",
                    subValue = "Logged Positions",
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Monthly analytics table
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Monthly Win Rate Analytics",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Trading effectiveness broken down by monthly milestones.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        if (monthlyStats.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = "No history logged yet. Monthly breakdowns appear automatically once you save trades.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(monthlyStats) { month ->
                MonthlyStatRowItem(month = month)
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subValue: String,
    backgroundColor: Color,
    textColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = if (textColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subValue,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun MonthlyStatRowItem(
    month: MonthlyPerformance,
    modifier: Modifier = Modifier
) {
    val winColor = if (month.winRate >= 50) Color(0xFF2E7D32) else Color(0xFFC62828)
    val profitColor = if (month.netProfit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("month_item_${month.monthKey}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = month.monthName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${month.tradesCount} logged trades (${month.wins} W / ${month.losses} L)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${String.format(Locale.getDefault(), "%.1f", month.winRate)}% Win",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = winColor
                )
                Text(
                    text = if (month.netProfit >= 0) "+$${month.netProfit.toInt()}" else "-$${Math.abs(month.netProfit).toInt()}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = profitColor
                )
            }
        }
    }
}

data class MonthlyPerformance(
    val monthKey: String, // "YYYY-MM"
    val monthName: String, // "June 2026"
    val tradesCount: Int,
    val wins: Int,
    val losses: Int,
    val winRate: Double,
    val netProfit: Double
)

fun calculateMonthlyStats(entries: List<TradeEntry>): List<MonthlyPerformance> {
    val grouped = entries.groupBy {
        if (it.date.length >= 7) it.date.substring(0, 7) else "Other"
    }

    val formatInput = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val formatOutput = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    return grouped.entries.mapNotNull { (key, list) ->
        if (key == "Other") return@mapNotNull null
        
        val dateParsed = formatInput.parse(key)
        val readableName = if (dateParsed != null) formatOutput.format(dateParsed) else key

        val total = list.size
        val wins = list.count { it.profitOrLoss > 0 }
        val losses = list.count { it.profitOrLoss < 0 }
        val net = list.sumOf { it.profitOrLoss }
        val winR = if (total > 0) (wins.toDouble() / total * 100.0) else 0.0

        MonthlyPerformance(
            monthKey = key,
            monthName = readableName,
            tradesCount = total,
            wins = wins,
            losses = losses,
            winRate = winR,
            netProfit = net
        )
    }.sortedByDescending { it.monthKey }
}
