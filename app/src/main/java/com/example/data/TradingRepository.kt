package com.example.data

import kotlinx.coroutines.flow.Flow

class TradingRepository(private val dao: TradingDao) {
    val allTradeEntries: Flow<List<TradeEntry>> = dao.getAllTradeEntriesFlow()
    val allTradingRules: Flow<List<TradingRule>> = dao.getAllTradingRulesFlow()
    val userProfile: Flow<UserProfile?> = dao.getUserProfileFlow()

    suspend fun getTradeEntriesForDate(date: String): List<TradeEntry> {
        return dao.getTradeEntriesForDate(date)
    }

    suspend fun insertTradeEntry(entry: TradeEntry) {
        dao.insertTradeEntry(entry)
    }

    suspend fun updateTradeEntry(entry: TradeEntry) {
        dao.updateTradeEntry(entry)
    }

    suspend fun deleteTradeEntryById(id: Int) {
        dao.deleteTradeEntryById(id)
    }

    suspend fun insertTradingRule(rule: TradingRule) {
        dao.insertTradingRule(rule)
    }

    suspend fun deleteTradingRuleById(id: Int) {
        dao.deleteTradingRuleById(id)
    }

    suspend fun getUserProfile(): UserProfile? {
        return dao.getUserProfile()
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        dao.insertUserProfile(profile)
    }
}
