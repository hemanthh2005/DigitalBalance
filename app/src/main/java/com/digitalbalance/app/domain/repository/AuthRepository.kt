package com.digitalbalance.app.domain.repository

import com.digitalbalance.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

sealed class AuthResult {
    data class Success(val user: UserEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

interface AuthRepository {
    fun getCurrentUser(): Flow<UserEntity?>
    suspend fun login(email: String, pass: String): AuthResult
    suspend fun signUp(email: String, pass: String, displayName: String): AuthResult
    suspend fun logout()
    suspend fun getFirebaseIdToken(): String?
}
