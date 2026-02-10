package com.alpha.cicdlearning.flappybird

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import com.alpha.cicdlearning.FlappyBird.di.FlappyBirdState
import kotlinx.coroutines.delay
import kotlin.random.Random



/* ---------------- GAME LOOP ---------------- */

@Composable
fun FlappyBirdLoop(state: FlappyBirdState) {
    LaunchedEffect(state.gameOver) {
        while (!state.gameOver) {
            delay(16)
            state.update()
        }
    }
}

/* ---------------- CONTROLS ---------------- */

fun Modifier.flappyControls(state: FlappyBirdState) = pointerInput(Unit) {
    detectTapGestures { state.flap() }
}

/* ---------------- BOARD ---------------- */

@Composable
fun FlappyBirdBoard(state: FlappyBirdState) {

    Canvas(modifier = Modifier.fillMaxSize()) {

        val w = size.width
        val h = size.height
        state.screenWidth = w
        state.screenHeight = h

        val birdSize = 50f

        // Bird
        drawRect(
            color = Color.Yellow,
            topLeft = Offset(state.birdX(), state.birdY),
            size = Size(birdSize, birdSize)
        )

        // Pipes
        for (pipe in state.pipes) {
            // Top pipe
            drawRect(
                color = Color.Green,
                topLeft = Offset(pipe.x, 0f),
                size = Size(state.pipeWidth, pipe.gapY)
            )
            // Bottom pipe
            drawRect(
                color = Color.Green,
                topLeft = Offset(pipe.x, pipe.gapY + state.pipeGap),
                size = Size(state.pipeWidth, state.screenHeight - pipe.gapY - state.pipeGap)
            )
        }
    }
}

/* ---------------- MAIN SCREEN ---------------- */

@Composable
fun FlappyBirdGame() {

    val state = remember { FlappyBirdState().apply { restart() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Cyan)
            .flappyControls(state)
    ) {

        FlappyBirdBoard(state)
        FlappyBirdLoop(state)

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
