package com.digitalbalance.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_usage",
    indices = [Index(value = ["date", "packageName"], unique = true)]
)
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val packageName: String,
    val appName: String,
    val category: String, // SOCIAL, VIDEO, GAMING, PRODUCTIVITY, ENTERTAINMENT, OTHER
    val foregroundTimeMs: Long,
    val sessionCount: Int,
    val isSocialMedia: Boolean = false,
    val isSyncPending: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
