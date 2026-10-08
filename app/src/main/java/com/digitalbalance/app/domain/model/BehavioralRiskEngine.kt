package com.digitalbalance.app.domain.model

import com.digitalbalance.app.data.local.entity.AppUsageEntity
import com.digitalbalance.app.services.usage.AppCategory
import kotlin.math.min
import kotlin.math.roundToInt

enum class RiskLevel {
    LOW,
    MODERATE,
    HIGH
}

data class RiskAssessmentResult(
    val score: Int, // 0 to 100
    val riskLevel: RiskLevel,
    val screenTimePenalty: Int,
    val categorySharePenalty: Int,
    val frequencyPenalty: Int,
    val totalScreenTimeMs: Long,
    val highRiskCategoryTimeMs: Long,
    val highRiskPercentage: Float,
    val totalSessions: Int,
    val reasons: List<String>
)

object BehavioralRiskEngine {

    /**
     * Calculates the transparent 100-point behavioral risk assessment score.
     * Engineered rule-based scoring mechanism:
     * Initial Score: 100
     * Indicator 1: Total Screen Time (Max 40 penalty)
     * Indicator 2: High-Risk Category Share (Max 30 penalty)
     * Indicator 3: Session Open Frequency (Max 30 penalty)
     */
    fun calculateRiskScore(usageList: List<AppUsageEntity>): RiskAssessmentResult {
        if (usageList.isEmpty()) {
            return RiskAssessmentResult(
                score = 100,
                riskLevel = RiskLevel.LOW,
                screenTimePenalty = 0,
                categorySharePenalty = 0,
                frequencyPenalty = 0,
                totalScreenTimeMs = 0L,
                highRiskCategoryTimeMs = 0L,
                highRiskPercentage = 0f,
                totalSessions = 0,
                reasons = listOf("No smartphone usage recorded for today yet.")
            )
        }

        val totalScreenTimeMs = usageList.sumOf { it.foregroundTimeMs }
        val totalScreenTimeMinutes = (totalScreenTimeMs / (1000 * 60)).toInt()

        val highRiskTimeMs = usageList
            .filter { usage ->
                val cat = try { AppCategory.valueOf(usage.category) } catch (_: Exception) { AppCategory.OTHER }
                cat == AppCategory.SOCIAL || cat == AppCategory.VIDEO || cat == AppCategory.GAMING
            }
            .sumOf { it.foregroundTimeMs }

        val highRiskPercentage = if (totalScreenTimeMs > 0) {
            (highRiskTimeMs.toFloat() / totalScreenTimeMs.toFloat()) * 100f
        } else 0f

        val totalSessions = usageList.sumOf { it.sessionCount }

        // --- Penalty 1: Total Screen Time (Max 40) ---
        val screenTimePenalty = when {
            totalScreenTimeMinutes <= 120 -> 0 // <= 2 hours: healthy
            totalScreenTimeMinutes <= 240 -> { // 2h - 4h: 0 to 20 penalty
                ((totalScreenTimeMinutes - 120) / 120.0 * 20.0).roundToInt()
            }
            totalScreenTimeMinutes <= 360 -> { // 4h - 6h: 20 to 30 penalty
                20 + ((totalScreenTimeMinutes - 240) / 120.0 * 10.0).roundToInt()
            }
            else -> { // > 6 hours: 30 to 40 penalty
                min(40, 30 + ((totalScreenTimeMinutes - 360) / 120.0 * 10.0).roundToInt())
            }
        }

        // --- Penalty 2: High-Risk Category Share (Max 30) ---
        val categorySharePenalty = when {
            highRiskPercentage <= 20f -> 0
            highRiskPercentage <= 50f -> { // 20% to 50%: 0 to 15 penalty
                ((highRiskPercentage - 20f) / 30f * 15f).roundToInt()
            }
            else -> { // > 50%: 15 to 30 penalty
                min(30, 15 + ((highRiskPercentage - 50f) / 50f * 15f).roundToInt())
            }
        }

        // --- Penalty 3: Session Open Frequency (Max 30) ---
        val frequencyPenalty = when {
            totalSessions <= 25 -> 0
            totalSessions <= 60 -> { // 25 to 60 opens: 0 to 15 penalty
                ((totalSessions - 25) / 35.0 * 15.0).roundToInt()
            }
            else -> { // > 60 opens: 15 to 30 penalty
                min(30, 15 + ((totalSessions - 60) / 40.0 * 15.0).roundToInt())
            }
        }

        val totalPenalties = screenTimePenalty + categorySharePenalty + frequencyPenalty
        val finalScore = (100 - totalPenalties).coerceIn(0, 100)

        val riskLevel = when {
            finalScore >= 70 -> RiskLevel.LOW
            finalScore >= 40 -> RiskLevel.MODERATE
            else -> RiskLevel.HIGH
        }

        // Generate explainable AI-style reasons for viva demonstration
        val reasons = mutableListOf<String>()

        if (screenTimePenalty > 15) {
            val hours = totalScreenTimeMinutes / 60
            val mins = totalScreenTimeMinutes % 60
            reasons.add("Elevated daily screen time detected (${hours}h ${mins}m).")
        } else {
            reasons.add("Daily screen time is within a healthy range.")
        }

        if (categorySharePenalty > 10) {
            reasons.add("High proportion of time spent on social media/entertainment (${highRiskPercentage.roundToInt()}%).")
        }

        if (frequencyPenalty > 10) {
            reasons.add("Frequent application opening triggers detected ($totalSessions app opens today).")
        }

        if (reasons.isEmpty()) {
            reasons.add("Usage patterns demonstrate balanced digital habits.")
        }

        return RiskAssessmentResult(
            score = finalScore,
            riskLevel = riskLevel,
            screenTimePenalty = screenTimePenalty,
            categorySharePenalty = categorySharePenalty,
            frequencyPenalty = frequencyPenalty,
            totalScreenTimeMs = totalScreenTimeMs,
            highRiskCategoryTimeMs = highRiskTimeMs,
            highRiskPercentage = highRiskPercentage,
            totalSessions = totalSessions,
            reasons = reasons
        )
    }
}
