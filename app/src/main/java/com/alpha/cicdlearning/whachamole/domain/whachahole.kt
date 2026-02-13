package com.alpha.cicdlearning.whackamole

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/* ---------------- GAME STATE ---------------- */

class WhackAMoleState {

    companion object {
        const val ROWS = 3
        const val COLS = 3
        const val TOTAL_TIME = 30 // seconds
    }

    var score by mutableStateOf(0)
    var timeLeft by mutableStateOf(TOTAL_TIME)
    var molePosition by mutableStateOf(-1) // index 0..8
    var gameOver by mutableStateOf(false)

    var moleSpeed by mutableStateOf(800L) // default 0.8s

    fun restart() {
        score = 0
        timeLeft = TOTAL_TIME
        molePosition = -1
        gameOver = false
    }

    suspend fun startGame() {
        while (!gameOver) {
            delay(moleSpeed)
            if (!gameOver) molePosition = Random.nextInt(ROWS * COLS)
            timeLeft--
            if (timeLeft <= 0) gameOver = true
        }
    }

    fun hit(index: Int) {
        if (!gameOver && index == molePosition) {
            score += 1
            molePosition = -1 // mole disappears immediately after hit
        }
    }
}

/* ---------------- COMPOSABLE ---------------- */

@Composable
fun WhackAMoleGame(state: WhackAMoleState = remember { WhackAMoleState() }) {

    var gameStarted by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF81C784)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text("Score: ${state.score}", fontSize = 28.sp, color = Color.White)
        Text("Time: ${state.timeLeft}", fontSize = 20.sp, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        // Mole Speed Slider
        Text("Mole Speed: ${(state.moleSpeed / 100).toInt()}") // show in 0.1s units
        Slider(
            value = state.moleSpeed.toFloat(),
            onValueChange = { state.moleSpeed = it.toLong() },
            valueRange = 200f..1500f
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column {
            for (r in 0 until WhackAMoleState.ROWS) {
                Row {
                    for (c in 0 until WhackAMoleState.COLS) {
                        val index = r * WhackAMoleState.COLS + c
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .padding(8.dp)
                                .background(
                                    if (index == state.molePosition) Color.Red.copy(.5f) else Color.LightGray
                                )
                                .clickable { state.hit(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (index == state.molePosition)
                                Text("🐹", fontSize = 32.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!gameStarted) {
            Button(onClick = {
                gameStarted = true
                state.restart()
                // launch game loop
//                LaunchedEffect(Unit) {
                scope.launch {
                    state.startGame()
                }
//                }
            }) {
                Text("Start Game")
            }
        } else if (state.gameOver) {
            Text("Game Over!", fontSize = 28.sp, color = Color.Red)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                state.restart()
                gameStarted = false
            }) {
                Text("Restart")
            }
        }
    }
}
