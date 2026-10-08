package com.digitalbalance.app.domain.repository

import com.digitalbalance.app.data.local.entity.ProtectedAppEntity
import com.digitalbalance.app.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

interface ChildSafetyRepository {
    fun getSettings(): Flow<SettingsEntity?>
    fun getProtectedApps(): Flow<List<ProtectedAppEntity>>
    suspend fun toggleChildMode(enabled: Boolean): Boolean
    suspend fun setGuardianPin(pin: String): Boolean
    suspend fun verifyGuardianPin(pin: String): Boolean
    suspend fun addProtectedApp(packageName: String, appName: String, limitMinutes: Int)
    suspend fun removeProtectedApp(packageName: String)
    suspend fun isAppProtected(packageName: String): Boolean
}
