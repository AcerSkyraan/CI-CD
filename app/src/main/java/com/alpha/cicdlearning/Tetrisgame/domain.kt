package com.alpha.cicdlearning.tetris

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

/* ---------------- TETRIS STATE ---------------- */

class TetrisState {

    companion object {
        const val rows = 20
        const val cols = 10

        // Tetrimino shapes
        val I = arrayOf(arrayOf(1, 1, 1, 1))
        val O = arrayOf(arrayOf(1, 1), arrayOf(1, 1))
        val T = arrayOf(arrayOf(0, 1, 0), arrayOf(1, 1, 1))
        val L = arrayOf(arrayOf(1, 0, 0), arrayOf(1, 1, 1))
        val J = arrayOf(arrayOf(0, 0, 1), arrayOf(1, 1, 1))
        val S = arrayOf(arrayOf(0, 1, 1), arrayOf(1, 1, 0))
        val Z = arrayOf(arrayOf(1, 1, 0), arrayOf(0, 1, 1))

        // Return random Tetrimino
        fun randomPiece(): Array<Array<Int>> {
            return when (Random.nextInt(7)) {
                0 -> I
                1 -> O
                2 -> T
                3 -> L
                4 -> J
                5 -> S
                else -> Z
            }
        }
    }

    // Board
    var board by mutableStateOf(Array(rows) { Array(cols) { Color.Black } })
    var score by mutableStateOf(0)
    var gameOver by mutableStateOf(false)

    // Current piece
    var currentPiece by mutableStateOf(randomPiece())
    var currentRow by mutableStateOf(0)
    var currentCol by mutableStateOf(cols / 2 - 2)

    // Drop speed
    var dropDelay = 500L

    /* ---------------- GAME LOGIC ---------------- */

    fun restart() {
        board = Array(rows) { Array(cols) { Color.Black } }
        score = 0
        gameOver = false
        currentPiece = randomPiece()
        currentRow = 0
        currentCol = cols / 2 - 2
    }

    fun rotatePiece() {
        val piece = currentPiece
        val rotated = Array(piece[0].size) { Array(piece.size) { 0 } }
        for (r in piece.indices) {
            for (c in piece[0].indices) {
                rotated[c][piece.size - 1 - r] = piece[r][c]
            }
        }
        if (canPlace(rotated, currentRow, currentCol)) currentPiece = rotated
    }

    fun moveLeft() {
        if (canPlace(currentPiece, currentRow, currentCol - 1)) currentCol--
    }

    fun moveRight() {
        if (canPlace(currentPiece, currentRow, currentCol + 1)) currentCol++
    }

    fun moveDown(): Boolean {
        return if (canPlace(currentPiece, currentRow + 1, currentCol)) {
            currentRow++
            true
        } else {
            placePiece()
            clearLines()
            spawnPiece()
            false
        }
    }

    private fun canPlace(piece: Array<Array<Int>>, row: Int, col: Int): Boolean {
        for (r in piece.indices) {
            for (c in piece[0].indices) {
                if (piece[r][c] == 1) {
                    val newRow = row + r
                    val newCol = col + c
                    if (newRow !in 0 until rows || newCol !in 0 until cols) return false
                    if (board[newRow][newCol] != Color.Black) return false
                }
            }
        }
        return true
    }

    private fun placePiece() {
        for (r in currentPiece.indices) {
            for (c in currentPiece[0].indices) {
                if (currentPiece[r][c] == 1) {
                    board[currentRow + r][currentCol + c] = Color.Cyan
                }
            }
        }
    }

    private fun spawnPiece() {
        currentPiece = randomPiece()
        currentRow = 0
        currentCol = cols / 2 - 2
        if (!canPlace(currentPiece, currentRow, currentCol)) gameOver = true
    }

    private fun clearLines() {
        val newBoard = board.filter { row -> row.any { it == Color.Black } }.toMutableList()
        val cleared = rows - newBoard.size
        repeat(cleared) { newBoard.add(0, Array(cols) { Color.Black }) }
        board = newBoard.toTypedArray()
        score += cleared * 100
    }
}

/* ---------------- GAME LOOP ---------------- */

@Composable
fun TetrisLoop(state: TetrisState) {
    LaunchedEffect(state.gameOver) {
        while (!state.gameOver) {
            delay(state.dropDelay)
            state.moveDown()
        }
    }
}

/* ---------------- CONTROLS ---------------- */

fun Modifier.tetrisControls(state: TetrisState) = pointerInput(Unit) {
    detectTapGestures(
        onTap = { state.rotatePiece() },      // tap to rotate
        onDoubleTap = { state.moveDown() }    // optional fast drop
    )
}

/* ---------------- BOARD ---------------- */

@Composable
fun TetrisBoard(state: TetrisState) {

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cellWidth = w / TetrisState.cols
        val cellHeight = h / TetrisState.rows

        // Draw board
        for (r in 0 until TetrisState.rows) {
            for (c in 0 until TetrisState.cols) {
                drawRect(
                    color = state.board[r][c],
                    topLeft = androidx.compose.ui.geometry.Offset(c * cellWidth, r * cellHeight),
                    size = Size(cellWidth - 1f, cellHeight - 1f)
                )
            }
        }

        // Draw current piece
        for (r in state.currentPiece.indices) {
            for (c in state.currentPiece[0].indices) {
                if (state.currentPiece[r][c] == 1) {
                    val x = (state.currentCol + c) * cellWidth
                    val y = (state.currentRow + r) * cellHeight
                    drawRect(
                        color = Color.Red,
                        topLeft = androidx.compose.ui.geometry.Offset(x, y),
                        size = Size(cellWidth - 1f, cellHeight - 1f)
                    )
                }
            }
        }
    }
}

/* ---------------- MAIN SCREEN ---------------- */

@Composable
fun TetrisGame() {

    val state = remember { TetrisState().apply { restart() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .tetrisControls(state)
    ) {

        TetrisBoard(state)
        TetrisLoop(state)

        // Score
        Text(
            text = "Score: ${state.score}",
            color = Color.White,
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Game Over
        if (state.gameOver) {
            Button(
                onClick = { state.restart() },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text("Restart")
            }
        }
    }
}
