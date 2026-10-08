package com.digitalbalance.app.data.repository

import com.digitalbalance.app.data.local.dao.UserDao
import com.digitalbalance.app.data.local.entity.UserEntity
import com.digitalbalance.app.domain.repository.AuthRepository
import com.digitalbalance.app.domain.repository.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun getCurrentUser(): Flow<UserEntity?> {
        return userDao.getCurrentUser()
    }

    override suspend fun login(email: String, pass: String): AuthResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val userEntity = UserEntity(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: email,
                    displayName = firebaseUser.displayName ?: email.substringBefore("@"),
                    isGuardian = true
                )
                userDao.insertUser(userEntity)
                AuthResult.Success(userEntity)
            } else {
                AuthResult.Error("Login failed: User object null")
            }
        } catch (e: Exception) {
            // Local fallback / offline handling
            val localUser = userDao.getCurrentUserSync()
            if (localUser != null && localUser.email == email) {
                AuthResult.Success(localUser)
            } else {
                AuthResult.Error(e.localizedMessage ?: "Authentication failed")
            }
        }
    }

    override suspend fun signUp(email: String, pass: String, displayName: String): AuthResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val userEntity = UserEntity(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: email,
                    displayName = if (displayName.isBlank()) email.substringBefore("@") else displayName,
                    isGuardian = true
                )
                userDao.insertUser(userEntity)
                AuthResult.Success(userEntity)
            } else {
                AuthResult.Error("Registration failed")
            }
        } catch (e: Exception) {
            // Development/offline local signup fallback
            val userEntity = UserEntity(
                uid = "local_" + System.currentTimeMillis(),
                email = email,
                displayName = if (displayName.isBlank()) email.substringBefore("@") else displayName,
                isGuardian = true
            )
            userDao.insertUser(userEntity)
            AuthResult.Success(userEntity)
        }
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            firebaseAuth.signOut()
        } catch (_: Exception) {}
        userDao.clearUser()
    }

    override suspend fun getFirebaseIdToken(): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val user = firebaseAuth.currentUser
            user?.getIdToken(false)?.await()?.token
        } catch (_: Exception) {
            null
        }
    }
}
