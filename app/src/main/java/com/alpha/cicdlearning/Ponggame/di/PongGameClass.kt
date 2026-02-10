package com.alpha.cicdlearning.Ponggame.di

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs


/* ---------------- GAME STATE ---------------- */

/* ---------------- GAME STATE ---------------- */

class PongGameState {

    // Ball
    var ballX by mutableStateOf(300f)
    var ballY by mutableStateOf(600f)
    var ballVX by mutableStateOf(7f)
    var ballVY by mutableStateOf(7f)

    // Paddles
    var playerX by mutableStateOf(300f)
    var aiX by mutableStateOf(300f)

    // Scores
    var playerScore by mutableStateOf(0)
    var aiScore by mutableStateOf(0)

    var gameOver by mutableStateOf(false)

    // Board size (REAL screen)
    var boardWidth by mutableStateOf(1f)
    var boardHeight by mutableStateOf(1f)

    val paddleWidth = 220f
    val paddleHeight = 28f
    val ballSize = 26f

    fun restart() {
        ballX = boardWidth / 2
        ballY = boardHeight / 2
        ballVX = 7f
        ballVY = 7f
        playerScore = 0
        aiScore = 0
        gameOver = false
    }

    fun update() {
        if (gameOver || boardWidth <= 1f) return

        ballX += ballVX
        ballY += ballVY

        // Wall collision
        if (ballX <= 0 || ballX >= boardWidth - ballSize) {
            ballVX *= -1
        }

        // AI follow
        aiX += (ballX - aiX) * 0.06f

        val playerPaddleY = boardHeight - 120f
        val aiPaddleY = 80f

        // ----- PLAYER COLLISION -----
        if (
            ballY + ballSize >= playerPaddleY &&
            ballY <= playerPaddleY + paddleHeight &&
            ballX + ballSize >= playerX - paddleWidth / 2 &&
            ballX <= playerX + paddleWidth / 2
        ) {
            ballVY = -abs(ballVY)

            val hitPos = (ballX - playerX) / paddleWidth
            ballVX += hitPos * 3
        }

        // ----- AI COLLISION -----
        if (
            ballY <= aiPaddleY + paddleHeight &&
            ballY + ballSize >= aiPaddleY &&
            ballX + ballSize >= aiX - paddleWidth / 2 &&
            ballX <= aiX + paddleWidth / 2
        ) {
            ballVY = abs(ballVY)
        }

        // Score bottom
        if (ballY > boardHeight) {
            aiScore++
            resetBall(up = true)
        }

        // Score top
        if (ballY < 0) {
            playerScore++
            resetBall(up = false)
        }

        if (playerScore == 5 || aiScore == 5) {
            gameOver = true
        }
    }

    private fun resetBall(up: Boolean) {
        ballX = boardWidth / 2
        ballY = boardHeight / 2
        ballVY = if (up) -7f else 7f
    }
}
