package com.digitalbalance.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.digitalbalance.app.data.local.entity.ProtectedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtectedAppDao {
    @Query("SELECT * FROM protected_apps ORDER BY appName ASC")
    fun getAllProtectedApps(): Flow<List<ProtectedAppEntity>>

    @Query("SELECT * FROM protected_apps WHERE isProtected = 1")
    suspend fun getActiveProtectedAppsSync(): List<ProtectedAppEntity>

    @Query("SELECT * FROM protected_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getProtectedApp(packageName: String): ProtectedAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProtectedApp(app: ProtectedAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProtectedApps(apps: List<ProtectedAppEntity>)

    @Query("DELETE FROM protected_apps WHERE packageName = :packageName")
    suspend fun removeProtectedApp(packageName: String)
}
