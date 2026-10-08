package com.digitalbalance.app.domain.repository

import com.digitalbalance.app.data.local.entity.AppUsageEntity
import com.digitalbalance.app.data.local.entity.RecommendationEntity
import com.digitalbalance.app.data.local.entity.ReportEntity
import com.digitalbalance.app.domain.model.RiskAssessmentResult
import kotlinx.coroutines.flow.Flow

interface UsageRepository {
    fun getTodayUsage(): Flow<List<AppUsageEntity>>
    fun getUsageForDate(date: String): Flow<List<AppUsageEntity>>
    suspend fun refreshUsageForToday(): List<AppUsageEntity>
    suspend fun refreshUsageForPast7Days(): Map<String, List<AppUsageEntity>>
    suspend fun calculateTodayRisk(): RiskAssessmentResult
    fun getRecommendations(): Flow<List<RecommendationEntity>>
    fun getReportForDate(date: String): Flow<ReportEntity?>
    suspend fun generateDailyReport(date: String): ReportEntity
    suspend fun syncPendingUsageWithBackend(): Boolean
}
