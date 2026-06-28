package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TradingRule
import com.example.ui.TradingViewModel
import java.util.Locale

@Composable
fun RulesTab(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val rules by viewModel.allTradingRules.collectAsState()
    
    val entryPoint by viewModel.entryPoint.collectAsState()
    val takeProfitPoint by viewModel.takeProfitPoint.collectAsState()
    val stopLossPoint by viewModel.stopLossPoint.collectAsState()

    var newRuleText by remember { mutableStateOf("") }

    // Dynamic calculations for the Entry planner
    val entryVal = entryPoint.toDoubleOrNull() ?: 0.0
    val tpVal = takeProfitPoint.toDoubleOrNull() ?: 0.0
    val slVal = stopLossPoint.toDoubleOrNull() ?: 0.0

    val calculations = remember(entryVal, tpVal, slVal) {
        if (entryVal > 0.0) {
            val potentialGain = Math.abs(tpVal - entryVal)
            val potentialRisk = Math.abs(entryVal - slVal)
            val rrRatio = if (potentialRisk > 0.0) potentialGain / potentialRisk else 0.0
            
            // Format pips assuming forex 4-decimal points standard helper
            val gainPips = potentialGain * 10000
            val riskPips = potentialRisk * 10000

            Triple(gainPips, riskPips, rrRatio)
        } else {
            Triple(0.0, 0.0, 0.0)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("rules_tab_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Rules Section Header
        item {
            Text(
                text = "Trading Rules Blueprint",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "These rules act as your personal guardrails and stay saved permanently until deleted.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Add Rule Input Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newRuleText,
                        onValueChange = { newRuleText = it },
                        placeholder = { Text("Write a trading rule... e.g. Never risk more than 1% per trade") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("rule_input_field"),
                        colors = OutlinedTextFieldDefaults.colors()
                    )

                    IconButton(
                        onClick = {
                            if (newRuleText.isNotBlank()) {
                                viewModel.addTradingRule(newRuleText.trim())
                                newRuleText = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("add_rule_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Rule")
                    }
                }
            }
        }

        // Rules List Table
        if (rules.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = "No trading rules created yet. Write one above to construct your discipline blueprint!",
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
            items(rules) { rule ->
                RuleRowItem(
                    rule = rule,
                    onDelete = { viewModel.deleteTradingRule(rule.id) }
                )
            }
        }

        // Entry Spot / TP / SL Planner Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Active Position Setup Planner",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Draft your entries to calculate potential risk reward ratios instantly.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Interactive calculation fields
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Entry Spot Input
                    OutlinedTextField(
                        value = entryPoint,
                        onValueChange = { viewModel.updateEntryPoint(it) },
                        label = { Text("Entry Price Point") },
                        placeholder = { Text("e.g. 1.2500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("entry_point_input")
                    )

                    // Take Profit Spot Input
                    OutlinedTextField(
                        value = takeProfitPoint,
                        onValueChange = { viewModel.updateTakeProfitPoint(it) },
                        label = { Text("Take Profit (TP)") },
                        placeholder = { Text("e.g. 1.2600") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tp_point_input")
                    )

                    // Stop Loss Spot Input
                    OutlinedTextField(
                        value = stopLossPoint,
                        onValueChange = { viewModel.updateStopLossPoint(it) },
                        label = { Text("Stop Loss (SL)") },
                        placeholder = { Text("e.g. 1.2450") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sl_point_input")
                    )

                    // Live calculation values Display
                    if (entryVal > 0.0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Position Estimations (Pips):",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Target Profit: +${String.format(Locale.getDefault(), "%.1f", calculations.first)} pips",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF2E7D32)
                                    )
                                    Text(
                                        text = "Target Risk: -${String.format(Locale.getDefault(), "%.1f", calculations.second)} pips",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFC62828)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Calculated Reward-To-Risk:  1 : ${String.format(Locale.getDefault(), "%.2f", calculations.third)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // Buffer space
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun RuleRowItem(
    rule: TradingRule,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rule_item_${rule.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.FormatListNumbered,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = rule.ruleText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_rule_button_${rule.id}")
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Rule",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
