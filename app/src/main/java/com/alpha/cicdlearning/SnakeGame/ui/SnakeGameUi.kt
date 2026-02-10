package com.alpha.cicdlearning.SnakeGame.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpha.cicdlearning.SnakeGame.di.SnakeGameState
import com.alpha.cicdlearning.SnakeGame.domain.Direction
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.min

/* ---------------- GAME LOOP ---------------- */

@Composable
fun GameLoop(state: SnakeGameState) {
    LaunchedEffect(state.gameOver) {
        while (!state.gameOver) {
            delay(200)
            state.move()
        }
    }
}

/* ---------------- SWIPE CONTROLS ---------------- */

fun Modifier.swipeControls(state: SnakeGameState) = pointerInput(Unit) {
    detectDragGestures { _, dragAmount ->
        val (x, y) = dragAmount

        val newDirection = when {
            abs(x) > abs(y) ->
                if (x > 0) Direction.RIGHT else Direction.LEFT
            else ->
                if (y > 0) Direction.DOWN else Direction.UP
        }

        // 🚫 Prevent reverse direction
        val current = state.direction
        if (
            (current == Direction.RIGHT && newDirection == Direction.LEFT) ||
            (current == Direction.LEFT && newDirection == Direction.RIGHT) ||
            (current == Direction.UP && newDirection == Direction.DOWN) ||
            (current == Direction.DOWN && newDirection == Direction.UP)
        ) {
            return@detectDragGestures
        }

        state.direction = newDirection
    }
}

/* ---------------- BOARD DRAWING ---------------- */

@Composable
fun SnakeBoard(state: SnakeGameState) {

    Canvas(modifier = Modifier.fillMaxSize()) {

        val boardPixels = min(size.width, size.height)
        val cellSize = boardPixels / state.boardSize

        val offsetX = (size.width - boardPixels) / 2
        val offsetY = (size.height - boardPixels) / 2

        /* BORDER */
        drawRect(
            color = Color.White,
            topLeft = Offset(offsetX, offsetY),
            size = Size(boardPixels, boardPixels),
            style = Stroke(width = 6f)
        )

        /* GRID */
        for (i in 0..state.boardSize) {
            val pos = i * cellSize

            drawLine(
                color = Color.DarkGray,
                start = Offset(offsetX + pos, offsetY),
                end = Offset(offsetX + pos, offsetY + boardPixels),
                strokeWidth = 1f
            )

            drawLine(
                color = Color.DarkGray,
                start = Offset(offsetX, offsetY + pos),
                end = Offset(offsetX + boardPixels, offsetY + pos),
                strokeWidth = 1f
            )
        }

        /* SNAKE */
        state.snake.forEach {
            drawRect(
                color = Color.Green,
                topLeft = Offset(
                    offsetX + it.x * cellSize,
                    offsetY + it.y * cellSize
                ),
                size = Size(cellSize, cellSize)
            )
        }

        /* FOOD */
        drawRect(
            color = Color.Red,
            topLeft = Offset(
                offsetX + state.food.x * cellSize,
                offsetY + state.food.y * cellSize
            ),
            size = Size(cellSize, cellSize)
        )
    }
}

/* ---------------- GAME OVER OVERLAY ---------------- */

@Composable
fun GameOverScreen(onRestart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = "GAME OVER",
                color = Color.White,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = onRestart) {
                Text("Restart")
            }
        }
    }
}

/* ---------------- MAIN GAME SCREEN ---------------- */

@Composable
fun SnakeGame() {

    val state = remember { SnakeGameState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // or Nokia green: Color(0xFF9BBC0F)
            .swipeControls(state)
    ) {

        SnakeBoard(state)
        GameLoop(state)

        if (state.gameOver) {
            GameOverScreen {
                state.restart()
            }
        }
    }
}
