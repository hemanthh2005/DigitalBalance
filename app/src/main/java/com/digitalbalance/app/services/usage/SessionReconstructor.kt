package com.digitalbalance.app.services.usage

import com.digitalbalance.app.data.local.entity.SessionEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class RawUsageEvent(
    val packageName: String,
    val appName: String,
    val eventType: Int, // 1 = MOVE_TO_FOREGROUND, 2 = MOVE_TO_BACKGROUND, 3 = SCREEN_NON_INTERACTIVE
    val timestampMs: Long
)

object SessionReconstructor {

    const val EVENT_MOVE_TO_FOREGROUND = 1
    const val EVENT_MOVE_TO_BACKGROUND = 2
    const val EVENT_SCREEN_NON_INTERACTIVE = 3

    private val defaultLauncherPackages = setOf(
        "com.google.android.apps.nexuslauncher",
        "com.sec.android.app.launcher",
        "com.miui.home",
        "com.oppo.launcher",
        "com.vivo.launcher",
        "com.oneplus.launcher",
        "com.android.launcher3"
    )

    /**
     * Reconstructs raw usage events into clean session entities.
     * Excludes launcher background time and DigitalBalance self-usage.
     * Closes sessions on SCREEN_NON_INTERACTIVE / KEYGUARD_SHOWN.
     * Handles midnight splitting seamlessly.
     */
    fun reconstructSessions(
        events: List<RawUsageEvent>,
        queryEndTimeMs: Long = System.currentTimeMillis(),
        selfPackageName: String = "com.digitalbalance.app",
        dynamicLaunchers: Set<String> = emptySet()
    ): List<SessionEntity> {
        val launcherPackages = defaultLauncherPackages + dynamicLaunchers
        val sortedEvents = events.sortedBy { it.timestampMs }
        val activeSessions = mutableMapOf<String, RawUsageEvent>()
        val resultSessions = mutableListOf<SessionEntity>()

        for (event in sortedEvents) {
            val pkg = event.packageName

            // Log raw forensic event
            val eventName = when (event.eventType) {
                EVENT_MOVE_TO_FOREGROUND -> "ACTIVITY_RESUMED"
                EVENT_MOVE_TO_BACKGROUND -> "ACTIVITY_PAUSED"
                EVENT_SCREEN_NON_INTERACTIVE -> "SCREEN_NON_INTERACTIVE"
                else -> "UNKNOWN_$event.eventType"
            }
            safeLog(
                "DB_USAGE",
                "[DB_USAGE_FORENSIC_EVENT] time=${event.timestampMs}, package=$pkg, event=$eventName, class=N/A"
            )

            // Skip self package and system/dynamic launcher idle time
            if (pkg.isNotBlank() && (pkg == selfPackageName || launcherPackages.contains(pkg))) {
                continue
            }

            when (event.eventType) {
                EVENT_MOVE_TO_FOREGROUND -> {
                    // If previous session wasn't closed, close it first
                    if (activeSessions.containsKey(pkg)) {
                        val prevStart = activeSessions[pkg]!!
                        val splitList = splitSessionAtMidnight(
                            pkg = prevStart.packageName,
                            appName = prevStart.appName,
                            startTimeMs = prevStart.timestampMs,
                            endTimeMs = event.timestampMs
                        )
                        resultSessions.addAll(splitList)
                    }
                    activeSessions[pkg] = event
                }
                EVENT_MOVE_TO_BACKGROUND -> {
                    val startEvent = activeSessions.remove(pkg)
                    if (startEvent != null && event.timestampMs > startEvent.timestampMs) {
                        val splitList = splitSessionAtMidnight(
                            pkg = startEvent.packageName,
                            appName = startEvent.appName,
                            startTimeMs = startEvent.timestampMs,
                            endTimeMs = event.timestampMs
                        )
                        resultSessions.addAll(splitList)
                    }
                }
                EVENT_SCREEN_NON_INTERACTIVE -> {
                    // Device locked / screen turned off: close all active foreground sessions at this timestamp
                    for ((activePkg, startEvent) in activeSessions) {
                        if (event.timestampMs > startEvent.timestampMs) {
                            val splitList = splitSessionAtMidnight(
                                pkg = activePkg,
                                appName = startEvent.appName,
                                startTimeMs = startEvent.timestampMs,
                                endTimeMs = event.timestampMs
                            )
                            resultSessions.addAll(splitList)
                        }
                    }
                    activeSessions.clear()
                }
            }
        }

        // Handle open sessions that haven't received a foreground end event yet
        for ((pkg, startEvent) in activeSessions) {
            if (queryEndTimeMs > startEvent.timestampMs) {
                val splitList = splitSessionAtMidnight(
                    pkg = pkg,
                    appName = startEvent.appName,
                    startTimeMs = startEvent.timestampMs,
                    endTimeMs = queryEndTimeMs
                )
                resultSessions.addAll(splitList)
            }
        }

        // Log forensic session traces
        for (session in resultSessions) {
            val clippedNow = session.endTimeMs == queryEndTimeMs
            safeLog(
                "DB_USAGE",
                "[DB_USAGE_FORENSIC] package=${session.packageName}, appName=${session.appName}, start=${session.startTimeMs}, end=${session.endTimeMs}, durationMs=${session.durationMs}, date=${session.date}, midnightSplit=${session.isMidnightSplit}, clippedNow=$clippedNow"
            )
        }

        // Log forensic overlap analysis across all reconstructed sessions
        for (i in resultSessions.indices) {
            for (j in i + 1 until resultSessions.size) {
                val s1 = resultSessions[i]
                val s2 = resultSessions[j]
                val overlapStart = maxOf(s1.startTimeMs, s2.startTimeMs)
                val overlapEnd = minOf(s1.endTimeMs, s2.endTimeMs)
                if (overlapEnd > overlapStart) {
                    val overlapMs = overlapEnd - overlapStart
                    safeLog(
                        "DB_USAGE",
                        "[DB_USAGE_FORENSIC_OVERLAP] package1=${s1.packageName}, package2=${s2.packageName}, overlapStart=$overlapStart, overlapEnd=$overlapEnd, overlapDurationMs=$overlapMs"
                    )
                }
            }
        }

        return resultSessions
    }

    private fun safeLog(tag: String, msg: String) {
        try {
            android.util.Log.d(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] $msg")
        }
    }

    /**
     * Splits a session if it crosses one or more midnight boundaries (00:00:00).
     */
    fun splitSessionAtMidnight(
        pkg: String,
        appName: String,
        startTimeMs: Long,
        endTimeMs: Long
    ): List<SessionEntity> {
        if (endTimeMs <= startTimeMs) return emptyList()

        val sessions = mutableListOf<SessionEntity>()
        var currentStart = startTimeMs

        while (currentStart < endTimeMs) {
            val nextMidnight = getNextMidnightMs(currentStart)

            if (nextMidnight in (currentStart + 1)..endTimeMs) {
                // Crosses midnight!
                val duration = nextMidnight - currentStart
                val dateStr = formatDate(currentStart)
                sessions.add(
                    SessionEntity(
                        packageName = pkg,
                        appName = appName,
                        startTimeMs = currentStart,
                        endTimeMs = nextMidnight,
                        durationMs = duration,
                        date = dateStr,
                        isMidnightSplit = true
                    )
                )
                currentStart = nextMidnight
            } else {
                // Entire remaining session is within the same day
                val duration = endTimeMs - currentStart
                val dateStr = formatDate(currentStart)
                sessions.add(
                    SessionEntity(
                        packageName = pkg,
                        appName = appName,
                        startTimeMs = currentStart,
                        endTimeMs = endTimeMs,
                        durationMs = duration,
                        date = dateStr,
                        isMidnightSplit = currentStart != startTimeMs
                    )
                )
                break
            }
        }

        return sessions
    }

    private fun getNextMidnightMs(timeMs: Long): Long {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.timeInMillis = timeMs
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return calendar.timeInMillis
    }

    fun formatDate(timeMs: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date(timeMs))
    }
}
