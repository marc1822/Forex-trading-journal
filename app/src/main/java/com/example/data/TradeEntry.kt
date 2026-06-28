package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trade_entries")
data class TradeEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // "YYYY-MM-DD"
    val profitOrLoss: Double, // positive for win, negative for loss
    val market: String, // e.g. "GBPUSD", "BTCUSD"
    val riskToReward: Double = 1.0,
    val notes: String = "",
    val screenshotPath: String? = null, // Path or URI or description of screenshot
    val isCustomColor: Boolean = false,
    val customColorHex: String? = null // Hex value if user manually changed color
)
