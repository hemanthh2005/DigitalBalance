package com.digitalbalance.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val childModeEnabled: Boolean = false,
    val guardianPinHash: String = "", // Hashed PIN for Guardian authentication
    val syncEnabled: Boolean = true,
    val notificationEnabled: Boolean = true,
    val dailyGoalMinutes: Int = 180, // Default 3 hours goal
    val lastSyncTimestamp: Long = 0L
)
