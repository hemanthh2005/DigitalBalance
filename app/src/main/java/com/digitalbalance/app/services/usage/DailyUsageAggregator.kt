package com.digitalbalance.app.services.usage

import com.digitalbalance.app.data.local.entity.AppUsageEntity
import com.digitalbalance.app.data.local.entity.SessionEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyUsageAggregator @Inject constructor(
    private val categoryProvider: AppCategoryProvider
) {
    fun aggregateSessionsToDailyUsage(
        date: String,
        sessions: List<SessionEntity>
    ): List<AppUsageEntity> {
        val groupedByPackage = sessions.groupBy { it.packageName }

        return groupedByPackage.map { (pkg, appSessions) ->
            val appName = appSessions.firstOrNull()?.appName ?: pkg
            val totalForegroundTimeMs = appSessions.sumOf { it.durationMs }
            val sessionCount = appSessions.size
            val category = categoryProvider.getCategory(pkg)
            val isSocial = categoryProvider.isSocialMedia(pkg)

            AppUsageEntity(
                date = date,
                packageName = pkg,
                appName = appName,
                category = category.name,
                foregroundTimeMs = totalForegroundTimeMs,
                sessionCount = sessionCount,
                isSocialMedia = isSocial,
                isSyncPending = true,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
