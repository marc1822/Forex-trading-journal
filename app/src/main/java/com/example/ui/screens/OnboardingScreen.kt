package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
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
import com.example.ui.TradingViewModel

@Composable
fun OnboardingScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    var traderName by remember { mutableStateOf("") }
    var accountBalance by remember { mutableStateOf("10000.0") }
    var riskReward by remember { mutableStateOf("2.0") }
    var markets by remember { mutableStateOf("EURUSD, GBPUSD, BTCUSD") }
    var initialRule by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("onboarding_screen_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Greeting Banner
            Text(
                text = "Trading Journal",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Welcome! Set up your parameters below to establish your professional trading environment. You can modify these any time in your profile, or skip to start tracking immediately.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Form Fields
            OutlinedTextField(
                value = traderName,
                onValueChange = { traderName = it },
                label = { Text("Trader Name") },
                placeholder = { Text("e.g. Besufikad") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_name_input"),
                colors = OutlinedTextFieldDefaults.colors()
            )

            OutlinedTextField(
                value = accountBalance,
                onValueChange = { accountBalance = it },
                label = { Text("Initial Account Balance ($)") },
                placeholder = { Text("10000.0") },
                leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_balance_input")
            )

            OutlinedTextField(
                value = riskReward,
                onValueChange = { riskReward = it },
                label = { Text("Preferred Risk-to-Reward Ratio (1:X)") },
                placeholder = { Text("2.0") },
                leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_rr_input")
            )

            OutlinedTextField(
                value = markets,
                onValueChange = { markets = it },
                label = { Text("Markets You Trade (comma-separated)") },
                placeholder = { Text("GBPUSD, BTCUSD, AUDUSD...") },
                leadingIcon = { Icon(Icons.Default.ShowChart, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_markets_input")
            )

            OutlinedTextField(
                value = initialRule,
                onValueChange = { initialRule = it },
                label = { Text("Add Your First Trading Rule (Optional)") },
                placeholder = { Text("e.g. Always wait for retests before entry") },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_rule_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons Block
            Button(
                onClick = {
                    val finalName = traderName.trim().ifBlank { "Trader" }
                    val finalBalance = accountBalance.toDoubleOrNull() ?: 10000.0
                    val finalRR = riskReward.toDoubleOrNull() ?: 2.0
                    val finalMarkets = markets.trim().ifBlank { "EURUSD, GBPUSD, BTCUSD" }

                    viewModel.saveUserProfile(
                        name = finalName,
                        balance = finalBalance,
                        riskToReward = finalRR,
                        markets = finalMarkets,
                        onboarded = true
                    )

                    if (initialRule.isNotBlank()) {
                        viewModel.addTradingRule(initialRule.trim())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("onboarding_save_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Save & Start Trading",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            OutlinedButton(
                onClick = {
                    viewModel.skipOnboarding()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("onboarding_skip_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Skip Setup",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Developer Credits
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CREATED BY BESUFIKAD MEKONEN",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Contact: @AHBFORLIFE on Telegram",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
