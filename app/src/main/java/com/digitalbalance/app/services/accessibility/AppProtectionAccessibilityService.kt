package com.digitalbalance.app.services.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.digitalbalance.app.domain.repository.ChildSafetyRepository
import com.digitalbalance.app.ui.screens.childmode.RestrictionOverlayActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppProtectionAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var childSafetyRepository: ChildSafetyRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private var authorizedPackageForLaunch: String? = null

        /**
         * Authorizes a package for a single foreground launch.
         * Requirement 8 & 9: Once launched or switched away, authorization is immediately cleared
         * so every subsequent launch requires adult verification again.
         */
        fun authorizePackageTemporarily(packageName: String, durationMinutes: Int = 15) {
            authorizedPackageForLaunch = packageName
        }

        fun clearAuthorization() {
            authorizedPackageForLaunch = null
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        // NEVER block DigitalBalance itself, launcher, or system UI
        if (packageName == applicationContext.packageName ||
            packageName.contains("android.launcher") ||
            packageName.contains("systemui")
        ) {
            return
        }

        // Single-Launch Authorization check:
        if (packageName == authorizedPackageForLaunch) {
            // App is launching after successful adult verification!
            // Immediately clear authorization so next launch requires verification again.
            authorizedPackageForLaunch = null
            return
        }

        serviceScope.launch(Dispatchers.IO) {
            val isProtected = childSafetyRepository.isAppProtected(packageName)
            if (isProtected) {
                // Launch Restriction Overlay Activity
                val intent = Intent(applicationContext, RestrictionOverlayActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("BLOCKED_PACKAGE_NAME", packageName)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {}
}
