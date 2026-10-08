package com.digitalbalance.app.camera

import android.media.Image
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions

interface FaceVerificationCallback {
    fun onFaceDetected(faces: List<Face>, isValidAdultPresence: Boolean, message: String)
    fun onError(error: Exception)
}

class FaceDetectionAnalyzer(
    private val callback: FaceVerificationCallback
) : ImageAnalysis.Analyzer {

    // High performance transient face detector configuration
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.25f)
        .build()

    private val detector: FaceDetector = FaceDetection.getClient(options)
    private var lastProcessedTimestamp = 0L
    private val frameIntervalMs = 300L // Throttle frame processing to 3.3 FPS for CPU/battery efficiency

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastProcessedTimestamp < frameIntervalMs) {
            imageProxy.close()
            return
        }

        val mediaImage: Image? = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            lastProcessedTimestamp = currentTime

            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    evaluateFacePresence(faces)
                }
                .addOnFailureListener { e ->
                    callback.onError(e)
                }
                .addOnCompleteListener {
                    // MUST ALWAYS close ImageProxy to prevent frame pipeline freeze
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun evaluateFacePresence(faces: List<Face>) {
        when {
            faces.isEmpty() -> {
                callback.onFaceDetected(
                    faces = emptyList(),
                    isValidAdultPresence = false,
                    message = "No face detected in frame. Please position your face in view."
                )
            }
            faces.size > 1 -> {
                callback.onFaceDetected(
                    faces = faces,
                    isValidAdultPresence = false,
                    message = "Multiple faces detected. Please ensure only one adult is in view."
                )
            }
            else -> {
                val face = faces.first()
                val leftEyeOpenProb = face.leftEyeOpenProbability ?: -1f
                val rightEyeOpenProb = face.rightEyeOpenProbability ?: -1f

                // Transient presence check: Face detected and eyes open
                val isEyesOpen = (leftEyeOpenProb > 0.4f || leftEyeOpenProb == -1f) &&
                                 (rightEyeOpenProb > 0.4f || rightEyeOpenProb == -1f)

                if (isEyesOpen) {
                    callback.onFaceDetected(
                        faces = faces,
                        isValidAdultPresence = true,
                        message = "Adult presence verified! Proceed to Guardian authentication."
                    )
                } else {
                    callback.onFaceDetected(
                        faces = faces,
                        isValidAdultPresence = false,
                        message = "Face detected, but eyes appear closed. Please look at the camera."
                    )
                }
            }
        }
    }

    fun close() {
        try {
            detector.close()
        } catch (_: Exception) {}
    }
}
