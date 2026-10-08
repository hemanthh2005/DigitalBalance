package com.digitalbalance.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "protected_apps")
data class ProtectedAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isProtected: Boolean = true,
    val dailyLimitMinutes: Int = 60, // Default 60 minutes limit
    val requiresAdultVerification: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)
