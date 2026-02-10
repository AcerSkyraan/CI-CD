package com.alpha.cicdlearning.ObjectDetection




import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.DetectedObject


@Composable
fun ObjectDetectionScreen() {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var objects by remember { mutableStateOf<List<DetectedObject>>(emptyList()) }
    var analyzerWidth by remember { mutableStateOf(1) }
    var analyzerHeight by remember { mutableStateOf(1) }

    Box(Modifier.fillMaxSize()) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->

                val previewView = PreviewView(ctx)

                val cameraProviderFuture =
                    ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({

                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    val analyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(
                            ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                        )
                        .build()

                    analyzer.setAnalyzer(
                        ContextCompat.getMainExecutor(ctx),
                        ObjectDetectorAnalyzer { results, w, h ->
                            objects = results
                            analyzerWidth = w
                            analyzerHeight = h
                        }
                    )

                    val selector = CameraSelector.DEFAULT_BACK_CAMERA

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        analyzer
                    )

                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // DRAW BOXES
        Canvas(modifier = Modifier.fillMaxSize()) {

            // try normal scale
            val scaleX = size.width / analyzerWidth
            val scaleY = size.height / analyzerHeight

            objects.forEach { obj ->
                val box = obj.boundingBox

                val left = box.left * scaleX
                val top = box.top * scaleY
                val right = box.right * scaleX
                val bottom = box.bottom * scaleY

                drawRect(
                    color = Color.Red,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    style = Stroke(width = 5f)
                )

                obj.labels.firstOrNull()?.let { label ->
                    drawContext.canvas.nativeCanvas.drawText(
                        "${label.text} ${(label.confidence * 100).toInt()}%",
                        left,
                        top - 10,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.RED
                            textSize = 48f
                        }
                    )
                }
            }
        }
    }
}


