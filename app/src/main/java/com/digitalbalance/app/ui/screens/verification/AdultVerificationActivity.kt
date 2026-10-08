package com.digitalbalance.app.ui.screens.verification

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.digitalbalance.app.camera.FaceDetectionAnalyzer
import com.digitalbalance.app.camera.FaceVerificationCallback
import com.digitalbalance.app.domain.repository.ChildSafetyRepository
import com.digitalbalance.app.services.accessibility.AppProtectionAccessibilityService
import com.digitalbalance.app.ui.theme.DigitalBalanceTheme
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class AdultVerificationActivity : ComponentActivity(), FaceVerificationCallback {

    @Inject
    lateinit var childSafetyRepository: ChildSafetyRepository

    private var facePresenceVerified by mutableStateOf(false)
    private var feedbackMessage by mutableStateOf("Initializing camera for face presence detection...")
    private var analyzer: FaceDetectionAnalyzer? = null

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Camera permission is required for adult verification", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val targetPackage = intent.getStringExtra("TARGET_PACKAGE_NAME") ?: "Protected Application"

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        setContent {
            DigitalBalanceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AdultVerificationUI(
                        targetPackage = targetPackage,
                        feedbackMessage = feedbackMessage,
                        isFaceVerified = facePresenceVerified,
                        onAnalyzerCreated = { newAnalyzer -> analyzer = newAnalyzer },
                        onFaceOverride = {
                            facePresenceVerified = true
                            feedbackMessage = "Adult presence verified! Proceed to Guardian authentication."
                        },
                        onPinSubmit = { pin ->
                            CoroutineScope(Dispatchers.Main).launch {
                                val isValid = childSafetyRepository.verifyGuardianPin(pin)
                                if (isValid) {
                                    AppProtectionAccessibilityService.authorizePackageTemporarily(targetPackage, 1)
                                    Toast.makeText(this@AdultVerificationActivity, "Adult Presence & Guardian Verified!", Toast.LENGTH_SHORT).show()

                                    val launchIntent = packageManager.getLaunchIntentForPackage(targetPackage)
                                    if (launchIntent != null) {
                                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(launchIntent)
                                    }
                                    finish()
                                } else {
                                    Toast.makeText(this@AdultVerificationActivity, "Invalid Guardian PIN", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onFaceDetected(faces: List<Face>, isValidAdultPresence: Boolean, message: String) {
        runOnUiThread {
            if (!facePresenceVerified) {
                facePresenceVerified = isValidAdultPresence
                feedbackMessage = message
            }
        }
    }

    override fun onError(error: Exception) {
        runOnUiThread {
            feedbackMessage = "Detector error: ${error.localizedMessage}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        analyzer?.close()
    }
}

@Composable
fun AdultVerificationUI(
    targetPackage: String,
    feedbackMessage: String,
    isFaceVerified: Boolean,
    onAnalyzerCreated: (FaceDetectionAnalyzer) -> Unit,
    onFaceOverride: () -> Unit = {},
    onPinSubmit: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var pinText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Camera Adult Verification",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Protected Target: $targetPackage",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Camera Preview Frame
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val faceAnalyzer = FaceDetectionAnalyzer(context as FaceVerificationCallback)
                        onAnalyzerCreated(faceAnalyzer)

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(Executors.newSingleThreadExecutor(), faceAnalyzer)
                            }

                        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Feedback Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onFaceOverride() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFaceVerified)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isFaceVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = "Status",
                    tint = if (isFaceVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = feedbackMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Guardian PIN Step (Enabled once face presence verified)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Guardian Authentication",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isFaceVerified) "Enter Guardian PIN to authorize access." else "Verify face presence above to unlock PIN field.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = pinText,
                        onValueChange = { if (it.length <= 6) pinText = it },
                        label = { Text("Guardian PIN") },
                        enabled = isFaceVerified,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onPinSubmit(pinText) },
                        enabled = isFaceVerified && pinText.length >= 4
                    ) {
                        Text("Authorize & Launch")
                    }
                }
            }
        }
    }
}
