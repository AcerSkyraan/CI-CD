package com.alpha.cicdlearning



import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt

// ============= CORE WAVE REVEAL SYSTEM =============

data class WaveItemData(
    val position: Offset,
    val size: Size,
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

    val itemsList = remember { mutableListOf<WaveItemData>() }

    val waveScope = object : WaveRevealScope {
        override val itemColor = startItemColor

        override fun triggerWave() {
            scope.launch {
                targetDark = !currentDark

                startItemColor = if (currentDark) darkItemColor else lightItemColor
                endItemColor = if (targetDark) darkItemColor else lightItemColor

                startBgColor = if (currentDark) darkBackground else lightBackground
                endBgColor = if (targetDark) darkBackground else lightBackground

                waveRadius.snapTo(0f)

                waveRadius.animateTo(
                    maxRadius,
                    animationSpec = tween(animationDurationMs, easing = FastOutSlowInEasing)
                )

                currentDark = targetDark

                startItemColor = if (currentDark) darkItemColor else lightItemColor
                startBgColor = if (currentDark) darkBackground else lightBackground

                waveRadius.snapTo(0f)
                onThemeChange(currentDark)
            }
        }

        override fun registerItem(position: Offset, size: Size, draw: DrawScope.(Color) -> Unit) {
            itemsList.add(WaveItemData(position, size, draw))
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(startBgColor)) {

        itemsList.clear()

        waveScope.content()

        Canvas(modifier = Modifier.fillMaxSize()) {
            if (waveRadius.value > 1f) {
                val waveOriginPx = with(density) {
                    Offset(waveOriginDp.x.dp.toPx(), waveOriginDp.y.dp.toPx())
                }

                drawCircle(
                    color = endBgColor,
                    radius = waveRadius.value,
                    center = waveOriginPx
                )

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
    fun registerItem(position: Offset, size: Size, draw: DrawScope.(Color) -> Unit)

}

@Composable
fun WaveRevealScope.WaveItem(
    modifier: Modifier = Modifier,
    canvasDraw: DrawScope.(position: Offset, size: Size, color: Color) -> Unit,
    content: @Composable (color: Color) -> Unit
) {
    var itemPosition by remember { mutableStateOf(Offset.Zero) }
    var itemSize by remember { mutableStateOf(Size.Zero) }

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            val position = coordinates.positionInRoot()
            itemPosition = Offset(
                position.x + coordinates.size.width / 2f,
                position.y + coordinates.size.height / 2f
            )
            itemSize = Size(
                coordinates.size.width.toFloat(),
                coordinates.size.height.toFloat()
            )
        }
    ) {
        registerItem(itemPosition, itemSize) { color ->
            canvasDraw(itemPosition, itemSize, color)
        }

        content(itemColor)
    }
}

// ============= HELPER COMPOSABLES =============

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
        canvasDraw = { position, size, color ->
            drawRoundRect(
                color = color,
                topLeft = Offset(
                    position.x - size.width / 2f,
                    position.y - size.height / 2f
                ),
                size = size,
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

@Composable
fun WaveRevealScope.SimpleWaveCircle(
    modifier: Modifier = Modifier,
    size: Dp,
    content: @Composable BoxScope.(color: Color) -> Unit
) {
    WaveItem(
        modifier = modifier.size(size),
        canvasDraw = { position, itemSize, color ->
            drawCircle(
                color = color,
                radius = itemSize.width / 2f,
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
        canvasDraw = { position, size, color ->
            drawRoundRect(
                color = color,
                topLeft = Offset(
                    position.x - size.width / 2f,
                    position.y - size.height / 2f
                ),
                size = size,
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
        canvasDraw = { position, size, color ->
            drawRoundRect(
                color = color,
                topLeft = Offset(
                    position.x - size.width / 2f,
                    position.y - size.height / 2f
                ),
                size = size,
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

@Composable
fun WaveRevealScope.WaveText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign = TextAlign.Center,
    invertColor: Boolean = true,
    maxLines: Int = 1
) {
    val textMeasurer = rememberTextMeasurer()

    WaveItem(
        modifier = modifier,
        canvasDraw = { position, size, color ->
            val finalColor = if (invertColor) {
                if (color == Color.White) Color.Black else Color.White
            } else {
                color
            }

            val coloredText = textMeasurer.measure(
                text = text,
                style = TextStyle(
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    color = finalColor
                ),
                maxLines = maxLines
            )

            drawText(
                textLayoutResult = coloredText,
                topLeft = Offset(
                    position.x - coloredText.size.width / 2f,
                    position.y - coloredText.size.height / 2f
                )
            )
        }
    ) { color ->
        val finalColor = if (invertColor) {
            if (itemColor == Color.White) Color.Black else Color.White
        } else {
            itemColor
        }

        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
            color = finalColor,
            maxLines = maxLines
        )
    }
}

// ============= USAGE EXAMPLE =============

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
                fontWeight = FontWeight.Bold,
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
                    WaveText(text = "🌓", fontSize = 20.sp, invertColor = false)
                }
            }
        }

        // Center Column
        SimpleWaveColumn(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .padding(top = 100.dp),
            width = 200.dp,
            height = 150.dp,
            cornerRadius = 16.dp,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) { color ->
            WaveText(
                text = "Content",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )


            WaveText(
                text = "Content",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Bottom Navigation
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
