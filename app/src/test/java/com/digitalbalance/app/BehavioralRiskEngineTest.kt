package com.digitalbalance.app

import com.digitalbalance.app.data.local.entity.AppUsageEntity
import com.digitalbalance.app.domain.model.BehavioralRiskEngine
import com.digitalbalance.app.domain.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BehavioralRiskEngineTest {

    @Test
    fun testEmptyUsageReturnsLowRisk() {
        val result = BehavioralRiskEngine.calculateRiskScore(emptyList())
        assertEquals(100, result.score)
        assertEquals(RiskLevel.LOW, result.riskLevel)
        assertEquals(0, result.screenTimePenalty)
    }

    @Test
    fun testExcessiveUsageCalculatesHighRisk() {
        val usageList = listOf(
            AppUsageEntity(
                date = "2026-10-05",
                packageName = "com.instagram.android",
                appName = "Instagram",
                category = "SOCIAL",
                foregroundTimeMs = 5 * 3600 * 1000L, // 5 hours
                sessionCount = 75,
                isSocialMedia = true
            ),
            AppUsageEntity(
                date = "2026-10-05",
                packageName = "com.pubg.krmobile",
                appName = "PUBG",
                category = "GAMING",
                foregroundTimeMs = 3 * 3600 * 1000L, // 3 hours
                sessionCount = 20,
                isSocialMedia = false
            )
        )

        val result = BehavioralRiskEngine.calculateRiskScore(usageList)

        // Total time: 8 hours (>6h -> max 40 screen time penalty)
        // High risk share: 100% (>50% -> max 30 category share penalty)
        // Sessions: 95 (>60 -> 28 frequency penalty) -> Total score = 2 (HIGH RISK)
        assertEquals(RiskLevel.HIGH, result.riskLevel)
        assertTrue(result.score <= 10)
        assertTrue(result.reasons.isNotEmpty())
    }
}
