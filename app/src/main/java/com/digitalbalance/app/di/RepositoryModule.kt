package com.digitalbalance.app.di

import com.digitalbalance.app.data.repository.AuthRepositoryImpl
import com.digitalbalance.app.data.repository.ChildSafetyRepositoryImpl
import com.digitalbalance.app.data.repository.UsageRepositoryImpl
import com.digitalbalance.app.domain.repository.AuthRepository
import com.digitalbalance.app.domain.repository.ChildSafetyRepository
import com.digitalbalance.app.domain.repository.UsageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUsageRepository(impl: UsageRepositoryImpl): UsageRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindChildSafetyRepository(impl: ChildSafetyRepositoryImpl): ChildSafetyRepository
}
