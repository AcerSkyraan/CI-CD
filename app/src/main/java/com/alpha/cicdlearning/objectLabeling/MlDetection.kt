package com.alpha.cicdlearning.objectLabeling
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
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
fun MlImageLabelingScreen() {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var resultLabels by remember { mutableStateOf<List<String>>(emptyList()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Load bitmap from gallery
            val bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            bitmap = bmp
            resultLabels = emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Pick Image to Label")
        }

        Spacer(modifier = Modifier.height(16.dp))

        bitmap?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                labelImageWithMlKit(context, bmp) { labels ->
                    resultLabels = labels
                }
            }) {
                Text("Run Image Labeling")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        resultLabels.forEach { label ->
            Text(label)
        }
    }
}

fun labelImageWithMlKit(
    context: Context,
    bitmap: Bitmap,
    onResult: (List<String>) -> Unit
) {
    // Create MLKit InputImage
    val image = InputImage.fromBitmap(bitmap, 0)

    // Create labeler with default options
    val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    labeler.process(image)
        .addOnSuccessListener { labels ->
            val results = labels.map { label ->
                "${label.text} ${(label.confidence * 100).toInt()}%"
            }
            onResult(results)
        }
        .addOnFailureListener { e ->
            onResult(listOf("Error: ${e.localizedMessage}"))
        }
}

