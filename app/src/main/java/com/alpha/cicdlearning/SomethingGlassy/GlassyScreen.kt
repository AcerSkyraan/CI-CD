package com.alpha.cicdlearning.SomethingGlassy

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs


@Composable
fun LensClickScreen() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6E6E6))
    ) {

        // -------- MAIN CONTENT --------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            repeat(30) {
                Text(
                    text = "Glass Bottom Bar Demo Line $it",
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // -------- BOTTOM BAR --------
        ClickLensBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
    }
}



@Composable
fun ClickLensBar(modifier: Modifier = Modifier) {

    val emojis = listOf("😀","🚀","🎵","❤️","🔥")
    val itemWidth = 60.dp
    val lensWidth = 72.dp

    var selectedIndex by remember { mutableStateOf(2) }

    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = ""
    )

    Box(
        modifier = modifier
            .width(itemWidth * emojis.size)
            .height(84.dp)
            .clip(RoundedCornerShape(42.dp))
            .background(Color.White.copy(0.20f))
            .drawBehind {

                val r = 42.dp.toPx()

                // TOP FROST HIGHLIGHT
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(0.45f),
                            Color.Transparent
                        )
                    ),
                    cornerRadius = CornerRadius(r)
                )

                // OUTER REFLECTIVE BORDER
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(0.9f),
                            Color.Transparent,
                            Color.White.copy(0.7f)
                        )
                    ),
                    style = Stroke(width = 2.dp.toPx()),
                    cornerRadius = CornerRadius(r)
                )
            }
    ) {

        // ---------- EMOJIS ----------
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            emojis.forEachIndexed { index, emoji ->

                val distance = abs(animatedIndex - index)
                val scale = 1f + (1f - distance.coerceIn(0f, 1f)) * 0.6f

                Box(
                    modifier = Modifier
                        .size(itemWidth)
                        .clickable { selectedIndex = index },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 24.sp,
                        modifier = Modifier.graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                    )
                }
            }
        }

        // ---------- MAGNIFYING LENS ----------
        Box(
            modifier = Modifier
                .offset(x = itemWidth * animatedIndex)
                .size(width = lensWidth, height = 58.dp)
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(50))
                .drawBehind {

                    val r = 50.dp.toPx()

                    // INNER SOFT GLOW
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(0.25f),
                                Color.Transparent
                            )
                        ),
                        cornerRadius = CornerRadius(r)
                    )

                    // REFLECTIVE BORDER
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White,
                                Color.Yellow,
                                Color.White
                            )
                        ),
                        style = Stroke(width = 3.dp.toPx()),
                        cornerRadius = CornerRadius(r)
                    )
                }
        )
    }
}


