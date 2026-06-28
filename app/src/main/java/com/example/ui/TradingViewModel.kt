package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiApi
import com.example.data.TradeEntry
import com.example.data.TradingDatabase
import com.example.data.TradingRepository
import com.example.data.TradingRule
import com.example.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ChatMessage(
    val sender: String, // "user" or "ai"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class TradingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TradingRepository

    init {
        val database = TradingDatabase.getDatabase(application)
        repository = TradingRepository(database.tradingDao())
    }

    // --- Flows from Room Database ---
    val allTradeEntries: StateFlow<List<TradeEntry>> = repository.allTradeEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTradingRules: StateFlow<List<TradingRule>> = repository.allTradingRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- UI States ---
    private val _selectedDate = MutableStateFlow(getCurrentDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _aiChatHistory = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "ai",
                message = "Welcome to your AI Trading Coach! Ask me anything about your trades, rules, or general trading strategies. I can analyze your trading history once you start adding trades!"
            )
        )
    )
    val aiChatHistory: StateFlow<List<ChatMessage>> = _aiChatHistory.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // --- Win Rate Calculator State ---
    private val _calcWins = MutableStateFlow("0")
    val calcWins: StateFlow<String> = _calcWins.asStateFlow()

    private val _calcLosses = MutableStateFlow("0")
    val calcLosses: StateFlow<String> = _calcLosses.asStateFlow()

    // --- Entry, TP, and SL spots ---
    private val _entryPoint = MutableStateFlow("")
    val entryPoint: StateFlow<String> = _entryPoint.asStateFlow()

    private val _takeProfitPoint = MutableStateFlow("")
    val takeProfitPoint: StateFlow<String> = _takeProfitPoint.asStateFlow()

    private val _stopLossPoint = MutableStateFlow("")
    val stopLossPoint: StateFlow<String> = _stopLossPoint.asStateFlow()

    // --- Combined selected date entries ---
    val selectedDateEntries: StateFlow<List<TradeEntry>> = combine(
        allTradeEntries,
        selectedDate
    ) { entries, date ->
        entries.filter { it.date == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Utility functions ---
    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Calendar.getInstance().time)
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    // --- Database Operations ---
    fun addTradeEntry(
        date: String,
        profitOrLoss: Double,
        market: String,
        riskToReward: Double,
        notes: String,
        screenshotPath: String?,
        isCustomColor: Boolean,
        customColorHex: String?
    ) {
        viewModelScope.launch {
            val entry = TradeEntry(
                date = date,
                profitOrLoss = profitOrLoss,
                market = market,
                riskToReward = riskToReward,
                notes = notes,
                screenshotPath = screenshotPath,
                isCustomColor = isCustomColor,
                customColorHex = customColorHex
            )
            repository.insertTradeEntry(entry)

            // Dynamically adjust user balance
            val profile = repository.getUserProfile() ?: UserProfile()
            val newBalance = profile.currentBalance + profitOrLoss
            repository.saveUserProfile(profile.copy(currentBalance = newBalance))
        }
    }

    fun updateTradeEntry(entry: TradeEntry) {
        viewModelScope.launch {
            // Re-calculate balance adjustment
            val oldEntry = allTradeEntries.value.find { it.id == entry.id }
            val diff = entry.profitOrLoss - (oldEntry?.profitOrLoss ?: 0.0)

            repository.updateTradeEntry(entry)

            val profile = repository.getUserProfile() ?: UserProfile()
            repository.saveUserProfile(profile.copy(currentBalance = profile.currentBalance + diff))
        }
    }

    fun deleteTradeEntry(entry: TradeEntry) {
        viewModelScope.launch {
            repository.deleteTradeEntryById(entry.id)

            // Adjust balance back
            val profile = repository.getUserProfile() ?: UserProfile()
            repository.saveUserProfile(profile.copy(currentBalance = profile.currentBalance - entry.profitOrLoss))
        }
    }

    fun addTradingRule(text: String) {
        viewModelScope.launch {
            if (text.isNotBlank()) {
                repository.insertTradingRule(TradingRule(ruleText = text))
            }
        }
    }

    fun deleteTradingRule(id: Int) {
        viewModelScope.launch {
            repository.deleteTradingRuleById(id)
        }
    }

    fun saveUserProfile(
        name: String,
        balance: Double,
        riskToReward: Double,
        markets: String,
        onboarded: Boolean
    ) {
        viewModelScope.launch {
            val existing = repository.getUserProfile() ?: UserProfile()
            val updated = existing.copy(
                traderName = name,
                initialBalance = balance,
                currentBalance = balance + calculateNetProfit(), // Set current balance to initial + accumulated profits
                riskToRewardRatio = riskToReward,
                marketsSeparatedByComma = markets,
                isOnboarded = onboarded
            )
            repository.saveUserProfile(updated)
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch {
            val defaultProfile = UserProfile(
                traderName = "Guest Trader",
                initialBalance = 10000.0,
                currentBalance = 10000.0,
                riskToRewardRatio = 2.0,
                marketsSeparatedByComma = "EURUSD, GBPUSD, BTCUSD",
                isOnboarded = true
            )
            repository.saveUserProfile(defaultProfile)
        }
    }

    private fun calculateNetProfit(): Double {
        return allTradeEntries.value.sumOf { it.profitOrLoss }
    }

    // --- Win Rate Calculator Setters ---
    fun updateCalcWins(wins: String) {
        _calcWins.value = wins
    }

    fun updateCalcLosses(losses: String) {
        _calcLosses.value = losses
    }

    // --- Entry, TP, SL setters ---
    fun updateEntryPoint(value: String) {
        _entryPoint.value = value
    }

    fun updateTakeProfitPoint(value: String) {
        _takeProfitPoint.value = value
    }

    fun updateStopLossPoint(value: String) {
        _stopLossPoint.value = value
    }

    // --- Gemini AI Assistant Integration ---
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return

        val userMessage = ChatMessage(sender = "user", message = userText)
        _aiChatHistory.value = _aiChatHistory.value + userMessage

        _isAiLoading.value = true

        viewModelScope.launch {
            // Compile context from actual Room database state!
            val profile = repository.getUserProfile() ?: UserProfile()
            val trades = allTradeEntries.value
            val rules = allTradingRules.value

            val totalTrades = trades.size
            val winningTrades = trades.count { it.profitOrLoss > 0 }
            val losingTrades = trades.count { it.profitOrLoss < 0 }
            val totalProfit = trades.sumOf { it.profitOrLoss }
            val winRate = if (totalTrades > 0) (winningTrades.toDouble() / totalTrades * 100) else 0.0

            // Market stats
            val winsByMarket = trades.filter { it.profitOrLoss > 0 }.groupBy { it.market }
            val lossesByMarket = trades.filter { it.profitOrLoss < 0 }.groupBy { it.market }
            val favoriteWinMarket = winsByMarket.maxByOrNull { it.value.size }?.key ?: "None"
            val favoriteLossMarket = lossesByMarket.maxByOrNull { it.value.size }?.key ?: "None"

            // Construct System Instruction to inject the trader's actual reality!
            val systemInstruction = """
                You are an expert AI Trading Coach. Your mission is to help Forex and Crypto traders analyze their journals, find consistency, and refine their strategies.
                
                Here is the real-time context of the current trader using your app:
                - Trader Name: ${profile.traderName.ifBlank { "Guest Trader" }}
                - Current Account Balance: $${String.format(Locale.getDefault(), "%.2f", profile.currentBalance)}
                - Target Risk-to-Reward Ratio: 1:${profile.riskToRewardRatio}
                - Permitted Markets: ${profile.marketsSeparatedByComma}
                
                Active Trading Rules Defined by Trader:
                ${if (rules.isEmpty()) "- No rules registered yet." else rules.joinToString("\n") { "• " + it.ruleText }}
                
                Performance History (ACTUAL TRADES FROM JOURNAL):
                - Total Recorded Trades: ${totalTrades}
                - Wins: ${winningTrades} (${String.format(Locale.getDefault(), "%.1f", winRate)}% Win Rate)
                - Losses: ${losingTrades}
                - Net Accumulated Profit/Loss: $${String.format(Locale.getDefault(), "%.2f", totalProfit)}
                - Best performing market: $favoriteWinMarket
                - Worst performing market: $favoriteLossMarket
                
                Recent Trades Log:
                ${if (trades.isEmpty()) "- No trade logs available yet." else trades.take(10).joinToString("\n") { "• Date: ${it.date}, Market: ${it.market}, Profit/Loss: $${it.profitOrLoss}, Notes: ${it.notes}" }}

                When answering, analyze this actual data whenever they ask "why did I lose", "what market do I lose in", etc. If there is no data, advise them to start logging trades in their calendar. Give short, professional, action-oriented, and highly analytical answers. Do not make up trades, reference the actual trades log above!
            """.trimIndent()

            val response = GeminiApi.generateContent(userText, systemInstruction)
            
            _aiChatHistory.value = _aiChatHistory.value + ChatMessage(sender = "ai", message = response)
            _isAiLoading.value = false
        }
    }

    fun clearChat() {
        _aiChatHistory.value = listOf(
            ChatMessage(
                sender = "ai",
                message = "Chat cleared! How can your AI Trading Coach help you refine your performance today?"
            )
        )
    }
}

// Factory for ViewModel
class TradingViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TradingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TradingViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
