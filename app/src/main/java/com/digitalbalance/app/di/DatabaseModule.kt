package com.digitalbalance.app.di

import android.content.Context
import androidx.room.Room
import com.digitalbalance.app.data.local.DigitalBalanceDatabase
import com.digitalbalance.app.data.local.dao.AppUsageDao
import com.digitalbalance.app.data.local.dao.NotificationDao
import com.digitalbalance.app.data.local.dao.ProtectedAppDao
import com.digitalbalance.app.data.local.dao.RecommendationDao
import com.digitalbalance.app.data.local.dao.ReportDao
import com.digitalbalance.app.data.local.dao.SessionDao
import com.digitalbalance.app.data.local.dao.SettingsDao
import com.digitalbalance.app.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DigitalBalanceDatabase {
        return Room.databaseBuilder(
            context,
            DigitalBalanceDatabase::class.java,
            "digital_balance.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserDao(db: DigitalBalanceDatabase): UserDao = db.userDao()

    @Provides
    fun provideAppUsageDao(db: DigitalBalanceDatabase): AppUsageDao = db.appUsageDao()

    @Provides
    fun provideSessionDao(db: DigitalBalanceDatabase): SessionDao = db.sessionDao()

    @Provides
    fun provideRecommendationDao(db: DigitalBalanceDatabase): RecommendationDao = db.recommendationDao()

    @Provides
    fun provideReportDao(db: DigitalBalanceDatabase): ReportDao = db.reportDao()

    @Provides
    fun provideProtectedAppDao(db: DigitalBalanceDatabase): ProtectedAppDao = db.protectedAppDao()

    @Provides
    fun provideNotificationDao(db: DigitalBalanceDatabase): NotificationDao = db.notificationDao()

    @Provides
    fun provideSettingsDao(db: DigitalBalanceDatabase): SettingsDao = db.settingsDao()
}
