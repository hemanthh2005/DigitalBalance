package com.digitalbalance.app.ui.screens.reports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digitalbalance.app.data.local.dao.ReportDao
import com.digitalbalance.app.data.local.entity.AppUsageEntity
import com.digitalbalance.app.data.local.entity.ProtectedAppEntity
import com.digitalbalance.app.data.local.entity.ReportEntity
import com.digitalbalance.app.domain.repository.ChildSafetyRepository
import com.digitalbalance.app.domain.repository.UsageRepository
import com.digitalbalance.app.utils.PdfReportGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ReportsUiState(
    val isLoading: Boolean = true,
    val todayReport: ReportEntity? = null,
    val usageList: List<AppUsageEntity> = emptyList(),
    val protectedApps: List<ProtectedAppEntity> = emptyList(),
    val recentReports: List<ReportEntity> = emptyList(),
    val statusMessage: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val reportDao: ReportDao,
    private val usageRepository: UsageRepository,
    private val childSafetyRepository: ChildSafetyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadReports()
    }

    fun loadReports() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            usageRepository.generateDailyReport(todayStr)

            reportDao.getReportForDate(todayStr).collect { report ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    todayReport = report
                )
            }
        }

        viewModelScope.launch {
            usageRepository.getTodayUsage().collect { usage ->
                _uiState.value = _uiState.value.copy(usageList = usage)
            }
        }

        viewModelScope.launch {
            childSafetyRepository.getProtectedApps().collect { apps ->
                _uiState.value = _uiState.value.copy(protectedApps = apps)
            }
        }

        viewModelScope.launch {
            reportDao.getRecentReports().collect { reports ->
                _uiState.value = _uiState.value.copy(recentReports = reports)
            }
        }
    }

    fun downloadAndOpenPdfReport(context: Context) {
        val report = _uiState.value.todayReport ?: return
        val usage = _uiState.value.usageList
        val protectedList = _uiState.value.protectedApps

        viewModelScope.launch {
            val file = PdfReportGenerator.generateAndSavePdfReport(
                context = context,
                report = report,
                usageList = usage,
                protectedApps = protectedList
            )

            if (file != null && file.exists()) {
                _uiState.value = _uiState.value.copy(statusMessage = "Report PDF saved to Downloads!")
                PdfReportGenerator.openOrSharePdf(context, file)
            } else {
                _uiState.value = _uiState.value.copy(statusMessage = "Failed to generate PDF file.")
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }
}
