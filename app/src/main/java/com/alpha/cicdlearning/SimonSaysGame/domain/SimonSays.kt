package com.alpha.cicdlearning.simonsays

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

/* ---------------- GAME STATE ---------------- */

class SimonSaysState {

    data class ColorButton(val color: Color, val id: Int)

    val buttons = listOf(
        ColorButton(Color.Red, 0),
        ColorButton(Color.Green, 1),
        ColorButton(Color.Blue, 2),
        ColorButton(Color.Yellow, 3)
    )

    var sequence = mutableStateListOf<Int>() // sequence of button ids
    var playerIndex by mutableStateOf(0)       // where player is in sequence
    var score by mutableStateOf(0)
    var gameOver by mutableStateOf(false)
    var showingSequence by mutableStateOf(false)

    // For flashing animation
    var highlightedButton by mutableStateOf(-1)

    fun restart() {
        sequence.clear()
        playerIndex = 0
        score = 0
        gameOver = false
        highlightedButton = -1
        showingSequence = false
    }

    fun pressButton(id: Int) {
        if (gameOver || showingSequence) return

        if (id == sequence[playerIndex]) {
            playerIndex++
            if (playerIndex >= sequence.size) {
                score++
                playerIndex = 0
            }
        } else {
            gameOver = true
        }
    }

    suspend fun nextRound() {
        sequence.add(Random.nextInt(buttons.size))
        showingSequence = true

        // Show the sequence with visible flashing
        for (id in sequence) {
            highlightedButton = id
            delay(500L)           // button highlighted for 0.5s
            highlightedButton = -1
            delay(200L)           // short pause between flashes
        }

        showingSequence = false
        playerIndex = 0
    }
}

/* ---------------- COMPOSABLE ---------------- */

@Composable
fun SimonSaysGame(state: SimonSaysState = remember { SimonSaysState() }) {

    var roundStarted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text("Score: ${state.score}", fontSize = 28.sp, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        // 2x2 color button grid
        Column {
            for (row in 0 until 2) {
                Row {
                    for (col in 0 until 2) {
                        val index = row * 2 + col
                        val button = state.buttons[index]
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .padding(8.dp)
                                .background(
                                    if (state.highlightedButton == index) Color.White else button.color
                                )
                                .clickable {
                                    state.pressButton(index)
                                    // If sequence completed correctly, start next round
                                    if (!state.gameOver && state.playerIndex == 0 && !state.showingSequence) {
                                        roundStarted = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ){}
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Start Game button (only visible before first round)
        if (!roundStarted && state.sequence.isEmpty() && !state.gameOver) {
            Button(onClick = { roundStarted = true }) {
                Text("Start Game")
            }
        }

        // Game Over
        if (state.gameOver) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Game Over!", fontSize = 28.sp, color = Color.Red)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                state.restart()
                roundStarted = false
            }) {
                Text("Restart")
            }
        }
    }

    // Handle showing the next round
    LaunchedEffect(roundStarted) {
        if (roundStarted && !state.gameOver) {
            roundStarted = false
            state.nextRound()
        }
    }
}
