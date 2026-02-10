package com.alpha.cicdlearning.KidsColoring.Basic

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.alpha.cicdlearning.R


@Composable
fun ColoringScreen() {
    val context = LocalContext.current

    // Load small bitmap once
    val bitmap = remember {
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inSampleSize = 2 // reduce memory
        }

        BitmapFactory.decodeResource(
            context.resources,
            R.drawable.colorone,
            options
        ).copy(Bitmap.Config.ARGB_8888, true)
    }

    // force recomposition without copying bitmap
    var bitmapVersion by remember { mutableStateOf(0) }

    var selectedColor by remember { mutableStateOf(Color.Red) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // DRAW AREA
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(bitmapVersion) {
                    detectTapGestures { offset ->
                        val x = offset.x.toInt()
                        val y = offset.y.toInt()

                        if (x in 0 until bitmap.width &&
                            y in 0 until bitmap.height
                        ) {
                            floodFillSafe(
                                bitmap,
                                x,
                                y,
                                selectedColor.toArgb()
                            )
                            bitmapVersion++ // recompose
                        }
                    }
                }
        )

        // COLOR PICKER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                Color.Red,
                Color.Blue,
                Color.Green,
                Color.Yellow,
                Color.Magenta,
                Color.Cyan
            ).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color, CircleShape)
                        .clickable { selectedColor = color }
                )
            }
        }
    }
}


@Composable
fun ColorPalette(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color.Red,
        Color.Blue,
        Color.Green,
        Color.Yellow,
        Color.Magenta,
        Color.Cyan,
        Color.Black
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (color == selectedColor) 3.dp else 1.dp,
                        color = Color.DarkGray,
                        shape = CircleShape
                    )
                    .clickable {
                        onColorSelected(color)
                    }
            )
        }
    }
}



fun floodFillSafe(
    bitmap: Bitmap,
    startX: Int,
    startY: Int,
    newColor: Int
) {
    val width = bitmap.width
    val height = bitmap.height

    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    val startIndex = startY * width + startX
    val targetColor = pixels[startIndex]

    if (targetColor == newColor) return
    if (targetColor == Color.Black.toArgb()) return

    // stack size limited to avoid OOM
    val stack = IntArray(width * height / 6)
    var top = 0
    stack[top] = startIndex

    while (top >= 0) {
        val index = stack[top--]

        if (index < 0 || index >= pixels.size) continue
        if (pixels[index] != targetColor) continue

        pixels[index] = newColor

        val x = index % width
        val y = index / width

        if (top + 4 < stack.size) {
            stack[++top] = y * width + (x + 1)
            stack[++top] = y * width + (x - 1)
            stack[++top] = (y + 1) * width + x
            stack[++top] = (y - 1) * width + x
        }
    }

    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
}



