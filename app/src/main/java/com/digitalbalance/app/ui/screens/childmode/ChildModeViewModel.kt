package com.digitalbalance.app.ui.screens.childmode

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.TextUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digitalbalance.app.data.local.entity.ProtectedAppEntity
import com.digitalbalance.app.domain.repository.ChildSafetyRepository
import com.digitalbalance.app.services.accessibility.AppProtectionAccessibilityService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.digitalbalance.app.services.usage.AppCategoryProvider
import javax.inject.Inject

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val category: String,
    val isProtected: Boolean
)

data class ChildModeUiState(
    val isChildModeEnabled: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val protectedApps: List<ProtectedAppEntity> = emptyList(),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "ALL", // "ALL", "PROTECTED", "UNPROTECTED"
    val guardianPinSet: Boolean = false,
    val protectedCount: Int = 0
)

@HiltViewModel
class ChildModeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val childSafetyRepository: ChildSafetyRepository,
    private val categoryProvider: AppCategoryProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildModeUiState())
    val uiState: StateFlow<ChildModeUiState> = _uiState.asStateFlow()

    init {
        loadChildModeState()
    }

    fun loadChildModeState() {
        viewModelScope.launch {
            childSafetyRepository.getSettings().collect { settings ->
                _uiState.value = _uiState.value.copy(
                    isChildModeEnabled = settings?.childModeEnabled == true,
                    guardianPinSet = !settings?.guardianPinHash.isNullOrBlank(),
                    isAccessibilityEnabled = isAccessibilityServiceEnabled()
                )
            }
        }

        viewModelScope.launch {
            childSafetyRepository.getProtectedApps().collect { protectedList ->
                val protectedMap = protectedList.associateBy { it.packageName }
                _uiState.value = _uiState.value.copy(
                    protectedApps = protectedList,
                    protectedCount = protectedList.filter { it.isProtected }.size
                )
                refreshInstalledApps(protectedMap)
            }
        }
    }

    private fun refreshInstalledApps(protectedMap: Map<String, ProtectedAppEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolveInfos = pm.queryIntentActivities(intent, 0)
            val appMap = mutableMapOf<String, InstalledAppInfo>()

            for (info in resolveInfos) {
                val pkg = info.activityInfo.packageName
                if (pkg == context.packageName) continue

                val appName = info.loadLabel(pm).toString()
                val isProtected = protectedMap[pkg]?.isProtected == true
                val cat = categoryProvider.getCategory(pkg).name.lowercase().replaceFirstChar { it.uppercase() }

                if (!appMap.containsKey(pkg)) {
                    appMap[pkg] = InstalledAppInfo(
                        packageName = pkg,
                        appName = appName,
                        category = cat,
                        isProtected = isProtected
                    )
                }
            }

            val sortedList = appMap.values.sortedBy { it.appName.lowercase() }
            _uiState.value = _uiState.value.copy(installedApps = sortedList)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun toggleAppProtection(packageName: String, appName: String, protect: Boolean) {
        viewModelScope.launch {
            if (protect) {
                childSafetyRepository.addProtectedApp(packageName, appName, 60)
            } else {
                childSafetyRepository.removeProtectedApp(packageName)
            }
        }
    }

    fun toggleChildMode(enabled: Boolean) {
        viewModelScope.launch {
            childSafetyRepository.toggleChildMode(enabled)
            loadChildModeState()
        }
    }

    fun setGuardianPin(pin: String) {
        viewModelScope.launch {
            childSafetyRepository.setGuardianPin(pin)
            loadChildModeState()
        }
    }

    suspend fun verifyGuardianPin(pin: String): Boolean {
        return childSafetyRepository.verifyGuardianPin(pin)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedService = "${context.packageName}/${AppProtectionAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(expectedService, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
