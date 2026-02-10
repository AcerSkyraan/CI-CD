package com.alpha.cicdlearning.FlappyBird.di

import androidx.compose.runtime.*
import kotlin.random.Random

/* ---------------- GAME STATE ---------------- */
/* ---------------- GAME STATE ---------------- */

/* ---------------- GAME STATE ---------------- */
/* ---------------- GAME STATE ---------------- */

class FlappyBirdState {

    // Bird
    var birdY by mutableStateOf(500f)
    var birdVelocity by mutableStateOf(0f)

    // Pipes
    data class Pipe(var x: Float, var gapY: Float)
    var pipes by mutableStateOf(listOf<Pipe>())

    // Score
    var score by mutableStateOf(0)
    var gameOver by mutableStateOf(false)

    // Screen size
    var screenWidth by mutableStateOf(1f)
    var screenHeight by mutableStateOf(1f)

    // Config
    val gravity = 0.5f
    val jumpPower = -10f
    val pipeWidth = 120f      // static width
    val pipeGap = 350f        // vertical gap
    val pipeSpacing = 600f    // horizontal spacing

    // Scored pipes
    private val scoredPipes = mutableSetOf<Pipe>()

    // Tap buffer: fast-tap boost
    private var lastFlapTime = 0L
    private val flapBoost = -5f

    fun restart() {
        birdY = screenHeight / 2
        birdVelocity = 0f
        pipes = listOf(
            Pipe(screenWidth + 200f, randomGap()),
            Pipe(screenWidth + 200f + pipeSpacing, randomGap()),
            Pipe(screenWidth + 200f + pipeSpacing * 2, randomGap())
        )
        score = 0
        gameOver = false
        scoredPipes.clear()
        lastFlapTime = 0L
    }

    private fun randomGap(): Float {
        return Random.nextFloat() * (screenHeight - pipeGap - 200f) + 100f
    }

    fun flap() {
        val now = System.currentTimeMillis()
        val boost = if (now - lastFlapTime < 200) flapBoost else 0f
        birdVelocity = jumpPower + boost
        lastFlapTime = now
    }

    fun update() {
        if (gameOver || screenWidth <= 1f) return

        // Gravity
        birdVelocity += gravity
        birdY += birdVelocity

        // Move pipes
        val newPipes = pipes.map { it.copy(x = it.x - 6f) }.toMutableList()

        // Remove offscreen safely
        if (newPipes.isNotEmpty() && newPipes.first().x + pipeWidth < 0) {
            newPipes.removeAt(0)
        }

        // Add new pipe (original flow)
        if (newPipes.isEmpty() || newPipes.last().x < screenWidth - pipeSpacing) {
            newPipes.add(Pipe(screenWidth, randomGap()))
        }

        pipes = newPipes

        // Collision & scoring
        for (pipe in pipes) {
            val birdX = birdX()
            val birdSize = 50f

            if (birdX + birdSize > pipe.x && birdX < pipe.x + pipeWidth &&
                (birdY < pipe.gapY || birdY + birdSize > pipe.gapY + pipeGap)
            ) {
                gameOver = true
            } else if (!scoredPipes.contains(pipe) && birdX > pipe.x + pipeWidth / 2) {
                score++
                scoredPipes.add(pipe)
            }
        }

        // Ground / ceiling
        if (birdY < 0 || birdY + 50f > screenHeight) {
            gameOver = true
        }
    }

    fun birdX(): Float = screenWidth / 4
}
