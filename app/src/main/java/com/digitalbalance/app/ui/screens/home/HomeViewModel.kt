package com.digitalbalance.app.ui.screens.home

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digitalbalance.app.data.local.entity.AppUsageEntity
import com.digitalbalance.app.data.local.entity.RecommendationEntity
import com.digitalbalance.app.domain.model.RiskAssessmentResult
import com.digitalbalance.app.domain.repository.ChildSafetyRepository
import com.digitalbalance.app.domain.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DayUsageSummary(
    val dateStr: String,
    val dayLabel: String,
    val totalScreenTimeMs: Long,
    val isToday: Boolean = false
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val hasUsagePermission: Boolean = false,
    val selectedDateStr: String = "",
    val displayedUsage: List<AppUsageEntity> = emptyList(),
    val displayedScreenTimeMs: Long = 0L,
    val todayScreenTimeMs: Long = 0L,
    val yesterdayScreenTimeMs: Long = 0L,
    val vsYesterdayText: String = "",
    val weeklySummaries: List<DayUsageSummary> = emptyList(),
    val totalSessionsCount: Int = 0,
    val avgPickupDurationMs: Long = 0L,
    val riskResult: RiskAssessmentResult? = null,
    val recommendations: List<RecommendationEntity> = emptyList(),
    val isChildModeActive: Boolean = false,
    val protectedAppsCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageRepository: UsageRepository,
    private val childSafetyRepository: ChildSafetyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var past7DaysData: Map<String, List<AppUsageEntity>> = emptyMap()

    init {
        checkPermissionAndLoadData()
    }

    fun checkPermissionAndLoadData() {
        val hasPermission = checkUsagePermission()
        _uiState.value = _uiState.value.copy(hasUsagePermission = hasPermission)

        if (hasPermission) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)

                past7DaysData = usageRepository.refreshUsageForPast7Days()

                val timeZone = java.util.TimeZone.getDefault()
                val sdfDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply { this.timeZone = timeZone }
                val sdfLabel = java.text.SimpleDateFormat("EEE", java.util.Locale.US).apply { this.timeZone = timeZone }

                val todayStr = sdfDate.format(java.util.Date())
                val calYesterday = java.util.Calendar.getInstance(timeZone).apply {
                    add(java.util.Calendar.DAY_OF_YEAR, -1)
                }
                val yesterdayStr = sdfDate.format(calYesterday.time)

                // Build weekly summaries
                val summaries = mutableListOf<DayUsageSummary>()
                for (i in 6 downTo 0) {
                    val cal = java.util.Calendar.getInstance(timeZone).apply {
                        add(java.util.Calendar.DAY_OF_YEAR, -i)
                    }
                    val dateStr = sdfDate.format(cal.time)
                    val label = if (dateStr == todayStr) "Today" else sdfLabel.format(cal.time)
                    val dayList = past7DaysData[dateStr] ?: emptyList()
                    val totalMs = dayList.sumOf { it.foregroundTimeMs }
                    summaries.add(
                        DayUsageSummary(
                            dateStr = dateStr,
                            dayLabel = label,
                            totalScreenTimeMs = totalMs,
                            isToday = (dateStr == todayStr)
                        )
                    )
                }

                val todayList = past7DaysData[todayStr] ?: emptyList()
                val yesterdayList = past7DaysData[yesterdayStr] ?: emptyList()
                val todayMs = todayList.sumOf { it.foregroundTimeMs }
                val yesterdayMs = yesterdayList.sumOf { it.foregroundTimeMs }

                val totalSessions = todayList.sumOf { it.sessionCount }
                val avgPickupMs = if (totalSessions > 0) todayMs / totalSessions else 0L
                val risk = usageRepository.calculateTodayRisk()

                val activeSelectedDate = if (_uiState.value.selectedDateStr.isBlank()) todayStr else _uiState.value.selectedDateStr
                val selectedList = past7DaysData[activeSelectedDate] ?: todayList
                val selectedTotalMs = selectedList.sumOf { it.foregroundTimeMs }

                // Calculate prev day relative to selected date
                val calSelected = java.util.Calendar.getInstance(timeZone).apply {
                    time = sdfDate.parse(activeSelectedDate) ?: java.util.Date()
                    add(java.util.Calendar.DAY_OF_YEAR, -1)
                }
                val prevDateStr = sdfDate.format(calSelected.time)
                val prevList = past7DaysData[prevDateStr] ?: emptyList()
                val prevMs = prevList.sumOf { it.foregroundTimeMs }

                val vsText = calculateVsYesterdayText(selectedTotalMs, prevMs)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedDateStr = activeSelectedDate,
                    displayedUsage = selectedList,
                    displayedScreenTimeMs = selectedTotalMs,
                    todayScreenTimeMs = todayMs,
                    yesterdayScreenTimeMs = yesterdayMs,
                    vsYesterdayText = vsText,
                    weeklySummaries = summaries,
                    totalSessionsCount = totalSessions,
                    avgPickupDurationMs = avgPickupMs,
                    riskResult = risk
                )
            }

            viewModelScope.launch {
                usageRepository.getRecommendations().collect { recs ->
                    _uiState.value = _uiState.value.copy(recommendations = recs)
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }

        viewModelScope.launch {
            childSafetyRepository.getSettings().collect { settings ->
                _uiState.value = _uiState.value.copy(isChildModeActive = settings?.childModeEnabled == true)
            }
        }

        viewModelScope.launch {
            childSafetyRepository.getProtectedApps().collect { apps ->
                _uiState.value = _uiState.value.copy(protectedAppsCount = apps.size)
            }
        }
    }

    fun selectDate(dateStr: String) {
        val timeZone = java.util.TimeZone.getDefault()
        val sdfDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply { this.timeZone = timeZone }

        val selectedList = past7DaysData[dateStr] ?: emptyList()
        val selectedTotalMs = selectedList.sumOf { it.foregroundTimeMs }

        val calSelected = java.util.Calendar.getInstance(timeZone).apply {
            time = sdfDate.parse(dateStr) ?: java.util.Date()
            add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        val prevDateStr = sdfDate.format(calSelected.time)
        val prevList = past7DaysData[prevDateStr] ?: emptyList()
        val prevMs = prevList.sumOf { it.foregroundTimeMs }

        val vsText = calculateVsYesterdayText(selectedTotalMs, prevMs)

        _uiState.value = _uiState.value.copy(
            selectedDateStr = dateStr,
            displayedUsage = selectedList,
            displayedScreenTimeMs = selectedTotalMs,
            vsYesterdayText = vsText
        )
    }

    private fun calculateVsYesterdayText(currentMs: Long, previousMs: Long): String {
        val diffMs = currentMs - previousMs
        val diffMins = diffMs / (1000 * 60)
        return when {
            diffMins < 0 -> {
                val absMins = kotlin.math.abs(diffMins)
                val h = absMins / 60
                val m = absMins % 60
                if (h > 0) "↓ ${h}h ${m}m vs yesterday" else "↓ ${m}m vs yesterday"
            }
            diffMins > 0 -> {
                val h = diffMins / 60
                val m = diffMins % 60
                if (h > 0) "↑ ${h}h ${m}m vs yesterday" else "↑ ${m}m vs yesterday"
            }
            else -> "Same as yesterday"
        }
    }

    private fun checkUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
