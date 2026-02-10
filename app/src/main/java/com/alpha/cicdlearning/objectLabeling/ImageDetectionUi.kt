package com.alpha.cicdlearning.objectLabeling

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions


@Composable
fun ImageLabelScreen() {
    val context = LocalContext.current

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var labels by remember { mutableStateOf<List<String>>(emptyList()) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val stream = context.contentResolver.openInputStream(uri)
            bitmap = BitmapFactory.decodeStream(stream)
            labels = emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(onClick = { picker.launch("image/*") }) {
            Text("Pick Image")
        }

        Spacer(Modifier.height(16.dp))

        bitmap?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

            Spacer(Modifier.height(16.dp))

            Button(onClick = {
                runImageLabeling(context, bmp) { result ->
                    labels = result
                }
            }) {
                Text("Detect Objects")
            }
        }

        Spacer(Modifier.height(16.dp))

        labels.forEach {
            Text(it)
        }
    }
}


fun runImageLabeling(
    context: Context,
    bitmap: Bitmap,
    onResult: (List<String>) -> Unit
) {
    val image = InputImage.fromBitmap(bitmap, 0)

    val options = ImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.6f)
        .build()

    val labeler = ImageLabeling.getClient(options)

    labeler.process(image)
        .addOnSuccessListener { labels ->
            val results = labels.map {
                "${it.text} ${(it.confidence * 100).toInt()}%"
            }
            onResult(results)
        }
        .addOnFailureListener {
            onResult(listOf("Failed to detect"))
        }
}
