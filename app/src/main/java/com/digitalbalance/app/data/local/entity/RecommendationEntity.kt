package com.digitalbalance.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recommendations")
data class RecommendationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val explanation: String,
    val reason: String,
    val suggestedAction: String,
    val priority: String, // HIGH, MEDIUM, LOW
    val category: String, // SCREEN_TIME, SOCIAL_MEDIA, SESSION_FREQUENCY, HEALTHY
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
