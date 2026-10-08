package com.digitalbalance.app.ui.screens.verification

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@Composable
fun AdultVerificationScreen(navController: NavController) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val intent = Intent(context, AdultVerificationActivity::class.java).apply {
            putExtra("TARGET_PACKAGE_NAME", "Sample Social Media App")
        }
        context.startActivity(intent)
        navController.popBackStack()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
