package com.alpha.cicdlearning.MemoryLaneNaver.domain

import androidx.compose.ui.Alignment


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.min

/* ---------------- GAME STATE ---------------- */

class MazeGameState(val rows: Int = 10, val cols: Int = 10) {

    // 0 = empty, 1 = wall
    var maze = List(rows) { MutableList(cols) { 0 } }
    var playerRow by mutableStateOf(0)
    var playerCol by mutableStateOf(0)
    var goalRow = rows - 1
    var goalCol = cols - 1
    var gameOver by mutableStateOf(false)

    init {
        generateMaze()
    }

    private fun generateMaze() {
        // Simple maze: add some random walls
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if ((r != 0 || c != 0) && (r != goalRow || c != goalCol)) {
                    maze[r][c] = if (Math.random() < 0.2) 1 else 0 // 20% walls
                }
            }
        }
    }

    fun move(deltaRow: Int, deltaCol: Int) {
        if (gameOver) return

        val newRow = playerRow + deltaRow
        val newCol = playerCol + deltaCol

        if (newRow in 0 until rows && newCol in 0 until cols) {
            if (maze[newRow][newCol] == 0) { // empty space
                playerRow = newRow
                playerCol = newCol
                if (playerRow == goalRow && playerCol == goalCol) {
                    gameOver = true
                }
            }
        }
    }

    fun restart() {
        playerRow = 0
        playerCol = 0
        gameOver = false
        generateMaze()
    }
}

/* ---------------- COMPOSABLE ---------------- */

@Composable
fun MazeGameScreen(state: MazeGameState = remember { MazeGameState() }) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEEE))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    val (dx, dy) = dragAmount
                    if (abs(dx) > abs(dy)) {
                        if (dx > 0) state.move(0, 1) else state.move(0, -1)
                    } else {
                        if (dy > 0) state.move(1, 0) else state.move(-1, 0)
                    }
                }
            }
    ) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            val boardPixels = min(size.width, size.height)
            val cellSize = boardPixels / state.rows

            val offsetX = (size.width - boardPixels) / 2
            val offsetY = (size.height - boardPixels) / 2

            // Draw maze walls
            for (r in 0 until state.rows) {
                for (c in 0 until state.cols) {
                    val color = when {
                        r == state.goalRow && c == state.goalCol -> Color.Yellow
                        state.maze[r][c] == 1 -> Color.DarkGray
                        else -> Color.White
                    }

                    drawRect(
                        color = color,
                        topLeft = Offset(offsetX + c * cellSize, offsetY + r * cellSize),
                        size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                    )
                }
            }

            // Draw player
            drawCircle(
                color = Color.Red,
                radius = cellSize / 2.5f,
                center = Offset(
                    offsetX + state.playerCol * cellSize + cellSize / 2,
                    offsetY + state.playerRow * cellSize + cellSize / 2
                )
            )
        }

        // Game over overlay
        if (state.gameOver) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("You Win!", fontSize = 32.sp, color = Color.White)
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = { state.restart() }) {
                    Text("Restart")
                }
            }
        }
    }
}
