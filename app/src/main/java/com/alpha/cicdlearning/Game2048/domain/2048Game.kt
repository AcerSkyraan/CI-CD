package com.alpha.cicdlearning.game2048

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.random.Random

/* ---------------- GAME STATE ---------------- */

class Game2048State {

    companion object {
        const val SIZE = 4
    }

    var board by mutableStateOf(Array(SIZE) { Array(SIZE) { 0 } })
    var score by mutableStateOf(0)
    var gameOver by mutableStateOf(false)

    init {
        reset()
    }

    fun reset() {
        board = Array(SIZE) { Array(SIZE) { 0 } }
        score = 0
        gameOver = false
        addRandomTile()
        addRandomTile()
    }

    private fun addRandomTile() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until SIZE) {
            for (c in 0 until SIZE) {
                if (board[r][c] == 0) emptyCells.add(r to c)
            }
        }
        if (emptyCells.isNotEmpty()) {
            val (r, c) = emptyCells.random()
            board[r][c] = if (Random.nextFloat() < 0.9f) 2 else 4
        }
    }

    /* ---------------- MOVE LOGIC ---------------- */

    fun moveLeft(): Boolean {
        var moved = false
        for (r in 0 until SIZE) {
            val row = board[r].filter { it != 0 }.toMutableList()
            var i = 0
            while (i < row.size - 1) {
                if (row[i] == row[i + 1]) {
                    row[i] *= 2
                    score += row[i]
                    row.removeAt(i + 1)
                }
                i++
            }
            while (row.size < SIZE) row.add(0)

            val newRow = row.toTypedArray()
            if (!newRow.contentEquals(board[r])) {
                board[r] = newRow
                moved = true
            }
        }
        if (moved) addRandomTile()
        checkGameOver()
        return moved
    }


    fun moveRight(): Boolean {
        rotateBoard180()
        val moved = moveLeft()
        rotateBoard180()
        return moved
    }

    fun moveUp(): Boolean {
        rotateBoardClockwise()
        rotateBoardClockwise()
        rotateBoardClockwise()
        val moved = moveLeft()
        rotateBoardClockwise()
        return moved
    }

    fun moveDown(): Boolean {
        rotateBoardClockwise()
        val moved = moveLeft()
        rotateBoardClockwise()
        rotateBoardClockwise()
        rotateBoardClockwise()
        return moved
    }

    private fun rotateBoardClockwise() {
        val newBoard = Array(SIZE) { Array(SIZE) { 0 } }
        for (r in 0 until SIZE) {
            for (c in 0 until SIZE) {
                newBoard[c][SIZE - 1 - r] = board[r][c]
            }
        }
        board = newBoard
    }

    private fun rotateBoard180() {
        rotateBoardClockwise()
        rotateBoardClockwise()
    }

    /* ---------------- GAME OVER CHECK ---------------- */

    private fun canMoveLeft(): Boolean {
        for (r in 0 until SIZE) {
            for (c in 1 until SIZE) {
                if (board[r][c] != 0 && (board[r][c - 1] == 0 || board[r][c - 1] == board[r][c])) {
                    return true
                }
            }
        }
        return false
    }

    private fun canMoveRight(): Boolean {
        for (r in 0 until SIZE) {
            for (c in 0 until SIZE - 1) {
                if (board[r][c] != 0 && (board[r][c + 1] == 0 || board[r][c + 1] == board[r][c])) {
                    return true
                }
            }
        }
        return false
    }

    private fun canMoveUp(): Boolean {
        for (c in 0 until SIZE) {
            for (r in 1 until SIZE) {
                if (board[r][c] != 0 && (board[r - 1][c] == 0 || board[r - 1][c] == board[r][c])) {
                    return true
                }
            }
        }
        return false
    }

    private fun canMoveDown(): Boolean {
        for (c in 0 until SIZE) {
            for (r in 0 until SIZE - 1) {
                if (board[r][c] != 0 && (board[r + 1][c] == 0 || board[r + 1][c] == board[r][c])) {
                    return true
                }
            }
        }
        return false
    }

    private fun checkGameOver() {
        gameOver = !(canMoveLeft() || canMoveRight() || canMoveUp() || canMoveDown())
    }
}

/* ---------------- COMPOSABLE ---------------- */

@Composable
fun Game2048(state: Game2048State = remember { Game2048State() }) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFBBADA0))
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    val (dx, dy) = dragAmount
                    if (abs(dx) > abs(dy)) {
                        if (dx > 0) state.moveRight() else state.moveLeft()
                    } else {
                        if (dy > 0) state.moveDown() else state.moveUp()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Score: ${state.score}", color = Color.White, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .background(Color(0xFFBBADA0))
                    .padding(8.dp)
            ) {
                for (r in 0 until Game2048State.SIZE) {
                    Row {
                        for (c in 0 until Game2048State.SIZE) {
                            val value = state.board[r][c]
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(4.dp)
                                    .background(getTileColor(value)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (value != 0)
                                    Text("$value", color = Color.White, fontSize = 24.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.gameOver) {
                Button(onClick = { state.reset() }) {
                    Text("Restart")
                }
            }
        }
    }
}

/* ---------------- TILE COLORS ---------------- */

fun getTileColor(value: Int): Color {
    return when (value) {
        2 -> Color(0xFFEEE4DA)
        4 -> Color(0xFFEDE0C8)
        8 -> Color(0xFFF2B179)
        16 -> Color(0xFFF59563)
        32 -> Color(0xFFF67C5F)
        64 -> Color(0xFFF65E3B)
        128 -> Color(0xFFEDCF72)
        256 -> Color(0xFFEDCC61)
        512 -> Color(0xFFEDC850)
        1024 -> Color(0xFFEDC53F)
        2048 -> Color(0xFFEDC22E)
        else -> Color(0xFFCDC1B4)
    }
}
