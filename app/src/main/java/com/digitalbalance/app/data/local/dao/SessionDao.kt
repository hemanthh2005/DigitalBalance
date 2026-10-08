package com.digitalbalance.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.digitalbalance.app.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM app_sessions WHERE date = :date ORDER BY startTimeMs ASC")
    fun getSessionsForDate(date: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM app_sessions WHERE date = :date ORDER BY startTimeMs ASC")
    suspend fun getSessionsForDateSync(date: String): List<SessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<SessionEntity>)

    @Query("DELETE FROM app_sessions WHERE date < :beforeDate")
    suspend fun clearOldSessions(beforeDate: String)
}
