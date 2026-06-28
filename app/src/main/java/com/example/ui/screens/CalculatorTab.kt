package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TradingViewModel
import java.util.Locale

@Composable
fun CalculatorTab(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val loggedTrades by viewModel.allTradeEntries.collectAsState()
    
    val calcWins by viewModel.calcWins.collectAsState()
    val calcLosses by viewModel.calcLosses.collectAsState()

    val scrollState = rememberScrollState()

    // High precision computation
    val winsDouble = calcWins.toDoubleOrNull() ?: 0.0
    val lossesDouble = calcLosses.toDoubleOrNull() ?: 0.0
    val totalTrades = winsDouble + lossesDouble

    val winRatePercentage = remember(winsDouble, lossesDouble, totalTrades) {
        if (totalTrades > 0) {
            (winsDouble / totalTrades) * 100.0
        } else {
            0.0
        }
    }

    val animatedWinProgress by animateFloatAsState(
        targetValue = (winRatePercentage / 100.0).toFloat(),
        animationSpec = tween(durationMillis = 600)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
            .testTag("calculator_tab_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Win Rate Calculator",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Calculate exact trading win percentages instantly across hundreds or thousands of trades.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        )

        // Visual gauge card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circular Indicator
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(150.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { animatedWinProgress },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 12.dp,
                        color = when {
                            winRatePercentage >= 60.0 -> Color(0xFF2E7D32) // Excellent Win Green
                            winRatePercentage >= 40.0 -> Color(0xFFF57C00) // Average Amber
                            else -> Color(0xFFC62828) // Bad Red
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${String.format(Locale.getDefault(), "%.1f", winRatePercentage)}%",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Win Percentage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Summary Stats text
                Text(
                    text = "Out of ${totalTrades.toInt()} total trade setups, you executed ${winsDouble.toInt()} winning positions successfully.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        // Inputs Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Enter Performance Figures",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Wins input
                OutlinedTextField(
                    value = calcWins,
                    onValueChange = { viewModel.updateCalcWins(it) },
                    label = { Text("Number of Wins") },
                    placeholder = { Text("e.g. 52") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calc_wins_input")
                )

                // Losses input
                OutlinedTextField(
                    value = calcLosses,
                    onValueChange = { viewModel.updateCalcLosses(it) },
                    label = { Text("Number of Losses") },
                    placeholder = { Text("48") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calc_losses_input")
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Import option
                Button(
                    onClick = {
                        val loggedWins = loggedTrades.count { it.profitOrLoss > 0 }
                        val loggedLosses = loggedTrades.count { it.profitOrLoss < 0 }
                        viewModel.updateCalcWins(loggedWins.toString())
                        viewModel.updateCalcLosses(loggedLosses.toString())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_journal_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Logged Journal Stats")
                }
            }
        }

        // Educational Advice Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "The Law of Large Numbers",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "To find your true statistical edge, analyze win-rate frequencies over sample sizes exceeding 100+ trades. A 50% win rate can yield massive profits when paired with a disciplined 1:2.0 risk-reward profile!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
