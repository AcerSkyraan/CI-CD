package com.alpha.cicdlearning.pong

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.sp
import com.alpha.cicdlearning.Ponggame.di.PongGameState
import kotlinx.coroutines.delay
import kotlin.math.abs



/* ---------------- GAME LOOP ---------------- */

@Composable
fun PongLoop(state: PongGameState) {
    LaunchedEffect(state.gameOver) {
        while (!state.gameOver) {
            delay(16)
            state.update()
        }
    }
}

/* ---------------- CONTROLS ---------------- */

fun Modifier.pongControls(state: PongGameState) = pointerInput(Unit) {
    detectDragGestures { change, _ ->
        state.playerX = change.position.x
    }
}

/* ---------------- BOARD ---------------- */

@Composable
fun PongBoard(state: PongGameState) {

    Canvas(modifier = Modifier.fillMaxSize()) {

        val w = size.width
        val h = size.height

        // IMPORTANT: Send real size to state
        state.boardWidth = w
        state.boardHeight = h

        val playerPaddleY = h - 120f
        val aiPaddleY = 80f

        // Ball
        drawRect(
            Color.White,
            topLeft = Offset(state.ballX, state.ballY),
            size = Size(state.ballSize, state.ballSize)
        )

        // Player Paddle
        drawRect(
            Color.Green,
            topLeft = Offset(
                state.playerX - state.paddleWidth / 2,
                playerPaddleY
            ),
            size = Size(state.paddleWidth, state.paddleHeight)
        )

        // AI Paddle
        drawRect(
            Color.Red,
            topLeft = Offset(
                state.aiX - state.paddleWidth / 2,
                aiPaddleY
            ),
            size = Size(state.paddleWidth, state.paddleHeight)
        )
    }
}

/* ---------------- MAIN SCREEN ---------------- */

@Composable
fun PongGame() {

    val state = remember { PongGameState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pongControls(state)
    ) {

        PongBoard(state)
        PongLoop(state)

        Text(
            text = "Player ${state.playerScore} : ${state.aiScore} AI",
            color = Color.White,
            fontSize = 22.sp,
            modifier = Modifier.align(Alignment.TopCenter)
        )

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
