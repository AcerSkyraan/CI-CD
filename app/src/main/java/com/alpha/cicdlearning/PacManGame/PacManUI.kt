package com.example.pacman

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.*

// Game Constants
const val GRID_SIZE = 20
const val BASE_GAME_SPEED = 150L // milliseconds per frame
const val ANIMATION_STEPS = 8 // Steps for smooth movement interpolation

// Direction enum
enum class Direction(val dx: Int, val dy: Int, val angle: Float) {
    RIGHT(1, 0, 0f),
    DOWN(0, 1, 90f),
    LEFT(-1, 0, 180f),
    UP(0, -1, 270f)
}

// Game entities
data class Position(val x: Int, val y: Int) {
    operator fun plus(dir: Direction) = Position(x + dir.dx, y + dir.dy)
}

data class Ghost(
    var position: Position,
    val color: Color,
    var direction: Direction = Direction.RIGHT,
    var scatter: Boolean = false
)

// Smooth position for animations
data class SmoothPosition(val x: Float, val y: Float)

// Game State
class PacManGameState {
    var pacManPosition by mutableStateOf(Position(1, 1))
    var pacManSmoothPos by mutableStateOf(SmoothPosition(1f, 1f))
    var direction by mutableStateOf(Direction.RIGHT)
    var nextDirection by mutableStateOf(Direction.RIGHT)
    var score by mutableIntStateOf(0)
    var lives by mutableIntStateOf(3)
    var gameOver by mutableStateOf(false)
    var won by mutableStateOf(false)

    // Pellets and power pellets
    val pellets = mutableStateListOf<Position>()
    val powerPellets = mutableStateListOf<Position>()

    // Ghosts with smooth positions
    val ghosts = mutableStateListOf<Ghost>()
    val ghostSmoothPositions = mutableStateMapOf<Ghost, SmoothPosition>()
    var powerMode by mutableStateOf(false)
    var powerModeTimer by mutableIntStateOf(0)

    // Speed control
    var ghostSpeedMultiplier by mutableFloatStateOf(1.0f) // 0.5 to 2.0
    var updateCounter by mutableIntStateOf(0)

    // Maze (1 = wall, 0 = path)
    val maze = arrayOf(
        intArrayOf(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1),
        intArrayOf(1,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,1),
        intArrayOf(1,0,1,1,0,1,1,1,0,1,1,0,1,1,1,0,1,1,0,1),
        intArrayOf(1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1),
        intArrayOf(1,0,1,1,0,1,0,1,1,1,1,1,1,0,1,0,1,1,0,1),
        intArrayOf(1,0,0,0,0,1,0,0,0,1,1,0,0,0,1,0,0,0,0,1),
        intArrayOf(1,1,1,1,0,1,1,1,0,1,1,0,1,1,1,0,1,1,1,1),
        intArrayOf(0,0,0,1,0,1,0,0,0,0,0,0,0,0,1,0,1,0,0,0),
        intArrayOf(1,1,1,1,0,1,0,1,1,0,0,1,1,0,1,0,1,1,1,1),
        intArrayOf(0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0),
        intArrayOf(1,1,1,1,0,1,0,1,1,1,1,1,1,0,1,0,1,1,1,1),
        intArrayOf(0,0,0,1,0,1,0,0,0,0,0,0,0,0,1,0,1,0,0,0),
        intArrayOf(1,1,1,1,0,1,0,1,1,1,1,1,1,0,1,0,1,1,1,1),
        intArrayOf(1,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,1),
        intArrayOf(1,0,1,1,0,1,1,1,0,1,1,0,1,1,1,0,1,1,0,1),
        intArrayOf(1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1),
        intArrayOf(1,1,0,1,0,1,0,1,1,1,1,1,1,0,1,0,1,0,1,1),
        intArrayOf(1,0,0,0,0,1,0,0,0,1,1,0,0,0,1,0,0,0,0,1),
        intArrayOf(1,0,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,0,1),
        intArrayOf(1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1),
        intArrayOf(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1),
    )

    fun initializeGame() {
        // Initialize pellets
        pellets.clear()
        powerPellets.clear()
        for (y in maze.indices) {
            for (x in maze[y].indices) {
                if (maze[y][x] == 0) {
                    pellets.add(Position(x, y))
                }
            }
        }

        // Add power pellets at corners
        powerPellets.addAll(listOf(
            Position(1, 1),
            Position(18, 1),
            Position(1, 18),
            Position(18, 18)
        ))
        pellets.removeAll(powerPellets)

        // Initialize ghosts
        ghosts.clear()
        ghostSmoothPositions.clear()
        val initialGhosts = listOf(
            Ghost(Position(9, 8), Color(0xFFFF0000)), // Red
            Ghost(Position(10, 8), Color(0xFFFFB8FF)), // Pink
            Ghost(Position(9, 10), Color(0xFF00FFFF)), // Cyan
            Ghost(Position(10, 10), Color(0xFFFFB852))  // Orange
        )
        ghosts.addAll(initialGhosts)
        initialGhosts.forEach { ghost ->
            ghostSmoothPositions[ghost] = SmoothPosition(ghost.position.x.toFloat(), ghost.position.y.toFloat())
        }

        pacManPosition = Position(1, 1)
        pacManSmoothPos = SmoothPosition(1f, 1f)
        direction = Direction.RIGHT
        nextDirection = Direction.RIGHT
        score = 0
        lives = 3
        gameOver = false
        won = false
        powerMode = false
        powerModeTimer = 0
        updateCounter = 0
    }

    fun isWall(pos: Position): Boolean {
        if (pos.y < 0 || pos.y >= maze.size || pos.x < 0 || pos.x >= maze[0].size) {
            return true
        }
        return maze[pos.y][pos.x] == 1
    }

    fun update() {
        if (gameOver || won) return

        updateCounter++

        // Smooth interpolation for Pac-Man
        pacManSmoothPos = SmoothPosition(
            lerp(pacManSmoothPos.x, pacManPosition.x.toFloat(), 0.3f),
            lerp(pacManSmoothPos.y, pacManPosition.y.toFloat(), 0.3f)
        )

        // Try to change direction
        val nextPos = pacManPosition + nextDirection
        if (!isWall(nextPos)) {
            direction = nextDirection
        }

        // Move Pac-Man
        val newPos = pacManPosition + direction
        if (!isWall(newPos)) {
            pacManPosition = newPos

            // Check pellet collision
            if (pellets.remove(pacManPosition)) {
                score += 10
            }

            // Check power pellet collision
            if (powerPellets.remove(pacManPosition)) {
                score += 50
                powerMode = true
                powerModeTimer = 30 // 30 frames of power mode
            }
        }

        // Update power mode
        if (powerMode) {
            powerModeTimer--
            if (powerModeTimer <= 0) {
                powerMode = false
            }
        }

        // Move ghosts based on speed multiplier
        val ghostMoveInterval = max(1, (1.0f / ghostSpeedMultiplier).toInt())
        if (updateCounter % ghostMoveInterval == 0) {
            for (ghost in ghosts) {
                moveGhost(ghost)
            }
        }

        // Smooth interpolation for ghosts
        ghosts.forEach { ghost ->
            val currentSmooth = ghostSmoothPositions[ghost] ?: SmoothPosition(ghost.position.x.toFloat(), ghost.position.y.toFloat())
            ghostSmoothPositions[ghost] = SmoothPosition(
                lerp(currentSmooth.x, ghost.position.x.toFloat(), 0.25f),
                lerp(currentSmooth.y, ghost.position.y.toFloat(), 0.25f)
            )
        }

        // Check ghost collision
        checkGhostCollision()

        // Check win condition
        if (pellets.isEmpty() && powerPellets.isEmpty()) {
            won = true
        }
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + (end - start) * fraction
    }

    private fun moveGhost(ghost: Ghost) {
        val possibleDirections = Direction.entries.filter { dir ->
            !isWall(ghost.position + dir) &&
                    dir != getOppositeDirection(ghost.direction)
        }

        if (possibleDirections.isEmpty()) return

        // Simple AI: In power mode, move away from Pac-Man, otherwise chase
        val targetPos = if (powerMode) {
            // Move away from Pac-Man
            Position(
                ghost.position.x + (ghost.position.x - pacManPosition.x).sign,
                ghost.position.y + (ghost.position.y - pacManPosition.y).sign
            )
        } else {
            pacManPosition
        }

        val bestDirection = possibleDirections.minByOrNull { dir ->
            val newPos = ghost.position + dir
            val dx = (newPos.x - targetPos.x).toDouble()
            val dy = (newPos.y - targetPos.y).toDouble()
            sqrt(dx * dx + dy * dy)
        } ?: possibleDirections.random()

        ghost.direction = bestDirection
        ghost.position = ghost.position + bestDirection
    }

    private fun getOppositeDirection(dir: Direction): Direction = when(dir) {
        Direction.UP -> Direction.DOWN
        Direction.DOWN -> Direction.UP
        Direction.LEFT -> Direction.RIGHT
        Direction.RIGHT -> Direction.LEFT
    }

    private fun checkGhostCollision() {
        ghosts.forEach { ghost ->
            if (ghost.position == pacManPosition) {
                if (powerMode) {
                    // Eat ghost
                    score += 200
                    val resetPos = Position(9 + ghosts.indexOf(ghost) % 2, 9)
                    ghost.position = resetPos
                    ghostSmoothPositions[ghost] = SmoothPosition(resetPos.x.toFloat(), resetPos.y.toFloat())
                } else {
                    // Lose life
                    lives--
                    if (lives <= 0) {
                        gameOver = true
                    } else {
                        pacManPosition = Position(1, 1)
                        pacManSmoothPos = SmoothPosition(1f, 1f)
                        direction = Direction.RIGHT
                        nextDirection = Direction.RIGHT
                    }
                }
            }
        }
    }
}

@Composable
fun PacManGame() {
    val gameState = remember { PacManGameState() }
    var showSettings by remember { mutableStateOf(false) }

    // Calculate cell size based on screen size
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val cellSize = remember(screenWidth, screenHeight) {
        minOf(screenWidth.value, screenHeight.value) / (GRID_SIZE + 1)
    }

    LaunchedEffect(Unit) {
        gameState.initializeGame()

        while (isActive) {
            delay(BASE_GAME_SPEED)
            gameState.update()
        }
    }

    // Mouth animation
    val infiniteTransition = rememberInfiniteTransition(label = "mouth")
    val mouthAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 45f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mouthAngle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Game Canvas - Full screen
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val (dx, dy) = dragAmount
                        gameState.nextDirection = when {
                            abs(dx) > abs(dy) -> if (dx > 0) Direction.RIGHT else Direction.LEFT
                            else -> if (dy > 0) Direction.DOWN else Direction.UP
                        }
                    }
                }
        ) {
            val offsetX = (size.width - (GRID_SIZE * cellSize)) / 2
            val offsetY = (size.height - (GRID_SIZE * cellSize)) / 2

            // Draw maze
            drawMaze(gameState, cellSize, offsetX, offsetY)

            // Draw pellets
            gameState.pellets.forEach { pos ->
                drawCircle(
                    color = Color.White,
                    radius = cellSize * 0.1f,
                    center = Offset(
                        offsetX + pos.x * cellSize + cellSize / 2,
                        offsetY + pos.y * cellSize + cellSize / 2
                    )
                )
            }

            // Draw power pellets with pulse effect
            gameState.powerPellets.forEach { pos ->
                drawCircle(
                    color = Color.White,
                    radius = cellSize * 0.25f,
                    center = Offset(
                        offsetX + pos.x * cellSize + cellSize / 2,
                        offsetY + pos.y * cellSize + cellSize / 2
                    )
                )
            }

            // Draw ghosts with smooth positions
            gameState.ghosts.forEach { ghost ->
                val smoothPos = gameState.ghostSmoothPositions[ghost]
                    ?: SmoothPosition(ghost.position.x.toFloat(), ghost.position.y.toFloat())
                drawGhost(ghost, gameState.powerMode, cellSize, offsetX, offsetY, smoothPos)
            }

            // Draw Pac-Man with smooth position
            drawPacMan(gameState.pacManSmoothPos, gameState.direction, mouthAngle, cellSize, offsetX, offsetY)
        }

        // UI Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Score and Lives at top
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Score: ${gameState.score}",
                    color = Color.Yellow,
                    fontSize = 24.sp,
                    style = MaterialTheme.typography.headlineMedium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Lives: ",
                        color = Color.White,
                        fontSize = 24.sp
                    )
                    repeat(gameState.lives) {
                        Text("🟡", fontSize = 20.sp)
                    }
                }

                // Settings button
                IconButton(
                    onClick = { showSettings = !showSettings },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Settings Panel
            if (showSettings) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xCC000000)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Ghost Speed: ${String.format("%.1fx", gameState.ghostSpeedMultiplier)}",
                            color = Color.White,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Slider(
                            value = gameState.ghostSpeedMultiplier,
                            onValueChange = { gameState.ghostSpeedMultiplier = it },
                            valueRange = 0.3f..2.5f,
                            steps = 21,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Yellow,
                                activeTrackColor = Color.Yellow,
                                inactiveTrackColor = Color.Gray
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Slower", color = Color.Gray, fontSize = 12.sp)
                            Text("Faster", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Game Over / Win message
            if (gameState.gameOver || gameState.won) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xDD000000)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (gameState.won) "🎉 YOU WIN! 🎉" else "☠️ GAME OVER",
                            color = if (gameState.won) Color(0xFF00FF00) else Color(0xFFFF0000),
                            fontSize = 32.sp,
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            "Final Score: ${gameState.score}",
                            color = Color.Yellow,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                gameState.initializeGame()
                                showSettings = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Yellow,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                "Play Again",
                                fontSize = 20.sp,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

fun DrawScope.drawMaze(gameState: PacManGameState, cellSize: Float, offsetX: Float, offsetY: Float) {
    for (y in gameState.maze.indices) {
        for (x in gameState.maze[y].indices) {
            if (gameState.maze[y][x] == 1) {
                drawRect(
                    color = Color(0xFF1976D2),
                    topLeft = Offset(offsetX + x * cellSize, offsetY + y * cellSize),
                    size = Size(cellSize, cellSize)
                )
            }
        }
    }
}

fun DrawScope.drawPacMan(
    position: SmoothPosition,
    direction: Direction,
    mouthAngle: Float,
    cellSize: Float,
    offsetX: Float,
    offsetY: Float
) {
    val center = Offset(
        offsetX + position.x * cellSize + cellSize / 2,
        offsetY + position.y * cellSize + cellSize / 2
    )

    rotate(direction.angle, center) {
        val path = Path().apply {
            moveTo(center.x, center.y)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    center.x - cellSize / 2,
                    center.y - cellSize / 2,
                    center.x + cellSize / 2,
                    center.y + cellSize / 2
                ),
                startAngleDegrees = mouthAngle,
                sweepAngleDegrees = 360 - (mouthAngle * 2),
                forceMoveTo = false
            )
            close()
        }

        drawPath(
            path = path,
            color = Color.Yellow
        )
    }
}

fun DrawScope.drawGhost(
    ghost: Ghost,
    scared: Boolean,
    cellSize: Float,
    offsetX: Float,
    offsetY: Float,
    smoothPos: SmoothPosition
) {
    val center = Offset(
        offsetX + smoothPos.x * cellSize + cellSize / 2,
        offsetY + smoothPos.y * cellSize + cellSize / 2
    )

    val color = if (scared) Color(0xFF3F51B5) else ghost.color

    // Body (circle top)
    drawCircle(
        color = color,
        radius = cellSize / 2,
        center = center.copy(y = center.y - cellSize / 6)
    )

    // Body (rectangle bottom)
    drawRect(
        color = color,
        topLeft = Offset(center.x - cellSize / 2, center.y - cellSize / 6),
        size = Size(cellSize, cellSize * 0.6f)
    )

    // Wavy bottom
    val waveWidth = cellSize / 4
    for (i in 0 until 4) {
        val path = Path().apply {
            moveTo(center.x - cellSize / 2 + i * waveWidth, center.y + cellSize * 0.44f)
            lineTo(center.x - cellSize / 2 + i * waveWidth + waveWidth / 2, center.y + cellSize * 0.5f)
            lineTo(center.x - cellSize / 2 + (i + 1) * waveWidth, center.y + cellSize * 0.44f)
            close()
        }
        drawPath(path, color)
    }

    // Eyes
    if (scared) {
        // Simple dots when scared
        drawCircle(
            color = Color.White,
            radius = cellSize * 0.08f,
            center = center.copy(x = center.x - cellSize * 0.15f, y = center.y - cellSize * 0.1f)
        )
        drawCircle(
            color = Color.White,
            radius = cellSize * 0.08f,
            center = center.copy(x = center.x + cellSize * 0.15f, y = center.y - cellSize * 0.1f)
        )
    } else {
        // Normal eyes
        // Left eye
        drawCircle(
            color = Color.White,
            radius = cellSize * 0.12f,
            center = center.copy(x = center.x - cellSize * 0.15f, y = center.y - cellSize * 0.1f)
        )
        drawCircle(
            color = Color(0xFF000080),
            radius = cellSize * 0.06f,
            center = center.copy(x = center.x - cellSize * 0.15f, y = center.y - cellSize * 0.1f)
        )
        // Right eye
        drawCircle(
            color = Color.White,
            radius = cellSize * 0.12f,
            center = center.copy(x = center.x + cellSize * 0.15f, y = center.y - cellSize * 0.1f)
        )
        drawCircle(
            color = Color(0xFF000080),
            radius = cellSize * 0.06f,
            center = center.copy(x = center.x + cellSize * 0.15f, y = center.y - cellSize * 0.1f)
        )
    }
}