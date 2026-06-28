package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trading_rules")
data class TradingRule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ruleText: String,
    val isCompleted: Boolean = false
)
