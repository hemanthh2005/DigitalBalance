package com.digitalbalance.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.digitalbalance.app.data.local.dao.AppUsageDao
import com.digitalbalance.app.data.local.dao.NotificationDao
import com.digitalbalance.app.data.local.dao.ProtectedAppDao
import com.digitalbalance.app.data.local.dao.RecommendationDao
import com.digitalbalance.app.data.local.dao.ReportDao
import com.digitalbalance.app.data.local.dao.SessionDao
import com.digitalbalance.app.data.local.dao.SettingsDao
import com.digitalbalance.app.data.local.dao.UserDao
import com.digitalbalance.app.data.local.entity.AppUsageEntity
import com.digitalbalance.app.data.local.entity.NotificationEntity
import com.digitalbalance.app.data.local.entity.ProtectedAppEntity
import com.digitalbalance.app.data.local.entity.RecommendationEntity
import com.digitalbalance.app.data.local.entity.ReportEntity
import com.digitalbalance.app.data.local.entity.SessionEntity
import com.digitalbalance.app.data.local.entity.SettingsEntity
import com.digitalbalance.app.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        AppUsageEntity::class,
        SessionEntity::class,
        RecommendationEntity::class,
        ReportEntity::class,
        ProtectedAppEntity::class,
        NotificationEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DigitalBalanceDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun appUsageDao(): AppUsageDao
    abstract fun sessionDao(): SessionDao
    abstract fun recommendationDao(): RecommendationDao
    abstract fun reportDao(): ReportDao
    abstract fun protectedAppDao(): ProtectedAppDao
    abstract fun notificationDao(): NotificationDao
    abstract fun settingsDao(): SettingsDao
}
