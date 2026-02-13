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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpha.cicdlearning.R
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun MagnifierContainer(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    borderColor: Color = Color.Yellow,
    magnification: Float = 1.3f,
    shape: Shape = RoundedCornerShape(50),
    content: @Composable BoxScope.() -> Unit
) {

    val scale by animateFloatAsState(
        targetValue = if (isActive) magnification else 1f,
        animationSpec = tween(250),
        label = ""
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .border(
                width = if (isActive) 2.dp else 0.dp,
                color = borderColor,
                shape = shape
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}


@Composable
fun MagnifierPlayground() {

    var selected by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Magnifier Playground", fontSize = 22.sp)

        // EMOJIS
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf("😀","🚀","🔥").forEachIndexed { i, e ->
                MagnifierContainer(
                    isActive = selected == i,
                    modifier = Modifier
                        .size(60.dp)
                        .clickable { selected = i },
                    shape = CircleShape
                ) {
                    Text(e, fontSize = 26.sp)
                }
            }
        }

        // ICONS
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf(Icons.Default.Home, Icons.Default.Favorite).forEachIndexed { i, icon ->
                val id = 10 + i
                MagnifierContainer(
                    isActive = selected == id,
                    modifier = Modifier
                        .size(60.dp)
                        .clickable { selected = id },
                    shape = CircleShape
                ) {
                    Icon(icon, null, tint = Color.Black)
                }
            }
        }

        // IMAGE
        MagnifierContainer(
            isActive = selected == 20,
            modifier = Modifier
                .size(100.dp)
                .clickable { selected = 20 },
            shape = RoundedCornerShape(16.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.colorone),
                contentDescription = null
            )
        }

        // TEXT
        MagnifierContainer(
            isActive = selected == 30,
            modifier = Modifier
                .clickable { selected = 30 }
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Tap Me", fontSize = 20.sp)
        }

        // CARD
        MagnifierContainer(
            isActive = selected == 40,
            modifier = Modifier
                .clickable { selected = 40 }
                .size(140.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Card {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Card")
                }
            }
        }
    }
}


@Composable
fun ReflectiveMagnifierContainer(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    magnification: Float = 1.35f,
    shape: Shape = RoundedCornerShape(50),
    content: @Composable BoxScope.() -> Unit
) {

    // ----- SCALE ANIMATION -----
    val scale by animateFloatAsState(
        targetValue = if (isActive) magnification else 1f,
        animationSpec = tween(250),
        label = ""
    )

    // ----- REFLECTION ANIMATION -----
    val infinite = rememberInfiniteTransition(label = "")
    val shineOffset by infinite.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .drawBehind {

                if (isActive) {

                    val stroke = 3.dp.toPx()

                    // Animated reflective border
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White,
                                Color.Transparent
                            ),
                            start = Offset(shineOffset, 0f),
                            end = Offset(shineOffset + 200f, size.height)
                        ),
                        style = Stroke(width = stroke),
                        cornerRadius = CornerRadius(50f)
                    )
                }
            },
        contentAlignment = Alignment.Center,
        content = content
    )
}


@Composable
fun ReflectivePlayground() {

    val emojis = listOf("😀","🚀","🔥","🎵","❤️")
    val itemSize = 70.dp

    var selected by remember { mutableStateOf(2) }

    val animatedIndex by animateFloatAsState(
        targetValue = selected.toFloat(),
        animationSpec = tween(350),
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2)),
        contentAlignment = Alignment.Center
    ) {

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {

            emojis.forEachIndexed { index, emoji ->

                val distance = abs(animatedIndex - index)
                val scaleBoost = 1f + (1f - distance.coerceIn(0f,1f)) * 0.5f

                ReflectiveMagnifierContainer(
                    isActive = selected == index,
                    modifier = Modifier
                        .size(itemSize)
                        .clickable { selected = index },
                    shape = CircleShape,
                    magnification = scaleBoost
                ) {
                    Text(emoji, fontSize = 28.sp)
                }
            }
        }
    }
}


@Composable
fun BookMagnifierScreen2() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // -------- BOOK TEXT --------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            repeat(40) {
                Text(
                    text = "This is a magnifier demo line $it. Drag the lens around.",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }

        // -------- DRAGGABLE LENS --------
        DraggableMagnifierLens()
    }
}

@Composable
fun BookMagnifierScree2n() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            repeat(50) {
                Text(
                    text = "Optical Lens Demo Line $it",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }

        OpticalLens()
    }
}
@Composable
fun OpticalLens2() {

    var offsetX by remember { mutableStateOf(200f) }
    var offsetY by remember { mutableStateOf(400f) }

    val infinite = rememberInfiniteTransition(label = "")
    val shine by infinite.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing)
        ),
        label = ""
    )

    val totalSize = 160.dp      // radius 40 visual equivalent
    val innerSize = 120.dp      // radius 30

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(totalSize)
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()
                    offsetX += drag.x
                    offsetY += drag.y
                }
            },
        contentAlignment = Alignment.Center
    ) {

        // ----- INNER MAGNIFICATION CORE -----
        Box(
            modifier = Modifier
                .size(innerSize)
                .clip(CircleShape)
                .graphicsLayer {
                    scaleX = 1.35f
                    scaleY = 1.35f
                    alpha = 0.18f
                }
                .background(Color.White)
        )

        // ----- OUTER REFLECTIVE RING -----
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            val stroke = 6.dp.toPx()
            val radius = size.minDimension / 2

            // soft outer glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(0.25f),
                        Color.Transparent
                    )
                ),
                radius = radius
            )

            // reflective moving stroke
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White,
                        Color.Transparent
                    ),
                    start = Offset(shine, 0f),
                    end = Offset(shine + 200f, size.height)
                ),
                style = Stroke(width = stroke)
            )
        }
    }
}

@Composable
fun DraggableMagnifierLens() {

    var offsetX by remember { mutableStateOf(200f) }
    var offsetY by remember { mutableStateOf(400f) }

    // reflection animation
    val infinite = rememberInfiniteTransition(label = "")
    val shine by infinite.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = ""
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(140.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .clip(CircleShape)
            .drawBehind {

                val stroke = 4.dp.toPx()

                // inner soft glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(0.25f),
                            Color.Transparent
                        )
                    )
                )

                // animated reflective border
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White,
                            Color.Transparent
                        ),
                        start = Offset(shine, 0f),
                        end = Offset(shine + 200f, size.height)
                    ),
                    style = Stroke(stroke)
                )
            },
        contentAlignment = Alignment.Center
    ) {

        // fake magnification glass center
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.15f
                    scaleY = 1.15f
                    alpha = 0.15f
                }
                .background(Color.White)
        )
    }
}


@Composable
fun BookContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        repeat(50) {
            Text(
                "Optical Lens Demo Line $it",
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}


@Composable
fun BookMagnifierScreen() {
    Box(Modifier.fillMaxSize().background(Color.White)) {

        BookContent(Modifier.fillMaxSize())

        OpticalLens()
    }
}


@Composable
fun OpticalLens() {

    var x by remember { mutableStateOf(200f) }
    var y by remember { mutableStateOf(400f) }

    val infinite = rememberInfiniteTransition(label = "")
    val shine by infinite.animateFloat(
        -300f, 300f,
        infiniteRepeatable(tween(2200, easing = LinearEasing)),
        label = ""
    )

    val lensSize = 180.dp
    val innerSize = 110.dp // ~60%

    Box(
        modifier = Modifier
            .offset { IntOffset(x.toInt(), y.toInt()) }
            .size(lensSize)
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()
                    x += drag.x
                    y += drag.y
                }
            },
        contentAlignment = Alignment.Center
    ) {

        // ---------- INNER TRUE MAGNIFICATION ----------
        Box(
            modifier = Modifier
                .size(innerSize)
                .clip(RoundedCornerShape(60)) // slight oval feel
                .graphicsLayer {
                    scaleX = 1.4f
                    scaleY = 1.4f
                }
        ) {
            BookContent(
                Modifier
                    .matchParentSize()
                    .background(Color(0x2200AEEF)) // subtle blue tint
            )
        }

        // ---------- OUTER REFLECTIVE RING ----------
        Canvas(Modifier.fillMaxSize()) {

            val stroke = 8.dp.toPx()
            val radius = size.minDimension / 2

            // bottom-right shadow
            drawCircle(
                color = Color.Black.copy(0.18f),
                radius = radius,
                center = Offset(size.width * .55f, size.height * .55f)
            )

            // glass glow
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color.White.copy(.35f), Color.Transparent)
                ),
                radius = radius
            )

            // moving reflection
            drawCircle(
                brush = Brush.linearGradient(
                    listOf(Color.Transparent, Color.White, Color.Transparent),
                    start = Offset(shine, 0f),
                    end = Offset(shine + 200f, size.height)
                ),
                style = Stroke(stroke)
            )
        }
    }
}


