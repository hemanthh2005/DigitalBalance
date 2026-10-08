package com.digitalbalance.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.digitalbalance.app.data.local.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    @Query("SELECT * FROM app_usage WHERE date = :date ORDER BY foregroundTimeMs DESC")
    fun getUsageForDate(date: String): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE date = :date ORDER BY foregroundTimeMs DESC")
    suspend fun getUsageForDateSync(date: String): List<AppUsageEntity>

    @Query("SELECT * FROM app_usage WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getUsageBetweenDates(startDate: String, endDate: String): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE isSyncPending = 1")
    suspend fun getPendingSyncUsage(): List<AppUsageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUsage(usageList: List<AppUsageEntity>)

    @Query("UPDATE app_usage SET isSyncPending = 0 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("DELETE FROM app_usage WHERE date < :beforeDate")
    suspend fun clearOldUsage(beforeDate: String)
}
