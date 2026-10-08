package com.digitalbalance.app.data.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.digitalbalance.app.data.local.dao.AppUsageDao
import com.digitalbalance.app.data.local.dao.RecommendationDao
import com.digitalbalance.app.data.local.dao.ReportDao
import com.digitalbalance.app.data.local.dao.SessionDao
import com.digitalbalance.app.data.local.entity.AppUsageEntity
import com.digitalbalance.app.data.local.entity.RecommendationEntity
import com.digitalbalance.app.data.local.entity.ReportEntity
import com.digitalbalance.app.domain.model.BehavioralRiskEngine
import com.digitalbalance.app.domain.model.RecommendationEngine
import com.digitalbalance.app.domain.model.RiskAssessmentResult
import com.digitalbalance.app.domain.repository.UsageRepository
import com.digitalbalance.app.services.usage.AppCategoryProvider
import com.digitalbalance.app.services.usage.DailyUsageAggregator
import com.digitalbalance.app.services.usage.RawUsageEvent
import com.digitalbalance.app.services.usage.SessionReconstructor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appUsageDao: AppUsageDao,
    private val sessionDao: SessionDao,
    private val recommendationDao: RecommendationDao,
    private val reportDao: ReportDao,
    private val aggregator: DailyUsageAggregator,
    private val categoryProvider: AppCategoryProvider
) : UsageRepository {

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager

    private val selfPackageName: String = context.packageName

    private val systemLaunchers = setOf(
        "com.google.android.apps.nexuslauncher",
        "com.sec.android.app.launcher",
        "com.miui.home",
        "com.oppo.launcher",
        "com.vivo.launcher",
        "com.oneplus.launcher",
        "com.android.launcher3",
        "com.android.systemui"
    )

    override fun getTodayUsage(): Flow<List<AppUsageEntity>> {
        val todayStr = getTodayDateString()
        return appUsageDao.getUsageForDate(todayStr)
    }

    override fun getUsageForDate(date: String): Flow<List<AppUsageEntity>> {
        return appUsageDao.getUsageForDate(date)
    }

    override suspend fun refreshUsageForToday(): List<AppUsageEntity> = withContext(Dispatchers.IO) {
        val allMap = refreshUsageForPast7Days()
        val todayStr = getTodayDateString()
        return@withContext allMap[todayStr] ?: appUsageDao.getUsageForDateSync(todayStr)
    }

    override suspend fun refreshUsageForPast7Days(): Map<String, List<AppUsageEntity>> = withContext(Dispatchers.IO) {
        val timeZone = java.util.TimeZone.getDefault()
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { this.timeZone = timeZone }
        val sdfHuman = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply { this.timeZone = timeZone }

        val endNowMs = System.currentTimeMillis()

        // 7 days ago midnight
        val cal7DaysAgo = Calendar.getInstance(timeZone).apply {
            add(Calendar.DAY_OF_YEAR, -7)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start7DaysMs = cal7DaysAgo.timeInMillis

        // Today midnight for forensic logging
        val calTodayMidnight = Calendar.getInstance(timeZone).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayMidnightMs = calTodayMidnight.timeInMillis

        android.util.Log.d("USAGE_FORENSIC", "[USAGE_FORENSIC] START: ${sdfHuman.format(Date(todayMidnightMs))} ($todayMidnightMs) TimeZone: ${timeZone.id}")
        android.util.Log.d("USAGE_FORENSIC", "[USAGE_FORENSIC] END: ${sdfHuman.format(Date(endNowMs))} ($endNowMs) TimeZone: ${timeZone.id}")

        val pm = context.packageManager
        val dynamicLaunchers = getInstalledLaunchers(pm)
        val allExcludedLaunchers = systemLaunchers + dynamicLaunchers
        val resultMap = mutableMapOf<String, List<AppUsageEntity>>()

        if (usageStatsManager != null) {
            val usageEvents = usageStatsManager.queryEvents(start7DaysMs, endNowMs)
            val rawEvents = mutableListOf<RawUsageEvent>()
            val event = UsageEvents.Event()

            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                val eventType = when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> SessionReconstructor.EVENT_MOVE_TO_FOREGROUND
                    UsageEvents.Event.ACTIVITY_PAUSED, UsageEvents.Event.ACTIVITY_STOPPED -> SessionReconstructor.EVENT_MOVE_TO_BACKGROUND
                    UsageEvents.Event.SCREEN_NON_INTERACTIVE, UsageEvents.Event.KEYGUARD_SHOWN -> SessionReconstructor.EVENT_SCREEN_NON_INTERACTIVE
                    else -> continue
                }
                val pkg = event.packageName ?: continue
                val appName = getAppNameForPackage(pm, pkg)
                rawEvents.add(
                    RawUsageEvent(
                        packageName = pkg,
                        appName = appName,
                        eventType = eventType,
                        timestampMs = event.timeStamp
                    )
                )
            }

            // Reconstruct exact screen-on sessions
            val reconstructedSessions = SessionReconstructor.reconstructSessions(
                events = rawEvents,
                queryEndTimeMs = endNowMs,
                selfPackageName = selfPackageName,
                dynamicLaunchers = dynamicLaunchers
            )

            // Persist reconstructed sessions
            val sessionEntities = reconstructedSessions.map { session ->
                com.digitalbalance.app.data.local.entity.SessionEntity(
                    packageName = session.packageName,
                    appName = session.appName,
                    startTimeMs = session.startTimeMs,
                    endTimeMs = session.endTimeMs,
                    durationMs = session.durationMs,
                    date = session.date,
                    isMidnightSplit = session.isMidnightSplit
                )
            }
            try {
                sessionDao.insertSessions(sessionEntities)
            } catch (_: Exception) {}

            val sessionsByDate = reconstructedSessions.groupBy { it.date }

            val datesList = mutableListOf<String>()
            for (i in 6 downTo 0) {
                val c = Calendar.getInstance(timeZone).apply {
                    add(Calendar.DAY_OF_YEAR, -i)
                }
                datesList.add(sdfDate.format(c.time))
            }

            val todayStr = sdfDate.format(Date())

            for (dateStr in datesList) {
                val daySessions = sessionsByDate[dateStr] ?: emptyList()
                val rawDailyList = aggregator.aggregateSessionsToDailyUsage(dateStr, daySessions)

                var rawTotalMs = 0L
                var filteredTotalMs = 0L
                val finalFilteredList = mutableListOf<AppUsageEntity>()

                for (item in rawDailyList) {
                    rawTotalMs += item.foregroundTimeMs
                    val pkg = item.packageName

                    val isSelf = (pkg == selfPackageName)
                    val isLauncher = allExcludedLaunchers.contains(pkg)
                    val isFacing = isUserFacingApp(pm, pkg)
                    val excluded = isSelf || isLauncher || !isFacing

                    val reason = when {
                        isSelf -> "SELF_PACKAGE"
                        isLauncher -> "LAUNCHER_SYSTEM_UI"
                        !isFacing -> "NON_USER_FACING"
                        else -> "USER_FACING_APP"
                    }

                    if (!excluded && item.foregroundTimeMs > 0L) {
                        filteredTotalMs += item.foregroundTimeMs
                        finalFilteredList.add(item)
                    }

                    if (dateStr == todayStr) {
                        android.util.Log.d(
                            "USAGE_FORENSIC",
                            "[USAGE_FORENSIC] date=$dateStr timezone=${timeZone.id} package=$pkg app=${item.appName} rawUsageMs=${item.foregroundTimeMs} included=${!excluded} reason=$reason finalUsageMs=${if (excluded) 0L else item.foregroundTimeMs}"
                        )
                    }
                }

                // If event query had no sessions for a past date, fallback to queryUsageStats for that day
                if (finalFilteredList.isEmpty()) {
                    val calDayStart = Calendar.getInstance(timeZone).apply {
                        time = sdfDate.parse(dateStr) ?: Date()
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val calDayEnd = (calDayStart.clone() as Calendar).apply {
                        if (dateStr == todayStr) {
                            timeInMillis = endNowMs
                        } else {
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                    }

                    val sysStats = usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        calDayStart.timeInMillis,
                        calDayEnd.timeInMillis
                    )
                    if (!sysStats.isNullOrEmpty()) {
                        for (stat in sysStats) {
                            val pkg = stat.packageName ?: continue
                            if (pkg == selfPackageName || allExcludedLaunchers.contains(pkg)) continue
                            if (stat.totalTimeInForeground > 0L && isUserFacingApp(pm, pkg)) {
                                val appName = getAppNameForPackage(pm, pkg)
                                val cat = categoryProvider.getCategory(pkg)
                                val entity = AppUsageEntity(
                                    date = dateStr,
                                    packageName = pkg,
                                    appName = appName,
                                    category = cat.name,
                                    foregroundTimeMs = stat.totalTimeInForeground,
                                    sessionCount = 1,
                                    isSocialMedia = categoryProvider.isSocialMedia(pkg),
                                    isSyncPending = true,
                                    updatedAt = System.currentTimeMillis()
                                )
                                finalFilteredList.add(entity)
                                filteredTotalMs += stat.totalTimeInForeground
                            }
                        }
                    }
                }

                if (dateStr == todayStr) {
                    android.util.Log.d("USAGE_FORENSIC", "[USAGE_FORENSIC] TOTAL_RAW_USAGE_MS=$rawTotalMs")
                    android.util.Log.d("USAGE_FORENSIC", "[USAGE_FORENSIC] TOTAL_AFTER_FILTERING_MS=$filteredTotalMs")
                }

                finalFilteredList.sortByDescending { it.foregroundTimeMs }
                appUsageDao.insertOrUpdateUsage(finalFilteredList)
                resultMap[dateStr] = finalFilteredList

                val h = filteredTotalMs / (1000 * 60 * 60)
                val m = (filteredTotalMs / (1000 * 60)) % 60
                android.util.Log.d(
                    "SCREEN_TIME_HISTORY",
                    "[SCREEN_TIME_HISTORY] $dateStr = $filteredTotalMs ms (${h}h ${m}m)"
                )
            }
        }

        // Calculate risk & generate recommendations for today
        val todayStr = sdfDate.format(Date())
        val todayList = resultMap[todayStr] ?: emptyList()
        val riskResult = BehavioralRiskEngine.calculateRiskScore(todayList)
        val recommendations = RecommendationEngine.generateRecommendations(riskResult)
        recommendationDao.insertRecommendations(recommendations)
        generateDailyReport(todayStr)

        return@withContext resultMap
    }

    override suspend fun calculateTodayRisk(): RiskAssessmentResult = withContext(Dispatchers.IO) {
        val todayStr = getTodayDateString()
        var usageList = appUsageDao.getUsageForDateSync(todayStr)
        if (usageList.isEmpty()) {
            usageList = refreshUsageForToday()
        }
        return@withContext BehavioralRiskEngine.calculateRiskScore(usageList)
    }

    override fun getRecommendations(): Flow<List<RecommendationEntity>> {
        return recommendationDao.getAllRecommendations()
    }

    override fun getReportForDate(date: String): Flow<ReportEntity?> {
        return reportDao.getReportForDate(date)
    }

    override suspend fun generateDailyReport(date: String): ReportEntity = withContext(Dispatchers.IO) {
        val usageList = appUsageDao.getUsageForDateSync(date)
        val riskResult = BehavioralRiskEngine.calculateRiskScore(usageList)

        val topAppsSummary = usageList.take(5).joinToString(", ") { "${it.appName} (${it.foregroundTimeMs / 60000}m)" }
        val reasonsText = riskResult.reasons.joinToString("\n• ")

        val report = ReportEntity(
            reportDate = date,
            totalScreenTimeMs = riskResult.totalScreenTimeMs,
            socialMediaTimeMs = riskResult.highRiskCategoryTimeMs,
            totalSessions = riskResult.totalSessions,
            riskScore = riskResult.score,
            riskLevel = riskResult.riskLevel.name,
            topAppsSummary = if (topAppsSummary.isBlank()) "No usage recorded" else topAppsSummary,
            riskReasons = "• $reasonsText",
            generatedAt = System.currentTimeMillis()
        )

        reportDao.insertOrUpdateReport(report)
        return@withContext report
    }

    override suspend fun syncPendingUsageWithBackend(): Boolean = withContext(Dispatchers.IO) {
        val pendingList = appUsageDao.getPendingSyncUsage()
        if (pendingList.isEmpty()) return@withContext true

        appUsageDao.markSynced(pendingList.map { it.id })
        return@withContext true
    }

    private fun getInstalledLaunchers(pm: PackageManager): Set<String> {
        val launchers = mutableSetOf<String>()
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                addCategory(android.content.Intent.CATEGORY_HOME)
            }
            val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (info in resolveInfos) {
                info.activityInfo?.packageName?.let { pkg ->
                    launchers.add(pkg)
                }
            }
        } catch (_: Exception) {}
        return launchers
    }

    private fun isUserFacingApp(pm: PackageManager, packageName: String): Boolean {
        if (packageName == selfPackageName || systemLaunchers.contains(packageName) || getInstalledLaunchers(pm).contains(packageName)) return false
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) return true
        return categoryProvider.isSocialMedia(packageName)
    }

    private fun getAppNameForPackage(pm: PackageManager, pkg: String): String {
        return try {
            val appInfo = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            pkg
        }
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }
}
