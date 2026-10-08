package com.digitalbalance.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.digitalbalance.app.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports WHERE reportDate = :date LIMIT 1")
    fun getReportForDate(date: String): Flow<ReportEntity?>

    @Query("SELECT * FROM reports WHERE reportDate = :date LIMIT 1")
    suspend fun getReportForDateSync(date: String): ReportEntity?

    @Query("SELECT * FROM reports ORDER BY reportDate DESC LIMIT 30")
    fun getRecentReports(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReport(report: ReportEntity)
}
