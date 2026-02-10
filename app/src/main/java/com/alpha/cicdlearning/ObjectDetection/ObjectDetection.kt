package com.alpha.cicdlearning.ObjectDetection

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions

class ObjectDetectorAnalyzer(
    private val onResults: (List<DetectedObject>, Int, Int) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector by lazy {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()

        ObjectDetection.getClient(options)
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        val width = imageProxy.width
        val height = imageProxy.height

        detector.process(image)
            .addOnSuccessListener { objects ->
                onResults(objects, width, height)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
