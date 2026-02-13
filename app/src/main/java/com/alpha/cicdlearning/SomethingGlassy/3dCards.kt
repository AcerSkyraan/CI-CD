package com.alpha.cicdlearning.SomethingGlassy



import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.alpha.cicdlearning.R
import com.example.pacman.Position
import com.google.android.filament.Engine
import io.github.sceneview.Scene
import io.github.sceneview.SceneView
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.node.LightNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberRenderer
import io.github.sceneview.rememberScene
import io.github.sceneview.rememberView
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


@Composable
fun Draggable3DCard() {

    var rotX by remember { mutableStateOf(0f) }
    var rotY by remember { mutableStateOf(0f) }

    // smooth reset animation
    val animX by animateFloatAsState(rotX, label = "")
    val animY by animateFloatAsState(rotY, label = "")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010)),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(width = 200.dp, height = 280.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            // return to flat
                            rotX = 0f
                            rotY = 0f
                        }
                    ) { change, drag ->

                        change.consume()

                        rotY += drag.x * 0.4f
                        rotX -= drag.y * 0.4f

                        rotX = rotX.coerceIn(-45f, 45f)
                        rotY = rotY.coerceIn(-45f, 45f)
                    }
                }
                .graphicsLayer {
                    rotationX = animX
                    rotationY = animY
                    cameraDistance = 12 * density
                }
        )
        {

            Card(
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Image(
                        painter = painterResource(R.drawable.colorone),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "3D Card",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Drag Me",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}






@Composable
fun Model3DScreen0() {

    val context = LocalContext.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->

            val sceneView = SceneView(ctx)

            val engine = Engine.create()
            val modelLoader = ModelLoader(engine, ctx)

            val modelInstance =
                modelLoader.createModelInstance("duckkt.glb")

            val modelNode = ModelNode(
                modelInstance = modelInstance
            ).apply {
                scale = io.github.sceneview.math.Scale(1.0f)
                //centerOrigin = true
            }

            sceneView.addChildNode(modelNode)

            // Auto frame camera
            sceneView.cameraNode.lookAt(modelNode)

            sceneView
        }
    )
}


/*@Composable
fun Model3DScreen2() {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    // Camera state for orbiting, zooming, panning
    val cameraManipulator = rememberCameraManipulator()

    // Model node at the origin
    val modelNode = rememberNodes {
        add(
            ModelNode(
                modelInstance = modelLoader.createModelInstance(
                    assetFileLocation = "duckkt.glb"
                ),
                scaleToUnits = 1.0f,
               // position = Position(0f, 0f, 0f)
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scene(
            modifier = Modifier
                .fillMaxSize()
                // Gesture detector for pinch/rotate/drag
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        // Rotate camera around the model
                        cameraManipulator.rotate(rotation)
                        // Pan camera
                        cameraManipulator.pan(pan.x, pan.y)
                        // Zoom camera
                        cameraManipulator.zoom(zoom)
                    }
                },
            childNodes = modelNode,
            engine = engine,
            cameraManipulator = cameraManipulator
        )

        // Optional: Control panel for resetting camera
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp)
        ) {
            Button(onClick = { cameraManipulator.reset() }) {
                Text("Reset Camera")
            }
        }
    }
}*/

@Composable
fun Model3DScreen() {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    // Default camera manipulator (enabled by SceneView)
    val cameraManipulator = rememberCameraManipulator()

    val modelNodes = rememberNodes {
        add(
            ModelNode(
                modelInstance = modelLoader.createModelInstance(
                    assetFileLocation = "duckkt.glb"
                ),
                scaleToUnits = 1.0f
            )
        )
    }

    Scene(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        cameraManipulator = cameraManipulator,
        childNodes = modelNodes
    )
}









