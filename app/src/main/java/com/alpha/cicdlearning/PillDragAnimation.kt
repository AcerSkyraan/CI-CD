package com.alpha.cicdlearning

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.compose.ui.unit.times
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun PillDragAnimation1(modifier: Modifier) {

    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }

    val animatedWidth by animateDpAsState(
        targetValue = if (isDragging) 360.dp else 180.dp,
        label = ""
    )

    val animatedHeight by animateDpAsState(
        targetValue = if (isDragging) 600.dp else 56.dp,
        label = ""
    )

    val animatedCorner by animateDpAsState(
        targetValue = if (isDragging) 0.dp else 36.dp,
        label = ""
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ){
        Box(
            modifier = Modifier
                .offset { IntOffset(0, dragOffset.roundToInt()) }
                .padding(top = 24.dp)
                .align(Alignment.TopCenter)
                .width(animatedWidth)
                .height(animatedHeight)
                .clip(RoundedCornerShape(animatedCorner))
                .background(Color.White)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            dragOffset = 0f
                        },
                        onDragCancel = {
                            isDragging = false
                            dragOffset = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount.y
                        }
                    )
                }
        )
    }
}


@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun PillDragAnimation2(modifier: Modifier = Modifier) {

    val scope = rememberCoroutineScope()

    val progress = remember { Animatable(0f) } // 0 pill, 1 rectangle

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current

    fun lerp(start: Dp, end: Dp, fraction: Float): Dp {
        return start + (end - start) * fraction
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {



        val width = lerp(180.dp, 360.dp, progress.value)
        val height = lerp(56.dp, screenHeight * 0.9f, progress.value)
        val corner = lerp(36.dp, 24.dp, progress.value)


        Box(
            modifier = Modifier
                .padding(top = 24.dp,start = 24.dp)
                .align(Alignment.TopStart)
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White)
        )



            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .align(Alignment.TopCenter)
                    .width(width)
                    .height(height)
                    .clip(RoundedCornerShape(corner))
                    .background(Color.White)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dragAmount ->

                                val delta = with(density) { dragAmount / 800f }
                                val newValue =
                                    (progress.value + delta).coerceIn(0f, 1f)

                                scope.launch {
                                    progress.snapTo(newValue)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    // snap to nearest state slowly
                                    if (progress.value > 0.5f) {
                                        progress.animateTo(
                                            1f,
                                            animationSpec = tween(
                                                durationMillis = 500
                                            )
                                        )
                                    } else {
                                        progress.animateTo(
                                            0f,
                                            animationSpec = tween(
                                                durationMillis = 500
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    }
            )



        Box(
            modifier = Modifier
                .padding(top = 24.dp,end = 24.dp)
                .align(Alignment.TopEnd)
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}





@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun PillDragAnimation3(modifier: Modifier = Modifier) {


    var isDarkTheme by remember { mutableStateOf(true) }
    val waveRadius = remember { Animatable(0f) }

    val config = LocalConfiguration.current
    val density = LocalDensity.current

    val maxRadius = with(density) {
        sqrt(
            (config.screenWidthDp.dp.toPx().pow(2)) +
                    (config.screenHeightDp.dp.toPx().pow(2))
        )
    }


    val scope = rememberCoroutineScope()

    val progress = remember { Animatable(0f) } // 0 pill, 1 rectangle

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    fun lerp(start: Dp, end: Dp, fraction: Float): Dp {
        return start + (end - start) * fraction
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color.Black else Color.White)
    ) {



        val width = lerp(180.dp, 360.dp, progress.value)
        val height = lerp(56.dp, screenHeight * 0.9f, progress.value)
        val corner = lerp(36.dp, 24.dp, progress.value)

        Box(
            modifier = Modifier
                .padding(top = 24.dp, start = 24.dp)
                .align(Alignment.TopStart)
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isDarkTheme) Color.White else Color.Black)
                .clickable {
                    scope.launch {
                        waveRadius.snapTo(0f)
                        waveRadius.animateTo(
                            maxRadius,
//                            animationSpec = spring(
//                                dampingRatio = 0.9f,
//                                stiffness = Spring.StiffnessLow
//                            )

                            animationSpec =
                                tween(700)
                        )
                        isDarkTheme = !isDarkTheme
                        waveRadius.snapTo(0f)
                    }
                }
        )



            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .align(Alignment.TopCenter)
                    .width(width)
                    .height(height)
                    .clip(RoundedCornerShape(corner))
                    .background(if (isDarkTheme) Color.White else Color.Black)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dragAmount ->

                                val delta = with(density) { dragAmount / 800f }
                                val newValue =
                                    (progress.value + delta).coerceIn(0f, 1f)

                                scope.launch {
                                    progress.snapTo(newValue)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    // snap to nearest state slowly
                                    if (progress.value > 0.5f) {
                                        progress.animateTo(
                                            1f,
                                            animationSpec = tween(
                                                durationMillis = 500
                                            )
                                        )
                                    } else {
                                        progress.animateTo(
                                            0f,
                                            animationSpec = tween(
                                                durationMillis = 500
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    }
            )



        Box(
            modifier = Modifier
                .padding(top = 24.dp,end = 24.dp)
                .align(Alignment.TopEnd)
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isDarkTheme) Color.White else Color.Black)
        )

        Canvas(modifier = Modifier.fillMaxSize()) {

            if (waveRadius.value > 0f) {
                drawCircle(
                    color = if (isDarkTheme) Color.White else Color.Black,
                    radius = waveRadius.value,
                    center = Offset(0f, 0f) // start from top-left circle
                )
            }
        }

    }
}


@SuppressLint("ConfigurationScreenWidthHeight", "RestrictedApi")
@Composable
fun PillDragAnimation4(modifier: Modifier = Modifier) {

    /* ---------------- THEME STATES ---------------- */

    var currentDark by remember { mutableStateOf(true) }
    var targetDark by remember { mutableStateOf(true) }

    // LOCKED COLORS DURING ANIMATION
    var startItemColor by remember { mutableStateOf(Color.White) }
    var endItemColor by remember { mutableStateOf(Color.Black) }

    var startBgColor by remember { mutableStateOf(Color.Black) }
    var endBgColor by remember { mutableStateOf(Color.White) }

    val waveRadius = remember { Animatable(0f) }

    val scope = rememberCoroutineScope()

    /* ---------------- SCREEN SIZE ---------------- */

    val config = LocalConfiguration.current
    val density = LocalDensity.current

    val maxRadius = with(density) {
        kotlin.math.sqrt(
            (config.screenWidthDp.dp.toPx().pow(2)) +
                    (config.screenHeightDp.dp.toPx().pow(2))
        )
    }

    /* ---------------- PILL PROGRESS ---------------- */

    val pillProgress = remember { Animatable(0f) }
    val screenHeight = config.screenHeightDp.dp

    /* ---------------- HELPERS ---------------- */

    fun lerpDp(start: Dp, end: Dp, fraction: Float): Dp {
        return start + (end - start) * fraction
    }

    /* ---------------- UI ---------------- */

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(startBgColor)
    ) {

        /* ----------- PILL MORPH ----------- */

        val width = lerpDp(180.dp, 360.dp, pillProgress.value)
        val height = lerpDp(56.dp, screenHeight * 0.9f, pillProgress.value)
        val corner = lerpDp(36.dp, 24.dp, pillProgress.value)

        /* ----------- LEFT CIRCLE (THEME TOGGLE) ----------- */

        Box(
            modifier = Modifier
                .padding(top = 24.dp, start = 24.dp)
                .align(Alignment.TopStart)
                .size(56.dp)
                .clip(CircleShape)
                .background(startItemColor)
                .clickable {
                    scope.launch {

                        targetDark = !currentDark

                        // LOCK COLORS FOR THIS ANIMATION
                        startItemColor = if (currentDark) Color.White else Color.Black
                        endItemColor = if (targetDark) Color.White else Color.Black

                        startBgColor = if (currentDark) Color.Black else Color.White
                        endBgColor = if (targetDark) Color.Black else Color.White

                        waveRadius.snapTo(0f)

                        waveRadius.animateTo(
                            maxRadius,
                            animationSpec = tween(
                                durationMillis = 1100,
                                easing = FastOutSlowInEasing
                            )
                        )

                        // Commit theme AFTER animation
                        currentDark = targetDark

                        // Update item colors
                        startItemColor = if (currentDark) Color.White else Color.Black
                        startBgColor = if (currentDark) Color.Black else Color.White

                        // prepare next animation
                        waveRadius.snapTo(0f)
                    }
                }
        )

        /* ----------- CENTER PILL ----------- */

        Box(
            modifier = Modifier
                .padding(top = 24.dp)
                .align(Alignment.TopCenter)
                .width(width)
                .height(height)
                .clip(RoundedCornerShape(corner))
                .background(startItemColor)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            val delta = dragAmount / 800f
                            val newValue =
                                (pillProgress.value + delta).coerceIn(0f, 1f)
                            scope.launch { pillProgress.snapTo(newValue) }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (pillProgress.value > 0.5f) {
                                    pillProgress.animateTo(1f, tween(500))
                                } else {
                                    pillProgress.animateTo(0f, tween(500))
                                }
                            }
                        }
                    )
                }
        )

        /* ----------- RIGHT CIRCLE ----------- */

        Box(
            modifier = Modifier
                .padding(top = 24.dp, end = 24.dp)
                .align(Alignment.TopEnd)
                .size(56.dp)
                .clip(CircleShape)
                .background(startItemColor)
        )

        ////


        /* ----------- ADDITIONAL ITEMS BELOW ----------- */

        // Row of small circles
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(5) { index ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(startItemColor)
                )
            }
        }

        // Large rectangle card
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 180.dp)
                .size(300.dp, 150.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(startItemColor)
        )

        // Bottom row of squares
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(startItemColor)
                )
            }
        }

        /* ----------- WAVE CANVAS (DRAWS ON TOP) ----------- */

        Canvas(modifier = Modifier.fillMaxSize()) {
            if (waveRadius.value > 1f) {
                // Draw the expanding circle with new theme colors
                drawCircle(
                    color = endBgColor,
                    radius = waveRadius.value,
                    center = Offset(56.dp.toPx(), 56.dp.toPx())
                )

                // Draw all items in new color within the wave
                val waveOriginX = 56.dp.toPx()
                val waveOriginY = 56.dp.toPx()

                // Helper to check if point is in wave
                fun isInWave(x: Float, y: Float): Boolean {
                    val distance = kotlin.math.sqrt(
                        (x - waveOriginX).pow(2) + (y - waveOriginY).pow(2)
                    )
                    return distance <= waveRadius.value
                }

                // Left circle
                if (isInWave(52.dp.toPx(), 52.dp.toPx())) {
                    drawCircle(
                        color = endItemColor,
                        radius = 28.dp.toPx(),
                        center = Offset(52.dp.toPx(), 52.dp.toPx())
                    )
                }

                // Center pill
                val centerX = size.width / 2
                if (isInWave(centerX, 52.dp.toPx())) {
                    val pillWidth = lerp(180.dp.toPx(), 360.dp.toPx(), pillProgress.value)
                    val pillHeight = lerp(56.dp.toPx(), (screenHeight * 0.9f).toPx(), pillProgress.value)
                    val pillCorner = lerp(36.dp.toPx(), 24.dp.toPx(), pillProgress.value)

                    drawRoundRect(
                        color = endItemColor,
                        topLeft = Offset(centerX - pillWidth / 2, 24.dp.toPx()),
                        size = Size(pillWidth, pillHeight),
                        cornerRadius = CornerRadius(pillCorner)
                    )
                }

                // Right circle
                val rightX = size.width - 52.dp.toPx()
                if (isInWave(rightX, 52.dp.toPx())) {
                    drawCircle(
                        color = endItemColor,
                        radius = 28.dp.toPx(),
                        center = Offset(rightX, 52.dp.toPx())
                    )
                }

                // Row of small circles
//                repeat(5) { index ->
//                    val itemX = centerX - 80.dp.toPx() + (index * 56.dp.toPx())
//                    if (isInWave(itemX, 136.dp.toPx())) {
//                        drawCircle(
//                            color = endItemColor,
//                            radius = 16.dp.toPx(),
//                            center = Offset(itemX, 136.dp.toPx())
//                        )
//                    }
//                }



                // Large rectangle card
                if (isInWave(centerX, 255.dp.toPx())) {
                    drawRoundRect(
                        color = endItemColor,
                        topLeft = Offset(centerX - 150.dp.toPx(), 180.dp.toPx()),
                        size = Size(300.dp.toPx(), 150.dp.toPx()),
                        cornerRadius = CornerRadius(16.dp.toPx())
                    )
                }

                // Bottom squares
//                val bottomY = size.height - 130.dp.toPx()
//                repeat(3) { index ->
//                    val itemX = centerX - 92.dp.toPx() + (index * 92.dp.toPx())
//                    if (isInWave(itemX, bottomY)) {
//                        drawRoundRect(
//                            color = endItemColor,
//                            topLeft = Offset(itemX - 30.dp.toPx(), bottomY - 30.dp.toPx()),
//                            size = Size(60.dp.toPx(), 60.dp.toPx()),
//                            cornerRadius = CornerRadius(12.dp.toPx())
//                        )
//                    }
//                }
            }
        }
    }
}


@Composable
fun MorphDragContainer0(
    modifier: Modifier = Modifier,

    // Sizes
    startWidth: Dp = 180.dp,
    endWidth: Dp = 360.dp,
    startHeight: Dp = 56.dp,
    endHeight: Dp,

    // Corners
    startCorner: Dp = 36.dp,
    endCorner: Dp = 24.dp,

    // Colors
    backgroundColor: Color = Color.White,
    screenBackground: Color = Color.Black,

    // Behavior
    dragSensitivity: Float = 800f,
    snapDuration: Int = 500,

    // Alignment
    alignment: Alignment = Alignment.TopCenter,
    padding: PaddingValues = PaddingValues(top = 24.dp),

    // Content inside container
    content: @Composable BoxScope.(progress: Float) -> Unit = {}
) {

    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }

    val density = LocalDensity.current

    fun lerpDp(start: Dp, end: Dp, fraction: Float): Dp {
        return start + (end - start) * fraction
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(screenBackground)
    ) {

        val width = lerpDp(startWidth, endWidth, progress.value)
        val height = lerpDp(startHeight, endHeight, progress.value)
        val corner = lerpDp(startCorner, endCorner, progress.value)

        Box(
            modifier = Modifier
                .padding(padding)
                .align(alignment)
                .width(width)
                .height(height)
                .clip(RoundedCornerShape(corner))
                .background(backgroundColor)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            val delta = with(density) {
                                dragAmount / dragSensitivity
                            }
                            val newValue =
                                (progress.value + delta).coerceIn(0f, 1f)

                            scope.launch { progress.snapTo(newValue) }
                        },
                        onDragEnd = {
                            scope.launch {
                                val target =
                                    if (progress.value > 0.5f) 1f else 0f
                                progress.animateTo(
                                    target,
                                    animationSpec = tween(snapDuration)
                                )
                            }
                        }
                    )
                }
        ) {
            content(progress.value)
        }
    }
}


@Composable
fun MorphDragContainer1(
    modifier: Modifier = Modifier,

    // Sizes
    startWidth: Dp = 180.dp,
    endWidth: Dp = 360.dp,
    startHeight: Dp = 56.dp,
    endHeight: Dp,

    // Corners
    startCorner: Dp = 36.dp,
    endCorner: Dp = 24.dp,

    // Colors
    backgroundColor: Color = Color.White,
    screenBackground: Color = Color.Black,

    // Behavior
    dragSensitivity: Float = 800f,

    // Animation
    snapAnimationSpec: AnimationSpec<Float> = tween(500),

    // Layout
    alignment: Alignment = Alignment.TopCenter,
    padding: PaddingValues = PaddingValues(top = 24.dp),

    content: @Composable BoxScope.(progress: Float) -> Unit = {}
) {

    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    val density = LocalDensity.current

    fun lerpDp(start: Dp, end: Dp, fraction: Float): Dp {
        return start + (end - start) * fraction
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(screenBackground)
    ) {

        val width = lerpDp(startWidth, endWidth, progress.value)
        val height = lerpDp(startHeight, endHeight, progress.value)
        val corner = lerpDp(startCorner, endCorner, progress.value)

        Box(
            modifier = Modifier
                .padding(padding)
                .align(alignment)
                .width(width)
                .height(height)
                .clip(RoundedCornerShape(corner))
                .background(backgroundColor)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            val delta = with(density) {
                                dragAmount / dragSensitivity
                            }
                            val newValue =
                                (progress.value + delta).coerceIn(0f, 1f)

                            scope.launch { progress.snapTo(newValue) }
                        },
                        onDragEnd = {
                            scope.launch {
                                val target =
                                    if (progress.value > 0.5f) 1f else 0f
                                progress.animateTo(
                                    target,
                                    animationSpec = snapAnimationSpec
                                )
                            }
                        }
                    )
                }
        ) {
            content(progress.value)
        }
    }
}




@Composable
fun MorphDragContainer(
    modifier: Modifier = Modifier,

    startWidth: Dp = 180.dp,
    endWidth: Dp = 360.dp,
    startHeight: Dp = 56.dp,
    endHeight: Dp,

    startCorner: Dp = 36.dp,
    endCorner: Dp = 24.dp,

    containerColor: Color = Color.White,

    content: @Composable BoxScope.(progress: Float) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }

    val decay = rememberSplineBasedDecay<Float>()
    val density = LocalDensity.current

    fun lerpDp(start: Dp, end: Dp, fraction: Float): Dp {
        return start + (end - start) * fraction
    }

    Box(modifier.fillMaxSize()) {

        val width = lerpDp(startWidth, endWidth, progress.value)
        val height = lerpDp(startHeight, endHeight, progress.value)
        val corner = lerpDp(startCorner, endCorner, progress.value)

        Box(
            modifier = modifier
                .width(width)
                .height(height)
                .clip(RoundedCornerShape(corner))
                .background(containerColor)
                .pointerInput(Unit) {

                    val velocityTracker = VelocityTracker()

                    detectVerticalDragGestures(
                        onDragStart = { velocityTracker.resetTracking() },

                        onVerticalDrag = { change, dragAmount ->
                            velocityTracker.addPosition(
                                change.uptimeMillis,
                                change.position
                            )

                            val delta = dragAmount / 900f
                            val newValue =
                                (progress.value + delta).coerceIn(0f, 1f)

                            scope.launch { progress.snapTo(newValue) }
                        },

                        onDragEnd = {
                            val velocity =
                                velocityTracker.calculateVelocity().y / 4000f

                            scope.launch {

                                progress.animateDecay(
                                    initialVelocity = velocity,
                                    animationSpec = decay
                                )

                                val target =
                                    if (progress.value > 0.5f) 1f else 0f

                                progress.animateTo(
                                    target,
                                    animationSpec = spring(
                                        dampingRatio =
                                            Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        }
                    )
                }
        ) {
            content(progress.value)
        }
    }
}



@Composable
fun WaveReveal(
    modifier: Modifier = Modifier,
    trigger: Boolean,
    origin: Offset,
    color: Color,
    duration: Int = 900,
    reverseDuration: Int = 700,
    background: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val config = LocalConfiguration.current

    val radius = remember { Animatable(0f) }

    val maxRadius = with(density) {
        sqrt(
            config.screenWidthDp.dp.toPx().pow(2) +
                    config.screenHeightDp.dp.toPx().pow(2)
        )
    }

    LaunchedEffect(trigger) {
        if (trigger) {
            radius.animateTo(maxRadius, tween(duration))
        } else {
            radius.animateTo(0f, tween(reverseDuration))
        }
    }

    Box(modifier.fillMaxSize()) {

        // BASE BACKGROUND
        background()

        // -------- WAVE COLOR LAYER --------
        Canvas(Modifier.matchParentSize()) {
            if (radius.value > 1f) {
                drawCircle(
                    color = color,
                    radius = radius.value,
                    center = origin
                )
            }
        }

        // -------- CLIPPED CONTENT --------
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer {
                    clip = true
                    shape = object : Shape {
                        override fun createOutline(
                            size: Size,
                            layoutDirection: LayoutDirection,
                            density: Density
                        ): Outline {
                            return Outline.Generic(
                                Path().apply {
                                    addOval(
                                        Rect(
                                            center = origin,
                                            radius = radius.value
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
        ) {
            content()
        }
    }
}







@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun DemoScreen() {


    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.foresttlottie))


    val density = LocalDensity.current
    val config = LocalConfiguration.current

    var openMenu by remember { mutableStateOf(false) }

    val origin = with(density) {
        Offset(44.dp.toPx(), 44.dp.toPx())
    }

    Box(
        Modifier
            .padding(top = 24.dp)
            .fillMaxSize()
    ){


        // -------- MENU BUTTON --------
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.Cyan)
                .clickable { openMenu = true }
        )

        // -------- NOTIFICATION --------
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.Green)
        )


        MorphDragContainer(
            modifier = Modifier
                .align(Alignment.TopCenter)
                //.padding(top = 24.dp)
            , containerColor = Color.Red,
            endHeight = config.screenHeightDp.dp * 0.9f
        ) { progress ->

            Text(
                text = if (progress < 0.5f) "Pill" else "Expanded",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Black
            )
        }

        WaveReveal(
            trigger = openMenu,
            origin = origin,
            color = Color.Gray,
        )
        {
            if (openMenu) {
                Box() {

                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier

                            .matchParentSize().fillMaxSize(),   // fills parent Box
                        contentScale = ContentScale.FillBounds, // VERY IMPORTANT
                    )


                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 120.dp, start = 24.dp)
                    )
                    {

                        Text("Menu Screen", color = Color.Black)

                        Spacer(Modifier.height(24.dp))

                        Text("Profile", color = Color.Black)
                        Spacer(Modifier.height(12.dp))
                        Text("Settings", color = Color.Black)

                        Spacer(Modifier.height(24.dp))

                        Text(
                            "Close",
                            modifier = Modifier
                                .clickable {
                                    openMenu = false
                                },
                            color = Color.Blue
                        )
                    }
                }
            }

        }

    }
}






