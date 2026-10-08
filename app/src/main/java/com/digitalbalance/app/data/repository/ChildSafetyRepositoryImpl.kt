package com.digitalbalance.app.data.repository

import com.digitalbalance.app.data.local.dao.ProtectedAppDao
import com.digitalbalance.app.data.local.dao.SettingsDao
import com.digitalbalance.app.data.local.entity.ProtectedAppEntity
import com.digitalbalance.app.data.local.entity.SettingsEntity
import com.digitalbalance.app.domain.repository.ChildSafetyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChildSafetyRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao,
    private val protectedAppDao: ProtectedAppDao
) : ChildSafetyRepository {

    override fun getSettings(): Flow<SettingsEntity?> {
        return settingsDao.getSettings()
    }

    override fun getProtectedApps(): Flow<List<ProtectedAppEntity>> {
        return protectedAppDao.getAllProtectedApps()
    }

    override suspend fun toggleChildMode(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        val currentSettings = settingsDao.getSettingsSync() ?: SettingsEntity()
        val updated = currentSettings.copy(childModeEnabled = enabled)
        settingsDao.updateSettings(updated)
        return@withContext true
    }

    override suspend fun setGuardianPin(pin: String): Boolean = withContext(Dispatchers.IO) {
        if (pin.length < 4) return@withContext false
        val pinHash = hashPin(pin)
        val currentSettings = settingsDao.getSettingsSync() ?: SettingsEntity()
        settingsDao.updateSettings(currentSettings.copy(guardianPinHash = pinHash))
        return@withContext true
    }

    override suspend fun verifyGuardianPin(pin: String): Boolean = withContext(Dispatchers.IO) {
        val currentSettings = settingsDao.getSettingsSync()
        val storedHash = currentSettings?.guardianPinHash ?: ""
        if (storedHash.isBlank()) {
            // Default pin for initial demo if unset is "1234"
            return@withContext pin == "1234" || hashPin(pin) == hashPin("1234")
        }
        return@withContext hashPin(pin) == storedHash
    }

    override suspend fun addProtectedApp(packageName: String, appName: String, limitMinutes: Int) = withContext(Dispatchers.IO) {
        val entity = ProtectedAppEntity(
            packageName = packageName,
            appName = appName,
            isProtected = true,
            dailyLimitMinutes = limitMinutes,
            requiresAdultVerification = true
        )
        protectedAppDao.insertOrUpdateProtectedApp(entity)
    }

    override suspend fun removeProtectedApp(packageName: String) = withContext(Dispatchers.IO) {
        protectedAppDao.removeProtectedApp(packageName)
    }

    override suspend fun isAppProtected(packageName: String): Boolean = withContext(Dispatchers.IO) {
        val settings = settingsDao.getSettingsSync()
        if (settings?.childModeEnabled != true) return@withContext false

        val app = protectedAppDao.getProtectedApp(packageName)
        return@withContext app?.isProtected == true
    }

    private fun hashPin(pin: String): String {
        return try {
            val salt = "DigitalBalanceGuardianSalt2026".toByteArray()
            val spec = PBEKeySpec(pin.toCharArray(), salt, 10000, 256)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hash = factory.generateSecret(spec).encoded
            hash.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
