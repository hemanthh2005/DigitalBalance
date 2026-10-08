package com.digitalbalance.app.ui.screens.reports

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.digitalbalance.app.domain.model.RiskLevel
import com.digitalbalance.app.ui.components.BottomNavBar
import com.digitalbalance.app.ui.components.RiskBadge
import com.digitalbalance.app.ui.theme.StitchAccentEmerald
import com.digitalbalance.app.ui.theme.StitchPrimaryIndigo
import com.digitalbalance.app.ui.theme.TextMutedSecondary
import com.digitalbalance.app.ui.theme.TextWhitePrimary

@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }

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
                text = "Digital Wellbeing Reports",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhitePrimary
            )
            Text(
                text = "Daily aggregated pattern analysis & PDF export",
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
                val report = state.todayReport

                if (report != null) {
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
                                    text = "Daily Summary (${report.reportDate})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhitePrimary
                                )
                                val level = try { RiskLevel.valueOf(report.riskLevel) } catch (_: Exception) { RiskLevel.LOW }
                                RiskBadge(riskLevel = level, score = report.riskScore)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val screenMins = report.totalScreenTimeMs / (1000 * 60)
                            val socialMins = report.socialMediaTimeMs / (1000 * 60)

                            ReportMetricRow("Total Screen Time:", "${screenMins / 60}h ${screenMins % 60}m")
                            ReportMetricRow("Social & Video Time:", "${socialMins / 60}h ${socialMins % 60}m")
                            ReportMetricRow("Total App Open Sessions:", "${report.totalSessions} opens")
                            ReportMetricRow("Top Used Apps:", report.topAppsSummary)

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Observed Behavioral Indicators:",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = report.riskReasons,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMutedSecondary
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Download / Export PDF Button
                            Button(
                                onClick = { viewModel.downloadAndOpenPdfReport(context) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StitchPrimaryIndigo)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = "Download PDF",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text(
                                        text = "Download PDF Report",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                                    contentDescription = "Academic Note",
                                    tint = StitchPrimaryIndigo,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = "Academic Evaluation Note",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhitePrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "This report evaluates digital-wellbeing risk using transparent rule-based metrics. Downloaded PDF files are formatted for academic viva demonstration.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMutedSecondary
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No report generated yet for today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMutedSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ReportMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMutedSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = TextWhitePrimary
        )
    }
}

