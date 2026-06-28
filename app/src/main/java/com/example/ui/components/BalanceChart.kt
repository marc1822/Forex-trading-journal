package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TradeEntry
import java.util.Locale

@Composable
fun BalanceChart(
    initialBalance: Double,
    tradeEntries: List<TradeEntry>,
    modifier: Modifier = Modifier
) {
    // Process trade entries chronologically to compute progressive balance points
    val balancePoints = remember(initialBalance, tradeEntries) {
        val sorted = tradeEntries.sortedWith(compareBy({ it.date }, { it.id }))
        val list = mutableListOf<Float>()
        list.add(initialBalance.toFloat())
        var tempBalance = initialBalance
        sorted.forEach {
            tempBalance += it.profitOrLoss
            list.add(tempBalance.toFloat())
        }
        list
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("balance_chart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Account Balance Curve",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            val finalBalance = balancePoints.last()
            val netProfit = finalBalance - initialBalance
            val percentChange = (netProfit / initialBalance) * 100

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = "$${String.format(Locale.getDefault(), "%.2f", finalBalance)}",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                val profitColor = if (netProfit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                val prefix = if (netProfit >= 0) "+" else ""
                Text(
                    text = "$prefix$${String.format(Locale.getDefault(), "%.2f", netProfit)} ($prefix${String.format(Locale.getDefault(), "%.2f", percentChange)}%)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = profitColor,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (tradeEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Start logging trades in your journal to display your balance growth path!",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                val primaryColor = MaterialTheme.colorScheme.primary
                val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("balance_canvas")
                ) {
                    val width = size.width
                    val height = size.height

                    val maxVal = (balancePoints.maxOrNull() ?: 0f) * 1.05f
                    val minVal = (balancePoints.minOrNull() ?: 0f) * 0.95f
                    val range = if (maxVal == minVal) 1f else (maxVal - minVal)

                    val pointsCount = balancePoints.size
                    val stepX = width / (pointsCount - 1).coerceAtLeast(1)

                    // Draw Horizontal Grid lines (3 levels)
                    val gridLinesCount = 3
                    for (i in 0..gridLinesCount) {
                        val y = height * (i.toFloat() / gridLinesCount)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // Build line path
                    val path = Path()
                    val fillPath = Path()

                    balancePoints.forEachIndexed { idx, valPoint ->
                        val x = idx * stepX
                        // Normalize y coordinate: top is 0, bottom is height
                        val y = height - ((valPoint - minVal) / range * height)

                        if (idx == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }

                        if (idx == pointsCount - 1) {
                            fillPath.lineTo(x, height)
                            fillPath.close()
                        }
                    }

                    // Draw Gradient Fill beneath the growth curve
                    val gradient = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.3f),
                            primaryColor.copy(alpha = 0.0f)
                        ),
                        startY = 0f,
                        endY = height
                    )
                    drawPath(
                        path = fillPath,
                        brush = gradient
                    )

                    // Draw main line path
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(
                            width = 3.dp.toPx()
                        )
                    )

                    // Draw points/nodes
                    balancePoints.forEachIndexed { idx, valPoint ->
                        val x = idx * stepX
                        val y = height - ((valPoint - minVal) / range * height)
                        
                        // Highlight the final point and initial point or show small circles
                        if (idx == 0 || idx == pointsCount - 1) {
                            drawCircle(
                                color = primaryColor,
                                radius = 5.dp.toPx(),
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = Offset(x, y)
                            )
                        } else if (pointsCount < 20) {
                            // Only draw node points if there are few items to keep it clean
                            drawCircle(
                                color = primaryColor.copy(alpha = 0.8f),
                                radius = 3.2.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }
        }
    }
}
