package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single profile entry
    val traderName: String = "",
    val currentBalance: Double = 10000.0,
    val initialBalance: Double = 10000.0,
    val riskToRewardRatio: Double = 2.0,
    val marketsSeparatedByComma: String = "GBPUSD, EURUSD, BTCUSD",
    val isOnboarded: Boolean = false
)
