package com.alpha.cicdlearning



import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.compose.ui.unit.times
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt



import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.sqrt
import androidx.compose.ui.unit.TextUnit

// Item registration data
data class WaveItemData(
    val position: Offset,
    val draw: DrawScope.(color: Color) -> Unit
)

@Composable
fun WaveRevealLayout(
    isDark: Boolean,
    onThemeChange: (Boolean) -> Unit = {},
    darkBackground: Color = Color.Black,
    lightBackground: Color = Color.White,
    darkItemColor: Color = Color.White,
    lightItemColor: Color = Color.Black,
    waveOriginDp: Offset = Offset(56f, 56f),
    animationDurationMs: Int = 1100,
    content: @Composable WaveRevealScope.() -> Unit
) {
    var currentDark by remember { mutableStateOf(isDark) }
    var targetDark by remember { mutableStateOf(isDark) }

    // LOCKED COLORS - only update after animation completes
    var startItemColor by remember { mutableStateOf(if (isDark) darkItemColor else lightItemColor) }
    var endItemColor by remember { mutableStateOf(if (isDark) darkItemColor else lightItemColor) }

    var startBgColor by remember { mutableStateOf(if (isDark) darkBackground else lightBackground) }
    var endBgColor by remember { mutableStateOf(if (isDark) darkBackground else lightBackground) }

    val waveRadius = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val config = LocalConfiguration.current
    val density = LocalDensity.current

    val maxRadius = with(density) {
        sqrt(
            config.screenWidthDp.dp.toPx().pow(2) +
                    config.screenHeightDp.dp.toPx().pow(2)
        )
    }

    // Use regular mutable list - NOT state list
    val itemsList = remember { mutableListOf<WaveItemData>() }

    val waveScope = object : WaveRevealScope {
        override val itemColor = startItemColor

        override fun triggerWave() {
            scope.launch {
                targetDark = !currentDark

                // LOCK COLORS FOR THIS ANIMATION
                startItemColor = if (currentDark) darkItemColor else lightItemColor
                endItemColor = if (targetDark) darkItemColor else lightItemColor

                startBgColor = if (currentDark) darkBackground else lightBackground
                endBgColor = if (targetDark) darkBackground else lightBackground

                waveRadius.snapTo(0f)

                waveRadius.animateTo(
                    maxRadius,
                    animationSpec = tween(animationDurationMs, easing = FastOutSlowInEasing)
                )

                // Commit theme AFTER animation
                currentDark = targetDark

                // Update colors for next animation
                startItemColor = if (currentDark) darkItemColor else lightItemColor
                startBgColor = if (currentDark) darkBackground else lightBackground

                waveRadius.snapTo(0f)
                onThemeChange(currentDark)
            }
        }

        override fun registerItem(position: Offset, draw: DrawScope.(Color) -> Unit) {
            itemsList.add(WaveItemData(position, draw))
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(startBgColor)) {

        // Clear list at start of composition
        itemsList.clear()

        // Build content - registers items
        waveScope.content()

        // Wave canvas - redraws on every waveRadius change
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (waveRadius.value > 1f) {
                val waveOriginPx = with(density) {
                    Offset(waveOriginDp.x.dp.toPx(), waveOriginDp.y.dp.toPx())
                }

                // Draw background wave
                drawCircle(
                    color = endBgColor,
                    radius = waveRadius.value,
                    center = waveOriginPx
                )

                // Draw all items within wave radius
                itemsList.forEach { item ->
                    val distance = sqrt(
                        (item.position.x - waveOriginPx.x).pow(2) +
                                (item.position.y - waveOriginPx.y).pow(2)
                    )

                    if (distance <= waveRadius.value) {
                        item.draw(this, endItemColor)
                    }
                }
            }
        }
    }
}

interface WaveRevealScope {
    val itemColor: Color
    fun triggerWave()
    fun registerItem(position: Offset, draw: DrawScope.(Color) -> Unit)
}

// Base WaveItem composable
@Composable
fun WaveRevealScope.WaveItem(
    modifier: Modifier = Modifier,
    canvasDraw: DrawScope.(position: Offset, color: Color) -> Unit,
    content: @Composable (color: Color) -> Unit
) {
    var itemPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            val position = coordinates.positionInRoot()
            itemPosition = Offset(
                position.x + coordinates.size.width / 2f,
                position.y + coordinates.size.height / 2f
            )
        }
    ) {
        // Register item with its position
        registerItem(itemPosition) { color ->
            canvasDraw(itemPosition, color)
        }

        // Render composable with start color
        content(itemColor)
    }
}

// ============= HELPER COMPOSABLES =============

// Helper for simple boxes
@Composable
fun WaveRevealScope.SimpleWaveBox(
    modifier: Modifier = Modifier,
    width: Dp,
    height: Dp,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.(color: Color) -> Unit
) {
    WaveItem(
        modifier = modifier.size(width, height),
        canvasDraw = { position, color ->
            drawRoundRect(
                color = color,
                topLeft = Offset(
                    position.x - (width / 2).toPx(),
                    position.y - (height / 2).toPx()
                ),
                size = Size(width.toPx(), height.toPx()),
                cornerRadius = CornerRadius(cornerRadius.toPx())
            )
        }
    ) { color ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius))
                .background(color),
            content = { content(color) }
        )
    }
}


// Helper for circles
@Composable
fun WaveRevealScope.SimpleWaveCircle(
    modifier: Modifier = Modifier,
    size: Dp,
    content: @Composable BoxScope.(color: Color) -> Unit
) {
    WaveItem(
        modifier = modifier.size(size),
        canvasDraw = { position, color ->
            drawCircle(
                color = color,
                radius = (size / 2).toPx(),
                center = position
            )
        }
    ) { color ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(color),
            content = { content(color) }
        )
    }
}

// Helper for text
// Refined WaveText with proper positioning
@Composable
fun WaveRevealScope.WaveText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    textAlign: androidx.compose.ui.text.style.TextAlign = TextAlign.Unspecified,
    invertColor: Boolean = true,
    maxLines: Int = Int.MAX_VALUE
) {
    val textMeasurer = rememberTextMeasurer()
    var textSize by remember { mutableStateOf(Size.Zero) }

    WaveItem(
        modifier = modifier
            .wrapContentSize()
            .onGloballyPositioned { coordinates ->
                textSize = Size(
                    coordinates.size.width.toFloat(),
                    coordinates.size.height.toFloat()
                )
            },
        canvasDraw = { position, color ->
            if (textSize != Size.Zero) {
                val finalColor = if (invertColor) {
                    if (color == Color.White) Color.Black else Color.White
                } else {
                    color
                }
                val textResult = textMeasurer.measure(
                    text = text,
                    style = TextStyle(
                        fontSize = fontSize,
                        color = finalColor,
                        textAlign = textAlign
                    ),
                    maxLines = maxLines
                )
                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(
                        position.x - textResult.size.width / 2,
                        position.y - textResult.size.height / 2
                    )
                )
            }
        }
    ) { color ->
        val finalColor = if (invertColor) {
            if (color == Color.White) Color.Black else Color.White
        } else {
            color
        }
        Text(
            text = text,
            fontSize = fontSize,
            textAlign = textAlign,
            color = finalColor,
            maxLines = maxLines
        )
    }
}


// Enhanced WaveText that works perfectly
@Composable
fun WaveRevealScope.WaveText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    fontWeight: androidx.compose.ui.text.font.FontWeight? = null,
    textAlign: androidx.compose.ui.text.style.TextAlign = TextAlign.Unspecified,
    invertColor: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    overflow: androidx.compose.ui.text.style.TextOverflow = androidx.compose.ui.text.style.TextOverflow.Clip
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // Measure text once to get stable size
    val measuredText = remember(text, fontSize, fontWeight, maxLines) {
        textMeasurer.measure(
            text = text,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = fontWeight,
                textAlign = textAlign
            ),
            maxLines = maxLines,
            overflow = overflow
        )
    }

    val textWidth = with(density) { measuredText.size.width.toDp() }
    val textHeight = with(density) { measuredText.size.height.toDp() }

    WaveItem(
        modifier = modifier
            .width(textWidth)
            .height(textHeight),
        canvasDraw = { position, color ->
            val finalColor = if (invertColor) {
                if (color == Color.White) Color.Black else Color.White
            } else {
                color
            }

            val canvasText = textMeasurer.measure(
                text = text,
                style = TextStyle(
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    color = finalColor,
                    textAlign = textAlign
                ),
                maxLines = maxLines,
                overflow = overflow
            )

            drawText(
                textLayoutResult = canvasText,
                topLeft = Offset(
                    position.x - canvasText.size.width / 2f,
                    position.y - canvasText.size.height / 2f
                )
            )
        }
    ) { color ->
        val finalColor = if (invertColor) {
            if (color == Color.White) Color.Black else Color.White
        } else {
            color
        }

        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
            color = finalColor,
            maxLines = maxLines,
            overflow = overflow,
            modifier = Modifier
                .width(textWidth)
                .height(textHeight)
        )
    }
}

// Remove SimpleWaveText - just use WaveText


// Updated MixedLayoutExample
@Composable
fun MixedLayoutExample() {
    var isDark by remember { mutableStateOf(true) }

    WaveRevealLayout(
        isDark = isDark,
        onThemeChange = { isDark = it }
    ) {

        // Header Row
        SimpleWaveRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            width = 360.dp,
            height = 60.dp,
            cornerRadius = 16.dp,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) { color ->
            WaveText(
                text = "My App",
                fontSize = 20.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )

            SimpleWaveCircle(
                modifier = Modifier.padding(end = 8.dp),
                size = 40.dp
            ) { innerColor ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { triggerWave() },
                    contentAlignment = Alignment.Center
                ) {
                    WaveText(
                        text = "🌓",
                        fontSize = 20.sp,
                        invertColor = false
                    )
                }
            }
        }

        // Content Column
        SimpleWaveColumn(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .padding(top = 100.dp),
            width = 320.dp,
            height = 400.dp,
            cornerRadius = 24.dp,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) { color ->
            Spacer(modifier = Modifier.height(20.dp))

            WaveText(
                text = "Welcome!",
                fontSize = 32.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            WaveText(
                text = "This is a demo of wave reveal animation with Column layout.",
                fontSize = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
                maxLines = 3
            )

            SimpleWaveBox(
                width = 200.dp,
                height = 50.dp,
                cornerRadius = 25.dp
            ) { btnColor ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { /* action */ },
                    contentAlignment = Alignment.Center
                ) {
                    WaveText(
                        text = "Click Me",
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                }
            }
        }

        // Bottom Navigation Row
        SimpleWaveRow(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            width = 360.dp,
            height = 70.dp,
            cornerRadius = 35.dp,
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) { color ->
            WaveText(text = "🏠", fontSize = 24.sp, invertColor = false)
            WaveText(text = "🔍", fontSize = 24.sp, invertColor = false)
            WaveText(text = "➕", fontSize = 24.sp, invertColor = false)
            WaveText(text = "❤️", fontSize = 24.sp, invertColor = false)
            WaveText(text = "👤", fontSize = 24.sp, invertColor = false)
        }
    }
}


// Helper for Column
@Composable
fun WaveRevealScope.SimpleWaveColumn(
    modifier: Modifier = Modifier,
    width: Dp,
    height: Dp,
    cornerRadius: Dp = 16.dp,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.(color: Color) -> Unit
) {
    WaveItem(
        modifier = modifier.size(width, height),
        canvasDraw = { position, color ->
            drawRoundRect(
                color = color,
                topLeft = Offset(
                    position.x - (width / 2).toPx(),
                    position.y - (height / 2).toPx()
                ),
                size = Size(width.toPx(), height.toPx()),
                cornerRadius = CornerRadius(cornerRadius.toPx())
            )
        }
    ) { color ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius))
                .background(color),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = { content(color) }
        )
    }
}

// Helper for Row
@Composable
fun WaveRevealScope.SimpleWaveRow(
    modifier: Modifier = Modifier,
    width: Dp,
    height: Dp,
    cornerRadius: Dp = 16.dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.(color: Color) -> Unit
) {
    WaveItem(
        modifier = modifier.size(width, height),
        canvasDraw = { position, color ->
            drawRoundRect(
                color = color,
                topLeft = Offset(
                    position.x - (width / 2).toPx(),
                    position.y - (height / 2).toPx()
                ),
                size = Size(width.toPx(), height.toPx()),
                cornerRadius = CornerRadius(cornerRadius.toPx())
            )
        }
    ) { color ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius))
                .background(color),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            content = { content(color) }
        )
    }
}

// ============= USAGE EXAMPLES =============




/// * --------------------------------------------------------------------------------------------- * ///

// Generic Wave Reveal Animation Component
/*
@Composable
fun WaveRevealTheme(
    isDark: Boolean,
    onThemeToggle: () -> Unit,
    darkColors: ThemeColors = ThemeColors(
        background = Color.Black,
        item = Color.White
    ),
    lightColors: ThemeColors = ThemeColors(
        background = Color.White,
        item = Color.Black
    ),
    waveOrigin: Offset = Offset(56f, 56f), // in dp
    animationDuration: Int = 1100,
    content: @Composable WaveRevealScope.() -> Unit
) {
    var currentDark by remember { mutableStateOf(isDark) }
    var targetDark by remember { mutableStateOf(isDark) }

    var startItemColor by remember { mutableStateOf(if (isDark) darkColors.item else lightColors.item) }
    var endItemColor by remember { mutableStateOf(if (isDark) darkColors.item else lightColors.item) }

    var startBgColor by remember { mutableStateOf(if (isDark) darkColors.background else lightColors.background) }
    var endBgColor by remember { mutableStateOf(if (isDark) darkColors.background else lightColors.background) }

    val waveRadius = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val config = LocalConfiguration.current
    val density = LocalDensity.current

    val maxRadius = with(density) {
        kotlin.math.sqrt(
            (config.screenWidthDp.dp.toPx().pow(2)) +
                    (config.screenHeightDp.dp.toPx().pow(2))
        )
    }

    val waveRevealScope = remember(waveRadius.value, startItemColor, endItemColor) {
        WaveRevealScopeImpl(
            waveRadius = waveRadius.value,
            waveOrigin = waveOrigin,
            startItemColor = startItemColor,
            endItemColor = endItemColor,
            density = density,
            onToggle = {
                scope.launch {
                    targetDark = !currentDark

                    startItemColor = if (currentDark) darkColors.item else lightColors.item
                    endItemColor = if (targetDark) darkColors.item else lightColors.item

                    startBgColor = if (currentDark) darkColors.background else lightColors.background
                    endBgColor = if (targetDark) darkColors.background else lightColors.background

                    waveRadius.snapTo(0f)

                    waveRadius.animateTo(
                        maxRadius,
                        animationSpec = tween(
                            durationMillis = animationDuration,
                            easing = FastOutSlowInEasing
                        )
                    )

                    currentDark = targetDark
                    startItemColor = if (currentDark) darkColors.item else lightColors.item
                    startBgColor = if (currentDark) darkColors.background else lightColors.background

                    waveRadius.snapTo(0f)

                    onThemeToggle()
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(startBgColor)
    ) {
        // Render content with current theme
        waveRevealScope.content()

        // Wave overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (waveRadius.value > 1f) {
                with(density) {
                    drawCircle(
                        color = endBgColor,
                        radius = waveRadius.value,
                        center = Offset(waveOrigin.x.dp.toPx(), waveOrigin.y.dp.toPx())
                    )
                }

                // Let scope draw revealed items
                waveRevealScope.drawRevealedItems(this)
            }
        }
    }
}

// Theme colors data class
data class ThemeColors(
    val background: Color,
    val item: Color
)

// Scope for building UI with wave reveal
interface WaveRevealScope {
    val itemColor: Color
    fun onToggleClick(onClick: () -> Unit)

    @Composable
    fun RevealItem(
        position: Offset, // in dp
        content: @Composable (Color) -> Unit
    )

    fun DrawScope.drawRevealedItem(
        position: Offset, // in dp
        draw: DrawScope.(Color) -> Unit
    )
}

private class WaveRevealScopeImpl(
    private val waveRadius: Float,
    private val waveOrigin: Offset,
    private val startItemColor: Color,
    private val endItemColor: Color,
    private val density: Density,
    private val onToggle: () -> Unit
) : WaveRevealScope {

    private val revealedItems = mutableListOf<Pair<Offset, DrawScope.(Color) -> Unit>>()

    override val itemColor: Color get() = startItemColor

    override fun onToggleClick(onClick: () -> Unit) {
        onClick()
        onToggle()
    }

    @Composable
    override fun RevealItem(
        position: Offset,
        content: @Composable (Color) -> Unit
    ) {
        content(startItemColor)
    }

    override fun DrawScope.drawRevealedItem(
        position: Offset,
        draw: DrawScope.(Color) -> Unit
    ) {
        revealedItems.add(position to draw)
    }

    fun DrawScope.drawRevealedItems(drawScope: DrawScope) {
        with(density) {
            val waveOriginPx = Offset(waveOrigin.x.dp.toPx(), waveOrigin.y.dp.toPx())

            revealedItems.forEach { (position, draw) ->
                val positionPx = Offset(position.x.dp.toPx(), position.y.dp.toPx())
                val distance = kotlin.math.sqrt(
                    (positionPx.x - waveOriginPx.x).pow(2) +
                            (positionPx.y - waveOriginPx.y).pow(2)
                )

                if (distance <= waveRadius) {
                    drawScope.draw(endItemColor)
                }
            }
        }

        revealedItems.clear()
    }
}

// Usage Example
@SuppressLint("RestrictedApi")
@Composable
fun PillDragAnimation(modifier: Modifier = Modifier) {
    var isDark by remember { mutableStateOf(true) }
    val pillProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp

    fun lerpDp(start: Dp, end: Dp, fraction: Float): Dp {
        return start + (end - start) * fraction
    }

    WaveRevealTheme(
        isDark = isDark,
        onThemeToggle = { isDark = !isDark }
    ) {
        val width = lerpDp(180.dp, 360.dp, pillProgress.value)
        val height = lerpDp(56.dp, screenHeight * 0.9f, pillProgress.value)
        val corner = lerpDp(36.dp, 24.dp, pillProgress.value)

        // Left circle (toggle)
        RevealItem(position = Offset(52f, 52f)) { color ->
            Box(
                modifier = Modifier
                    .padding(top = 24.dp, start = 24.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onToggleClick {} }
            )
        }

         drawRevealedItem(position = Offset(52f, 52f)) { color ->
            drawCircle(
                color = color,
                radius = 28.dp.toPx(),
                center = Offset(52.dp.toPx(), 52.dp.toPx())
            )
        }

        // Center pill
        RevealItem(position = Offset(config.screenWidthDp / 2f, 52f)) { color ->
            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .width(width)
                    .height(height)
                    .clip(RoundedCornerShape(corner))
                    .background(color)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dragAmount ->
                                val delta = dragAmount / 800f
                                val newValue = (pillProgress.value + delta).coerceIn(0f, 1f)
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
        }

        drawRevealedItem(position = Offset(config.screenWidthDp / 2f, 52f)) { color ->
            val pillWidth = lerp(180.dp.toPx(), 360.dp.toPx(), pillProgress.value)
            val pillHeight = lerp(56.dp.toPx(), (screenHeight * 0.9f).toPx(), pillProgress.value)
            val pillCorner = lerp(36.dp.toPx(), 24.dp.toPx(), pillProgress.value)

            drawRoundRect(
                color = color,
                topLeft = Offset(center.x - pillWidth / 2, 24.dp.toPx()),
                size = Size(pillWidth, pillHeight),
                cornerRadius = CornerRadius(pillCorner)
            )
        }

        // Right circle
        val rightX = config.screenWidthDp - 52f

        RevealItem(position = Offset(rightX, 52f)) { color ->
            Box(
                modifier = Modifier
                    .padding(top = 24.dp, end = 24.dp)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }

        drawRevealedItem(position = Offset(rightX, 52f)) { color ->
            drawCircle(
                color = color,
                radius = 28.dp.toPx(),
                center = Offset(size.width - 52.dp.toPx(), 52.dp.toPx())
            )
        }

        // Additional items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .padding(top = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(5) { index ->
                RevealItem(position = Offset(config.screenWidthDp / 2f - 80f + index * 40f, 136f)) { color ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }

        // Large card
        RevealItem(position = Offset(config.screenWidthDp / 2f, 255f)) { color ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .padding(top = 180.dp)
                    .size(300.dp, 150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
            )
        }

        drawRevealedItem(position = Offset(config.screenWidthDp / 2f, 255f)) { color ->
            drawRoundRect(
                color = color,
                topLeft = Offset(center.x - 150.dp.toPx(), 180.dp.toPx()),
                size = Size(300.dp.toPx(), 150.dp.toPx()),
                cornerRadius = CornerRadius(16.dp.toPx())
            )
        }
    }
}*/


/*

// Simple generic wave reveal component
@Composable
fun WaveRevealLayout(
    isDark: Boolean,
    onThemeChange: (Boolean) -> Unit = {},
    darkBackground: Color = Color.Black,
    lightBackground: Color = Color.White,
    darkItemColor: Color = Color.White,
    lightItemColor: Color = Color.Black,
    waveOriginDp: Offset = Offset(56f, 56f),
    animationDurationMs: Int = 1100,
    content: @Composable WaveRevealScope.() -> Unit
) {
    var currentDark by remember { mutableStateOf(isDark) }

    val startBg by remember(currentDark) {
        mutableStateOf(if (currentDark) darkBackground else lightBackground)
    }
    val endBg = if (!currentDark) darkBackground else lightBackground

    val startItem by remember(currentDark) {
        mutableStateOf(if (currentDark) darkItemColor else lightItemColor)
    }
    val endItem = if (!currentDark) darkItemColor else lightItemColor

    val waveRadius = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val config = LocalConfiguration.current
    val density = LocalDensity.current

    val maxRadius = with(density) {
        sqrt(
            config.screenWidthDp.dp.toPx().pow(2) +
                    config.screenHeightDp.dp.toPx().pow(2)
        )
    }

    // Items to be revealed
    val revealItems = remember { mutableStateListOf<WaveRevealItem>() }

    val waveScope = remember(startItem, waveRadius.value) {
        object : WaveRevealScope {
            override val currentColor = startItem

            override fun triggerWave() {
                scope.launch {
                    currentDark = !currentDark
                    waveRadius.snapTo(0f)

                    waveRadius.animateTo(
                        maxRadius,
                        animationSpec = tween(animationDurationMs, easing = FastOutSlowInEasing)
                    )

                    waveRadius.snapTo(0f)
                    onThemeChange(currentDark)
                }
            }

            override fun addRevealItem(
                centerDp: Offset,
                onDraw: DrawScope.(color: Color) -> Unit
            ) {
                revealItems.add(WaveRevealItem(centerDp, onDraw))
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(startBg)) {

        // Clear and rebuild items list
        SideEffect {
            revealItems.clear()
        }

        // Content
        waveScope.content()

        // Wave canvas
        // Wave canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (waveRadius.value > 1f) {
                with(density) {
                    val waveOriginPx = Offset(
                        waveOriginDp.x.dp.toPx(),
                        waveOriginDp.y.dp.toPx()
                    )

                    // Draw background circle
                    drawCircle(
                        color = endBg,
                        radius = waveRadius.value,
                        center = waveOriginPx
                    )

                    // Draw revealed items
                    revealItems.forEach { item ->
                        val itemPx = Offset(
                            item.centerDp.x.dp.toPx(),
                            item.centerDp.y.dp.toPx()
                        )

                        val distance = sqrt(
                            (itemPx.x - waveOriginPx.x).pow(2) +
                                    (itemPx.y - waveOriginPx.y).pow(2)
                        )

                        if (distance <= waveRadius.value) {
                            // Call extension function properly
                            with(item) {
                                this@Canvas.onDraw(endItem)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Scope interface
interface WaveRevealScope {
    val currentColor: Color
    fun triggerWave()
    fun addRevealItem(centerDp: Offset, onDraw: DrawScope.(color: Color) -> Unit)
}

// Item data class
private data class WaveRevealItem(
    val centerDp: Offset,
    val onDraw: DrawScope.(color: Color) -> Unit
)

// Extension function for easy item registration
@Composable
fun WaveRevealScope.RevealableItem(
    centerDp: Offset,
    modifier: Modifier = Modifier,
    canvasDrawing: DrawScope.(color: Color) -> Unit,
    content: @Composable (color: Color) -> Unit
) {
    addRevealItem(centerDp, canvasDrawing)
    content(currentColor)
}

// Usage Example
@SuppressLint("RestrictedApi")
@Composable
fun PillDragAnimation(modifier: Modifier = Modifier) {
    var isDark by remember { mutableStateOf(true) }
    val pillProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp

    fun lerpDp(start: Dp, end: Dp, fraction: Float) = start + (end - start) * fraction

    WaveRevealLayout(
        isDark = isDark,
        onThemeChange = { isDark = it }
    ) {
        val width = lerpDp(180.dp, 360.dp, pillProgress.value)
        val height = lerpDp(56.dp, screenHeight * 0.9f, pillProgress.value)
        val corner = lerpDp(36.dp, 24.dp, pillProgress.value)

        val centerX = config.screenWidthDp / 2f
        val rightX = config.screenWidthDp - 52f

        // Left circle (toggle button)
        RevealableItem(
            centerDp = Offset(52f, 52f),
            canvasDrawing = { color ->
                drawCircle(
                    color = color,
                    radius = 28.dp.toPx(),
                    center = Offset(52.dp.toPx(), 52.dp.toPx())
                )
            }
        ) { color ->
            Box(
                modifier = Modifier
                    .padding(top = 24.dp, start = 24.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { triggerWave() }
            )
        }

        // Center pill
        RevealableItem(
            centerDp = Offset(centerX, 52f),
            canvasDrawing = { color ->
                val pillWidth = lerp(180.dp.toPx(), 360.dp.toPx(), pillProgress.value)
                val pillHeight = lerp(56.dp.toPx(), (screenHeight * 0.9f).toPx(), pillProgress.value)
                val pillCorner = lerp(36.dp.toPx(), 24.dp.toPx(), pillProgress.value)

                drawRoundRect(
                    color = color,
                    topLeft = Offset(center.x - pillWidth / 2, 24.dp.toPx()),
                    size = Size(pillWidth, pillHeight),
                    cornerRadius = CornerRadius(pillCorner)
                )
            }
        ) { color ->
            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .width(width)
                    .height(height)
                    .clip(RoundedCornerShape(corner))
                    .background(color)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dragAmount ->
                                val delta = dragAmount / 800f
                                val newValue = (pillProgress.value + delta).coerceIn(0f, 1f)
                                scope.launch { pillProgress.snapTo(newValue) }
                            },
                            onDragEnd = {
                                scope.launch {
                                    pillProgress.animateTo(
                                        if (pillProgress.value > 0.5f) 1f else 0f,
                                        tween(500)
                                    )
                                }
                            }
                        )
                    }
            )
        }

        // Right circle
        RevealableItem(
            centerDp = Offset(rightX, 52f),
            canvasDrawing = { color ->
                drawCircle(
                    color = color,
                    radius = 28.dp.toPx(),
                    center = Offset(size.width - 52.dp.toPx(), 52.dp.toPx())
                )
            }
        ) { color ->
            Box(
                modifier = Modifier
                    .padding(top = 24.dp, end = 24.dp)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }

        // Row of small circles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .padding(top = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(5) { index ->
                val itemX = centerX - 80f + (index * 40f)
                RevealableItem(
                    centerDp = Offset(itemX, 136f),
                    canvasDrawing = { color ->
                        drawCircle(
                            color = color,
                            radius = 16.dp.toPx(),
                            center = Offset(itemX.dp.toPx(), 136.dp.toPx())
                        )
                    }
                ) { color ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }

        // Large card
        RevealableItem(
            centerDp = Offset(centerX, 255f),
            canvasDrawing = { color ->
                drawRoundRect(
                    color = color,
                    topLeft = Offset(center.x - 150.dp.toPx(), 180.dp.toPx()),
                    size = Size(300.dp.toPx(), 150.dp.toPx()),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
        ) { color ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .padding(top = 180.dp)
                    .size(300.dp, 150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
            )
        }

        // Bottom squares
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .fillMaxHeight()
                .wrapContentHeight(Alignment.Bottom)
                .padding(bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            repeat(3) { index ->
                val itemX = centerX - 80f + (index * 80f)
                val itemY = config.screenHeightDp - 130f

                RevealableItem(
                    centerDp = Offset(itemX, itemY),
                    canvasDrawing = { color ->
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(itemX.dp.toPx() - 30.dp.toPx(), itemY.dp.toPx() - 30.dp.toPx()),
                            size = Size(60.dp.toPx(), 60.dp.toPx()),
                            cornerRadius = CornerRadius(12.dp.toPx())
                        )
                    }
                ) { color ->
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}
*/


