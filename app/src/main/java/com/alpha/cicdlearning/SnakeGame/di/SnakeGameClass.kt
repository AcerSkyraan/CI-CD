package com.alpha.cicdlearning.SnakeGame.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.alpha.cicdlearning.SnakeGame.domain.Direction
import com.alpha.cicdlearning.SnakeGame.domain.Point

class SnakeGameState {

    var snake = mutableStateListOf(Point(5, 5))
    var direction by mutableStateOf(Direction.RIGHT)
    var food by mutableStateOf(Point(10, 10))
    var gameOver by mutableStateOf(false)

    val boardSize = 20

    fun move() {
        if (gameOver) return

        val head = snake.first()
        val newHead = when (direction) {
            Direction.UP -> Point(head.x, head.y - 1)
            Direction.DOWN -> Point(head.x, head.y + 1)
            Direction.LEFT -> Point(head.x - 1, head.y)
            Direction.RIGHT -> Point(head.x + 1, head.y)
        }

        // Collision with wall
        if (newHead.x !in 0 until boardSize ||
            newHead.y !in 0 until boardSize ||
            snake.contains(newHead)
        ) {
            gameOver = true
            return
        }

        snake.add(0, newHead)

        if (newHead == food) {
            spawnFood()
        } else {
            snake.removeLast()
        }
    }

    private fun spawnFood() {
        food = Point((0 until boardSize).random(), (0 until boardSize).random())
    }


    fun restart() {
        snake.clear()
        snake.add(Point(5, 5))
        direction = Direction.RIGHT
        food = Point(10, 10)
        gameOver = false
    }


}


