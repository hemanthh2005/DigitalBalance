package com.digitalbalance.app.ui.screens.childmode

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.digitalbalance.app.ui.components.BottomNavBar
import com.digitalbalance.app.ui.theme.DarkNavySurface
import com.digitalbalance.app.ui.theme.StitchAccentEmerald
import com.digitalbalance.app.ui.theme.StitchAccentTeal
import com.digitalbalance.app.ui.theme.StitchPrimaryIndigo
import com.digitalbalance.app.ui.theme.TextMutedSecondary
import com.digitalbalance.app.ui.theme.TextWhitePrimary

@Composable
fun ChildModeScreen(
    navController: NavController,
    viewModel: ChildModeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var newPinText by remember { mutableStateOf("") }

    val allCount = state.installedApps.size
    val protectedCount = state.installedApps.count { it.isProtected }
    val unprotectedCount = allCount - protectedCount

    val filteredList = state.installedApps.filter { app ->
        val matchesQuery = app.appName.contains(state.searchQuery, ignoreCase = true) ||
                app.packageName.contains(state.searchQuery, ignoreCase = true)
        val matchesTab = when (state.selectedFilter) {
            "PROTECTED" -> app.isProtected
            "UNPROTECTED" -> !app.isProtected
            else -> true
        }
        matchesQuery && matchesTab
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
            // Header Row (Exact Stitch Layout)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StitchPrimaryIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Logo",
                                tint = TextWhitePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DigitalBalance",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextWhitePrimary
                        )
                    }
                    Text(
                        text = "CHILD MODE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = StitchAccentTeal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Protect selected applications when Child Mode is active.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedSecondary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { }) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Alerts", tint = TextWhitePrimary)
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(StitchPrimaryIndigo.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile", tint = TextWhitePrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 1: Protection Active (Stitch Shield Card)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(StitchAccentTeal.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Shield",
                                    tint = StitchAccentTeal,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (state.isChildModeEnabled) StitchAccentTeal else TextMutedSecondary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (state.isChildModeEnabled) "Protection Active" else "Protection Inactive",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhitePrimary
                                    )
                                }
                                Text(
                                    text = "SYSTEM HOOK BOUND",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StitchAccentTeal
                                )
                            }
                        }

                        StitchSwitch(
                            checked = state.isChildModeEnabled,
                            onCheckedChange = { viewModel.toggleChildMode(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(StitchAccentTeal)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(StitchAccentTeal)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Service Daemon: DigitalBalance.AccessibilityService",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = StitchAccentTeal
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (state.isAccessibilityEnabled) {
                            "Accessibility service is actively intercepting protected app launches. Interceptor triggers instantly on window hierarchy changes."
                        } else {
                            "Protection Requires Accessibility Permission. Enable Accessibility Service to enforce locks."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedSecondary
                    )

                    if (!state.isAccessibilityEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StitchPrimaryIndigo),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Enable Accessibility Permission", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: Protection Verification Method Card (2 STEPS MULTI-STAGE GATING)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(StitchPrimaryIndigo.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Gating",
                                    tint = StitchPrimaryIndigo,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Protection Verification Method",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhitePrimary
                                )
                                Text(
                                    text = "MULTI-STAGE GATING",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMutedSecondary
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "2",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhitePrimary
                                )
                                Text(
                                    text = "STEPS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMutedSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step 1
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "1", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = TextWhitePrimary)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Face, contentDescription = "Face", tint = StitchAccentTeal, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Camera-Assisted Face Presence", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextWhitePrimary)
                                    }
                                    Text(text = "Validates child vs guardian proximity on screen launch", style = MaterialTheme.typography.bodySmall, color = TextMutedSecondary)
                                }
                            }
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Step 1 OK", tint = StitchAccentTeal, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step 2
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "2", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = TextWhitePrimary)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Pin, contentDescription = "PIN", tint = StitchPrimaryIndigo, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Guardian PIN Authentication", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextWhitePrimary)
                                    }
                                    Text(text = "Biometric cryptographic fall-back required per launch", style = MaterialTheme.typography.bodySmall, color = TextMutedSecondary)
                                }
                            }
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Step 2 Lock", tint = StitchPrimaryIndigo, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StitchPrimaryIndigo.copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Note", tint = StitchPrimaryIndigo, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Protected applications require verification before access. Verification is required each time an app is launched.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMutedSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // PIN Setup Bar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newPinText,
                            onValueChange = { if (it.length <= 6) newPinText = it },
                            label = { Text("Guardian PIN (4-6 digits)") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StitchPrimaryIndigo,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = StitchPrimaryIndigo,
                                unfocusedLabelColor = TextMutedSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (newPinText.length >= 4) {
                                    viewModel.setGuardianPin(newPinText)
                                    newPinText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StitchPrimaryIndigo),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(54.dp)
                        ) {
                            Text("Set PIN", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 3: Diagnostics Sandbox (Stitch Test Flow Button)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(context, RestrictionOverlayActivity::class.java).apply {
                            putExtra("BLOCKED_PACKAGE_NAME", "com.google.android.youtube")
                        }
                        context.startActivity(intent)
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = StitchAccentTeal, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(text = "DIAGNOSTICS SANDBOX", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = StitchAccentTeal)
                            Text(text = "Test Protection Flow", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextWhitePrimary)
                            Text(text = "Simulate Child Opening YouTube & verify Lock Overlay", style = MaterialTheme.typography.bodySmall, color = TextMutedSecondary)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Execute", tint = TextWhitePrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search installed applications...", color = TextMutedSecondary) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextMutedSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StitchPrimaryIndigo,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = StitchPrimaryIndigo,
                    unfocusedLabelColor = TextMutedSecondary,
                    focusedPlaceholderColor = TextMutedSecondary,
                    unfocusedPlaceholderColor = TextMutedSecondary,
                    focusedLeadingIconColor = TextMutedSecondary,
                    unfocusedLeadingIconColor = TextMutedSecondary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips (Stitch Chips: All 24 | Protected 3 | Unprotected 4)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipItem(
                    label = "All",
                    count = allCount,
                    isSelected = state.selectedFilter == "ALL",
                    onClick = { viewModel.setFilter("ALL") }
                )
                FilterChipItem(
                    label = "Protected",
                    count = protectedCount,
                    isSelected = state.selectedFilter == "PROTECTED",
                    hasDot = true,
                    onClick = { viewModel.setFilter("PROTECTED") }
                )
                FilterChipItem(
                    label = "Unprotected",
                    count = unprotectedCount,
                    isSelected = state.selectedFilter == "UNPROTECTED",
                    onClick = { viewModel.setFilter("UNPROTECTED") }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section Header: DISCOVERED NATIVE APPS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DISCOVERED NATIVE APPS (${filteredList.size} TARGET VECTORS)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextMutedSecondary
                )
                Text(
                    text = "AUTOMATIC SYNC",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = StitchAccentTeal
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Discovered Apps List — Individual Cards per Application (Exact Stitch Layout)
            if (filteredList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (state.searchQuery.isBlank()) "Discovering installed applications..." else "No applications match filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMutedSecondary
                        )
                    }
                }
            } else {
                filteredList.forEach { app ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                AppIconImage(
                                    packageName = app.packageName,
                                    appName = app.appName,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = app.appName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhitePrimary
                                        )
                                        if (app.isProtected) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(StitchAccentTeal.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "ACTIVE",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = StitchAccentTeal
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = app.category,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextMutedSecondary
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextMutedSecondary.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            StitchSwitch(
                                checked = app.isProtected,
                                onCheckedChange = { protect ->
                                    viewModel.toggleAppProtection(app.packageName, app.appName, protect)
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun AppIconImage(packageName: String, appName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember(packageName) {
        try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            drawable.toBitmap(width = 96, height = 96).asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = appName,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(StitchPrimaryIndigo.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = appName.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextWhitePrimary
            )
        }
    }
}

@Composable
fun StitchSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor = if (checked) StitchAccentTeal else MaterialTheme.colorScheme.surfaceVariant
    val thumbOffset by animateDpAsState(targetValue = if (checked) 22.dp else 2.dp, label = "stitch_switch")

    Box(
        modifier = modifier
            .width(52.dp)
            .height(30.dp)
            .clip(CircleShape)
            .background(trackColor)
            .clickable { onCheckedChange(!checked) }
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(26.dp)
                .clip(CircleShape)
                .background(if (checked) Color(0xFF0F172A) else Color(0xFF64748B)),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Active",
                    tint = StitchAccentTeal,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(2.dp)
                        .background(Color(0xFF1E293B))
                )
            }
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    count: Int,
    isSelected: Boolean,
    hasDot: Boolean = false,
    onClick: () -> Unit
) {
    val bg = if (isSelected) StitchAccentTeal else DarkNavySurface
    val textColor = if (isSelected) Color(0xFF0F172A) else TextWhitePrimary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (hasDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF0F172A) else StitchAccentTeal)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color(0xFF0F172A).copy(alpha = 0.2f)
                        else Color.White.copy(alpha = 0.15f)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}
