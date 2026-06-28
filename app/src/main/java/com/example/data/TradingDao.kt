package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TradingDao {
    // --- Trade Entries ---
    @Query("SELECT * FROM trade_entries ORDER BY date DESC")
    fun getAllTradeEntriesFlow(): Flow<List<TradeEntry>>

    @Query("SELECT * FROM trade_entries WHERE date = :date")
    suspend fun getTradeEntriesForDate(date: String): List<TradeEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTradeEntry(entry: TradeEntry)

    @Update
    suspend fun updateTradeEntry(entry: TradeEntry)

    @Query("DELETE FROM trade_entries WHERE id = :id")
    suspend fun deleteTradeEntryById(id: Int)

    // --- Trading Rules ---
    @Query("SELECT * FROM trading_rules ORDER BY id ASC")
    fun getAllTradingRulesFlow(): Flow<List<TradingRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTradingRule(rule: TradingRule)

    @Query("DELETE FROM trading_rules WHERE id = :id")
    suspend fun deleteTradingRuleById(id: Int)

    // --- User Profile ---
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)
}
