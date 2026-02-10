package com.alpha.cicdlearning.MemoryCardMatchGame.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpha.cicdlearning.MemoryCardMatchGame.di.MemoryGameState
import com.alpha.cicdlearning.MemoryCardMatchGame.domain.models.MemoryCard

@Composable
fun MemoryCardView(card: MemoryCard, onClick: () -> Unit) {

    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "flip"
    )

    val isFront = rotation > 90f

    Box(
        modifier = Modifier
            .size(80.dp)
            .padding(6.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .background(
                if (isFront) Color.White else Color.Gray
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {

        if (isFront) {
            // Rotate content back so emoji is not mirrored
            Text(
                text = card.content,
                fontSize = 28.sp,
                modifier = Modifier.graphicsLayer {
                    rotationY = 180f
                }
            )
        }
    }
}




@Composable
fun MemoryBoard(state: MemoryGameState) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.padding(16.dp)
    ) {
        items(state.cards.size) { index ->
            val card = state.cards[index]

            MemoryCardView(card) {
                state.onCardClick(card)
            }
        }
    }
}



@Composable
fun MemoryGameScreen() {

    val state = remember { MemoryGameState() }

    LaunchedEffect(state.secondSelected) {
        if (state.secondSelected != null) {
            state.checkMatch()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Moves: ${state.moves}",
            color = Color.White,
            fontSize = 22.sp
        )

        Spacer(Modifier.height(16.dp))

        MemoryBoard(state)

        Spacer(Modifier.height(16.dp))

        Button(onClick = { state.restart() }) {
            Text("Restart")
        }

        if (state.gameWon) {
            Spacer(Modifier.height(12.dp))
            Text("YOU WON 🎉", color = Color.Green, fontSize = 28.sp)
        }
    }
}


