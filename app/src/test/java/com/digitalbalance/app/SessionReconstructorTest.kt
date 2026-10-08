package com.digitalbalance.app

import com.digitalbalance.app.services.usage.RawUsageEvent
import com.digitalbalance.app.services.usage.SessionReconstructor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class SessionReconstructorTest {

    @Test
    fun testNormalSessionReconstruction() {
        val events = listOf(
            RawUsageEvent("com.instagram.android", "Instagram", 1, 1000L),
            RawUsageEvent("com.instagram.android", "Instagram", 2, 61000L) // 60 seconds
        )

        val sessions = SessionReconstructor.reconstructSessions(events, 100000L)
        assertEquals(1, sessions.size)
        assertEquals("com.instagram.android", sessions[0].packageName)
        assertEquals(60000L, sessions[0].durationMs)
    }

    @Test
    fun testSelfPackageExclusion() {
        val events = listOf(
            RawUsageEvent("com.digitalbalance.app", "DigitalBalance", 1, 1000L),
            RawUsageEvent("com.digitalbalance.app", "DigitalBalance", 2, 60000L)
        )

        val sessions = SessionReconstructor.reconstructSessions(events, 100000L, "com.digitalbalance.app")
        assertTrue(sessions.isEmpty())
    }

    @Test
    fun testMidnightSplittingLogic() {
        // Set up time crossing midnight: 23:55 on Oct 5 to 00:10 on Oct 6
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.set(2026, Calendar.OCTOBER, 5, 23, 55, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startTime = cal.timeInMillis

        val calEnd = Calendar.getInstance(TimeZone.getDefault())
        calEnd.set(2026, Calendar.OCTOBER, 6, 0, 10, 0)
        calEnd.set(Calendar.MILLISECOND, 0)
        val endTime = calEnd.timeInMillis

        val splitSessions = SessionReconstructor.splitSessionAtMidnight(
            pkg = "com.twitter.android",
            appName = "Twitter",
            startTimeMs = startTime,
            endTimeMs = endTime
        )

        assertEquals(2, splitSessions.size)
        assertEquals("2026-10-05", splitSessions[0].date)
        assertEquals(5 * 60 * 1000L, splitSessions[0].durationMs)

        assertEquals("2026-10-06", splitSessions[1].date)
        assertEquals(10 * 60 * 1000L, splitSessions[1].durationMs)
        assertTrue(splitSessions[1].isMidnightSplit)
    }

    @Test
    fun testScreenNonInteractiveClosesActiveSession() {
        val events = listOf(
            RawUsageEvent("com.google.android.youtube", "YouTube", 1, 1000L),
            RawUsageEvent("", "", 3, 31000L) // SCREEN_NON_INTERACTIVE after 30 sec
        )

        val sessions = SessionReconstructor.reconstructSessions(events, 100000L)
        assertEquals(1, sessions.size)
        assertEquals("com.google.android.youtube", sessions[0].packageName)
        assertEquals(30000L, sessions[0].durationMs)
    }

    @Test
    fun testMultipleAppSwitching() {
        val events = listOf(
            RawUsageEvent("com.google.android.youtube", "YouTube", 1, 1000L),
            RawUsageEvent("com.google.android.youtube", "YouTube", 2, 11000L),
            RawUsageEvent("com.android.chrome", "Chrome", 1, 11000L),
            RawUsageEvent("com.android.chrome", "Chrome", 2, 21000L),
            RawUsageEvent("com.google.android.youtube", "YouTube", 1, 21000L),
            RawUsageEvent("com.google.android.youtube", "YouTube", 2, 31000L)
        )

        val sessions = SessionReconstructor.reconstructSessions(events, 100000L)
        assertEquals(3, sessions.size)
        val totalYoutubeMs = sessions.filter { it.packageName == "com.google.android.youtube" }.sumOf { it.durationMs }
        val totalChromeMs = sessions.filter { it.packageName == "com.android.chrome" }.sumOf { it.durationMs }

        assertEquals(20000L, totalYoutubeMs)
        assertEquals(10000L, totalChromeMs)
    }

    @Test
    fun testDynamicLauncherExclusion() {
        val events = listOf(
            RawUsageEvent("com.teslacoilsw.launcher", "Nova Launcher", 1, 1000L),
            RawUsageEvent("com.teslacoilsw.launcher", "Nova Launcher", 2, 60000L)
        )

        val sessions = SessionReconstructor.reconstructSessions(
            events = events,
            queryEndTimeMs = 100000L,
            dynamicLaunchers = setOf("com.teslacoilsw.launcher")
        )
        assertTrue(sessions.isEmpty())
    }

    @Test
    fun testEmptyUsageEvents() {
        val sessions = SessionReconstructor.reconstructSessions(emptyList(), 100000L)
        assertTrue(sessions.isEmpty())
    }
}
