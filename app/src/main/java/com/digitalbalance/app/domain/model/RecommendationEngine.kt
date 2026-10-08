package com.digitalbalance.app.domain.model

import com.digitalbalance.app.data.local.entity.RecommendationEntity

object RecommendationEngine {

    fun generateRecommendations(riskResult: RiskAssessmentResult): List<RecommendationEntity> {
        val list = mutableListOf<RecommendationEntity>()
        val now = System.currentTimeMillis()

        if (riskResult.screenTimePenalty >= 20) {
            list.add(
                RecommendationEntity(
                    title = "Take a Digital Detox Break",
                    explanation = "Your screen time today has exceeded recommended limits (${(riskResult.totalScreenTimeMs / (1000 * 60 * 60))} hours).",
                    reason = "High total daily screen time",
                    suggestedAction = "Step away from your smartphone for 15-30 minutes to relax your eyes.",
                    priority = "HIGH",
                    category = "SCREEN_TIME",
                    timestamp = now
                )
            )
        }

        if (riskResult.categorySharePenalty >= 15) {
            list.add(
                RecommendationEntity(
                    title = "Schedule Social Media Windows",
                    explanation = "${riskResult.highRiskPercentage.toInt()}% of your active screen time is spent on social media and video apps.",
                    reason = "High social media & entertainment share",
                    suggestedAction = "Set specific 20-minute daily time windows for checking social feeds instead of continuous browsing.",
                    priority = "MEDIUM",
                    category = "SOCIAL_MEDIA",
                    timestamp = now
                )
            )
        }

        if (riskResult.frequencyPenalty >= 15) {
            list.add(
                RecommendationEntity(
                    title = "Reduce Interruption Triggers",
                    explanation = "You opened apps ${riskResult.totalSessions} times today, indicating frequent distraction cycles.",
                    reason = "High session open frequency",
                    suggestedAction = "Turn off push notifications for non-essential applications to decrease glance frequency.",
                    priority = "MEDIUM",
                    category = "SESSION_FREQUENCY",
                    timestamp = now
                )
            )
        }

        if (list.isEmpty() || riskResult.riskLevel == RiskLevel.LOW) {
            list.add(
                RecommendationEntity(
                    title = "Maintain Healthy Habits",
                    explanation = "Your digital usage habits are well balanced today. Excellent control!",
                    reason = "Low risk score (${riskResult.score}/100)",
                    suggestedAction = "Continue balancing smartphone usage with outdoor and productive offline activities.",
                    priority = "LOW",
                    category = "HEALTHY",
                    timestamp = now
                )
            )
        }

        return list
    }
}
