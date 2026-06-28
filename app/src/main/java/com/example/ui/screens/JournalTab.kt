package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TrendingDown
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
import com.example.data.TradeEntry
import com.example.ui.TradingViewModel
import com.example.ui.components.CustomCalendar
import java.util.Locale

@Composable
fun JournalTab(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val tradeEntries by viewModel.allTradeEntries.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedDateEntries by viewModel.selectedDateEntries.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("journal_tab_container")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Calendar grid
            item {
                Text(
                    text = "Calendar Journal",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                CustomCalendar(
                    tradeEntries = tradeEntries,
                    selectedDate = selectedDate,
                    onDateSelected = { viewModel.selectDate(it) }
                )
            }

            // Trades for selected date header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Trades on $selectedDate",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${selectedDateEntries.size} trade(s) logged",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("open_add_trade_dialog")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Trade")
                    }
                }
            }

            // Entries List
            if (selectedDateEntries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "No trades logged for this date. Click 'Log Trade' to save your results!",
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
                items(selectedDateEntries) { entry ->
                    TradeEntryItem(
                        entry = entry,
                        onDelete = { viewModel.deleteTradeEntry(entry) }
                    )
                }
            }

            // Empty item for scrolling margin
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // Dialog for logging trade
        if (showAddDialog) {
            AddTradeDialog(
                date = selectedDate,
                onDismiss = { showAddDialog = false },
                onSave = { profit, market, rr, notes, screenshot, isCustom, hex ->
                    viewModel.addTradeEntry(
                        date = selectedDate,
                        profitOrLoss = profit,
                        market = market,
                        riskToReward = rr,
                        notes = notes,
                        screenshotPath = screenshot,
                        isCustomColor = isCustom,
                        customColorHex = hex
                    )
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun TradeEntryItem(
    entry: TradeEntry,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Custom color extraction if set
    val customColor = remember(entry) {
        if (entry.isCustomColor && !entry.customColorHex.isNullOrBlank()) {
            try {
                Color(android.graphics.Color.parseColor(entry.customColorHex))
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    val themeColor = when {
        customColor != null -> customColor
        entry.profitOrLoss >= 0 -> Color(0xFF2E7D32)
        else -> Color(0xFFC62828)
    }

    val backgroundGlow = themeColor.copy(alpha = 0.05f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("trade_entry_item_${entry.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundGlow)
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Trend Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(themeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (entry.profitOrLoss >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Trade details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.market.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = if (entry.profitOrLoss >= 0) {
                            "+$${String.format(Locale.getDefault(), "%.2f", entry.profitOrLoss)}"
                        } else {
                            "-$${String.format(Locale.getDefault(), "%.2f", Math.abs(entry.profitOrLoss))}"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = themeColor
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "R:R Ratio: 1:${entry.riskToReward}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (entry.isCustomColor) {
                        Text(
                            text = "Custom Color Applied",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                            color = themeColor
                        )
                    }
                }

                if (entry.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = entry.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Screenshot Preview Mockup (Very beautiful!)
                if (!entry.screenshotPath.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Attachment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Chart Setup: ${entry.screenshotPath}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Action delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_trade_button_${entry.id}")
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AddTradeDialog(
    date: String,
    onDismiss: () -> Unit,
    onSave: (
        profit: Double,
        market: String,
        rr: Double,
        notes: String,
        screenshot: String?,
        isCustom: Boolean,
        customHex: String?
    ) -> Unit
) {
    var market by remember { mutableStateOf("GBPUSD") }
    var amountText by remember { mutableStateOf("") }
    var isLoss by remember { mutableStateOf(false) }
    var rrText by remember { mutableStateOf("2.0") }
    var notes by remember { mutableStateOf("") }

    // Color customization state
    var isCustomColor by remember { mutableStateOf(false) }
    var selectedColorIndex by remember { mutableStateOf(0) }

    // Presets for manual calendar color choices:
    val colorPresets = listOf(
        Pair("Forest Green", "#2E7D32"),
        Pair("Royal Blue", "#1565C0"),
        Pair("Vibrant Teal", "#00838F"),
        Pair("Deep Orange", "#D84315"),
        Pair("Vibrant Purple", "#6A1B9A"),
        Pair("Crimson Red", "#C62828")
    )

    // Screenshot state
    var selectedScreenshotType by remember { mutableStateOf("No Screenshot") }
    val screenshotOptions = listOf("No Screenshot", "Support/Resistance Breakout", "EMA Trend Bounce", "Golden Pocket Fibonacci Bounce", "Double Bottom Trigger")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Trade for $date",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Market Pair
                item {
                    OutlinedTextField(
                        value = market,
                        onValueChange = { market = it },
                        label = { Text("Market / Currency Pair") },
                        placeholder = { Text("e.g. GBPUSD or BTCUSD") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_market_input")
                    )
                }

                // Win or Loss choice
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { isLoss = false },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (!isLoss) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (!isLoss) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Win (+)", fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = { isLoss = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isLoss) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isLoss) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Loss (-)", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Profit/Loss amount
                item {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount ($)") },
                        placeholder = { Text("150.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_amount_input")
                    )
                }

                // Risk to Reward
                item {
                    OutlinedTextField(
                        value = rrText,
                        onValueChange = { rrText = it },
                        label = { Text("Risk-to-Reward Ratio (1:X)") },
                        placeholder = { Text("2.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Notes
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Journal Notes") },
                        placeholder = { Text("e.g. Price broke dynamic resistance...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Screenshot selection (Simulation)
                item {
                    Text(
                        text = "Attach Mock Setup Chart (Simulation)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        screenshotOptions.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedScreenshotType = option }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedScreenshotType == option,
                                    onClick = { selectedScreenshotType = option }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = option, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // Optional Calendar Color Customization! (Requirement)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Customize Calendar Indicator",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Switch(
                            checked = isCustomColor,
                            onCheckedChange = { isCustomColor = it }
                        )
                    }

                    if (isCustomColor) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Select Custom Color Highlight:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            colorPresets.forEachIndexed { index, pair ->
                                val color = Color(android.graphics.Color.parseColor(pair.second))
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (selectedColorIndex == index) 2.dp else 0.dp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColorIndex = index }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rawAmount = amountText.toDoubleOrNull() ?: 0.0
                    val finalAmount = if (isLoss) -Math.abs(rawAmount) else Math.abs(rawAmount)
                    val finalRR = rrText.toDoubleOrNull() ?: 2.0
                    val finalScreenshot = if (selectedScreenshotType == "No Screenshot") null else selectedScreenshotType
                    val customHex = if (isCustomColor) colorPresets[selectedColorIndex].second else null

                    onSave(finalAmount, market, finalRR, notes, finalScreenshot, isCustomColor, customHex)
                },
                modifier = Modifier.testTag("dialog_save_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
