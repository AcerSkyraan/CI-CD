package com.alpha.cicdlearning

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun WaveRevealOriginal(
    visible: Boolean,
    modifier: Modifier = Modifier,
    origin: Offset? = null,
    duration: Int = 800,
    reverseDuration: Int = 600,
    background: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    val radiusAnim = remember { Animatable(0f) }

    // Max radius = diagonal of the screen
    val maxRadius = remember(config) {
        with(density) {
            sqrt(
                config.screenWidthDp.dp.toPx().pow(2) +
                        config.screenHeightDp.dp.toPx().pow(2)
            )
        }
    }

    // Infinite ripple transition
    val rippleTransition = rememberInfiniteTransition()
    val rippleOffset by rippleTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        )
    )

    // Animate radius when visible changes
    LaunchedEffect(visible) {
        if (visible) {
            radiusAnim.animateTo(maxRadius, tween(duration))
        } else {
            radiusAnim.animateTo(0f, tween(reverseDuration))
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Base background
        background()

        // Wave layer
        Canvas(Modifier.matchParentSize()) {
            val centerPoint = origin ?: center
            if (radiusAnim.value > 1f) {
                drawWaveCircle(centerPoint, radiusAnim.value, rippleOffset, isDark)
            }
        }

        // Clipped content
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
                            val centerPoint = origin ?: Offset(size.width / 2f, size.height / 2f)
                            val path = Path().apply {
                                // Add slight ripple by modulating radius
                                val dynamicRadius =
                                    radiusAnim.value + sin(rippleOffset * PI / 180).toFloat() * 20f
                                addOval(Rect(center = centerPoint, radius = dynamicRadius))
                            }
                            return Outline.Generic(path)
                        }
                    }
                }
        ) {
            content()
        }
    }
}

// Helper extension for Canvas to draw wavy gradient circle
private fun DrawScope.drawWaveCircle(center: Offset, radius: Float, offset: Float, isDark: Boolean) {
    val waveBrush = Brush.radialGradient(
        colors = if (isDark)
            listOf(Color(0xAA0D47A1), Color(0x220D47A1)) // dark mode blue
        else
            listOf(Color(0xAA00BFFF), Color(0x2200BFFF)), // light mode blue
        center = center,
        radius = radius
    )

    val dynamicRadius = radius + sin(offset * PI / 180).toFloat() * 20f

    drawCircle(
        brush = waveBrush,
        radius = dynamicRadius,
        center = center,
        alpha = 0.7f
    )
}


@Composable
fun LiquidWaveReveal(
    visible: Boolean,
    modifier: Modifier = Modifier,
    origin: Offset? = null,
    duration: Int = 800,
    reverseDuration: Int = 600,
    layers: Int = 3, // number of overlapping waves
    background: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    val radiusAnim = remember { Animatable(0f) }

    val maxRadius = remember(config) {
        with(density) {
            sqrt(
                config.screenWidthDp.dp.toPx().pow(2) +
                        config.screenHeightDp.dp.toPx().pow(2)
            )
        }
    }

    // Animate radius
    LaunchedEffect(visible) {
        if (visible) {
            radiusAnim.animateTo(maxRadius, tween(duration))
        } else {
            radiusAnim.animateTo(0f, tween(reverseDuration))
        }
    }

    // Infinite wave animation for ripple effect
    val infiniteTransition = rememberInfiniteTransition()
    val phaseShifts = List(layers) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000 + index * 400, easing = LinearEasing)
            )
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Base background
        background()

        // Wave layer
        Canvas(Modifier.matchParentSize()) {
            val centerPoint = origin ?: center

            for (i in 0 until layers) {
                val alpha = 0.4f / (i + 1)

                // Ensure radius > 0 to avoid RadialGradient crash
                val safeRadius = radiusAnim.value.coerceAtLeast(1f)

                val waveBrush = Brush.radialGradient(
                    colors = if (isDark)
                        listOf(Color(0xAA0D47A1), Color(0x220D47A1))
                    else
                        listOf(Color(0xAA00BFFF), Color(0x2200BFFF)),
                    center = centerPoint,
                    radius = safeRadius
                )

                // Dynamic wave radius for liquid motion
                val waveRadius = safeRadius + sin((phaseShifts[i].value + i * 45) * PI / 180).toFloat() * 30f

                drawCircle(
                    brush = waveBrush,
                    radius = waveRadius,
                    center = centerPoint,
                    alpha = alpha
                )
            }
        }

        // Clipped content inside the wave
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
                            val centerPoint = origin ?: Offset(size.width / 2f, size.height / 2f)
                            val path = Path()

                            // Largest wave determines clipping
                            val layersOffset = List(layers) { i ->
                                sin((phaseShifts[i].value + i * 45) * PI / 180).toFloat() * 30f
                            }
                            val maxWave = radiusAnim.value.coerceAtLeast(1f) + (layersOffset.maxOrNull() ?: 0f)
                            path.addOval(Rect(center = centerPoint, radius = maxWave))

                            return Outline.Generic(path)
                        }
                    }
                }
        ) {
            content()
        }
    }
}




@Composable
fun DemoWaveScreen() {
    var openMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        WaveRevealOriginal (
            visible = openMenu,
            origin = Offset(100f, 100f),
        ) {
            Column(modifier = Modifier.padding(32.dp)) {
                Text("Hello Ocean Wave!", color = Color.White)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Tap anywhere to close",
                    color = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color.Cyan)
                .align(Alignment.BottomCenter)
                .clickable { openMenu = !openMenu }
        )
    }
}


@Composable
fun LiquidWaveDemo() {
    var openMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        LiquidWaveReveal(
            visible = openMenu,
            origin = Offset(200f, 400f),
        ) {
            Column(modifier = Modifier.padding(32.dp)) {
                Text("Liquid Ocean Wave!", color = Color.White)
                Spacer(Modifier.height(16.dp))
                Text("Tap the button below to toggle", color = Color.White)
            }
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color.Cyan)
                .align(Alignment.BottomCenter)
                .clickable { openMenu = !openMenu }
        )
    }
}

