package com.digitalbalance.app.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digitalbalance.app.data.local.entity.AppUsageEntity
import com.digitalbalance.app.domain.model.RiskAssessmentResult
import com.digitalbalance.app.domain.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InsightsUiState(
    val isLoading: Boolean = true,
    val usageList: List<AppUsageEntity> = emptyList(),
    val riskResult: RiskAssessmentResult? = null
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val usageRepository: UsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        loadInsights()
    }

    fun loadInsights() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            usageRepository.getTodayUsage().collect { usage ->
                val risk = usageRepository.calculateTodayRisk()
                _uiState.value = InsightsUiState(
                    isLoading = false,
                    usageList = usage,
                    riskResult = risk
                )
            }
        }
    }
}
