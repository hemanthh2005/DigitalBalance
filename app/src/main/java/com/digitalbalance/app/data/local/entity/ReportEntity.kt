package com.digitalbalance.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val reportDate: String, // YYYY-MM-DD
    val totalScreenTimeMs: Long,
    val socialMediaTimeMs: Long,
    val totalSessions: Int,
    val riskScore: Int, // 0-100
    val riskLevel: String, // LOW, MODERATE, HIGH
    val topAppsSummary: String, // JSON or formatted text summary
    val riskReasons: String, // Bullet point explanations
    val generatedAt: Long = System.currentTimeMillis()
)
