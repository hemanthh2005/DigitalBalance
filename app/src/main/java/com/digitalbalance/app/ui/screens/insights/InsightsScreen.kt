package com.digitalbalance.app.ui.screens.insights

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.digitalbalance.app.data.local.entity.AppUsageEntity
import com.digitalbalance.app.domain.model.RiskAssessmentResult
import com.digitalbalance.app.ui.components.BottomNavBar
import com.digitalbalance.app.ui.components.RiskBadge
import com.digitalbalance.app.ui.theme.RiskHighColor
import com.digitalbalance.app.ui.theme.StitchAccentEmerald
import com.digitalbalance.app.ui.theme.StitchAccentTeal
import com.digitalbalance.app.ui.theme.StitchPrimaryIndigo
import com.digitalbalance.app.ui.theme.TextMutedSecondary
import com.digitalbalance.app.ui.theme.TextWhitePrimary

@Composable
fun InsightsScreen(
    navController: NavController,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Behavioral Analytics & Risk Insights",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhitePrimary
            )
            Text(
                text = "Explainable indicators computed from smartphone usage",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMutedSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = StitchPrimaryIndigo)
                }
            } else {
                val risk = state.riskResult

                if (risk != null) {
                    // Overall Risk Assessment Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Overall Behavioral Risk",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhitePrimary
                                )
                                RiskBadge(riskLevel = risk.riskLevel, score = risk.score)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Indicator 1: Screen Time Penalty (Max 40)
                            IndicatorRow(
                                label = "Indicator 1: Total Screen Time Penalty",
                                currentPenalty = risk.screenTimePenalty,
                                maxPenalty = 40,
                                detail = "${(risk.totalScreenTimeMs / (1000 * 60 * 60))}h ${((risk.totalScreenTimeMs / (1000 * 60)) % 60)}m total screen time"
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Indicator 2: High-Risk Category Share Penalty (Max 30)
                            IndicatorRow(
                                label = "Indicator 2: High-Risk Category Share Penalty",
                                currentPenalty = risk.categorySharePenalty,
                                maxPenalty = 30,
                                detail = "${risk.highRiskPercentage.toInt()}% Social/Gaming/Video Share"
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Indicator 3: Session Frequency Penalty (Max 30)
                            IndicatorRow(
                                label = "Indicator 3: Session Launch Frequency Penalty",
                                currentPenalty = risk.frequencyPenalty,
                                maxPenalty = 30,
                                detail = "${risk.totalSessions} Foreground App Opens"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // VISUAL CHART 1: App Category Distribution Chart
                    CategoryDistributionChartCard(usageList = state.usageList)

                    Spacer(modifier = Modifier.height(16.dp))

                    // VISUAL CHART 2: Top Applications Horizontal Bar Chart
                    TopAppsBarChartCard(usageList = state.usageList)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Explainable AI Reasons Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Explainability",
                                    tint = StitchPrimaryIndigo,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Risk Assessment Explanation",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhitePrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            risk.reasons.forEach { reason ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "• ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = StitchPrimaryIndigo
                                    )
                                    Text(
                                        text = reason,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextWhitePrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Tracked Applications List Breakdown
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Tracked Applications Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhitePrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (state.usageList.isEmpty()) {
                            Text(
                                text = "No application usage tracked yet for today.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMutedSecondary
                            )
                        } else {
                            state.usageList.forEachIndexed { index, usage ->
                                val appMins = usage.foregroundTimeMs / (1000 * 60)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(StitchPrimaryIndigo.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = usage.appName.take(1).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                color = StitchPrimaryIndigo
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = usage.appName,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                                color = TextWhitePrimary
                                            )
                                            Text(
                                                text = "${usage.category} • ${usage.sessionCount} launches",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = TextMutedSecondary
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${appMins}m",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = StitchAccentEmerald
                                    )
                                }

                                if (index < state.usageList.size - 1) {
                                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDistributionChartCard(usageList: List<AppUsageEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = "Category Share",
                    tint = StitchPrimaryIndigo,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Category Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhitePrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val totalMs = usageList.sumOf { it.foregroundTimeMs }
            if (totalMs <= 0L) {
                Text(
                    text = "No category usage available for today.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMutedSecondary
                )
            } else {
                val categoryGroups = usageList.groupBy { it.category }
                val categoryShares = categoryGroups.mapValues { (_, apps) ->
                    val catMs = apps.sumOf { it.foregroundTimeMs }
                    (catMs.toFloat() / totalMs.toFloat())
                }.toList().sortedByDescending { it.second }

                categoryShares.forEach { (catName, shareFraction) ->
                    val percentage = (shareFraction * 100f).toInt()
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = catName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhitePrimary
                            )
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = StitchAccentEmerald
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = shareFraction.coerceIn(0f, 1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when (catName) {
                                "SOCIAL" -> RiskHighColor
                                "VIDEO" -> StitchAccentTeal
                                "GAMING" -> StitchPrimaryIndigo
                                else -> StitchAccentEmerald
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopAppsBarChartCard(usageList: List<AppUsageEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Top Apps Chart",
                    tint = StitchPrimaryIndigo,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Top Used Applications Chart",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhitePrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val maxTimeMs = usageList.maxOfOrNull { it.foregroundTimeMs } ?: 0L
            if (maxTimeMs <= 0L) {
                Text(
                    text = "No app usage chart data available yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMutedSecondary
                )
            } else {
                usageList.take(5).forEach { app ->
                    val fraction = (app.foregroundTimeMs.toFloat() / maxTimeMs.toFloat()).coerceIn(0.05f, 1f)
                    val mins = app.foregroundTimeMs / (1000 * 60)

                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = app.appName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = TextWhitePrimary
                            )
                            Text(
                                text = "${mins}m",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = StitchAccentEmerald
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = fraction,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = StitchPrimaryIndigo,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IndicatorRow(
    label: String,
    currentPenalty: Int,
    maxPenalty: Int,
    detail: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextWhitePrimary
            )
            Text(
                text = "-$currentPenalty / -$maxPenalty pts",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (currentPenalty > 10) RiskHighColor else TextWhitePrimary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = (currentPenalty.toFloat() / maxPenalty.toFloat()).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (currentPenalty > 10) RiskHighColor else StitchPrimaryIndigo,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = detail,
            style = MaterialTheme.typography.labelMedium,
            color = TextMutedSecondary
        )
    }
}

