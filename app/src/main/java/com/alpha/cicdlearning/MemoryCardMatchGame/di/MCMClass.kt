package com.alpha.cicdlearning.MemoryCardMatchGame.di

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.alpha.cicdlearning.MemoryCardMatchGame.domain.models.MemoryCard
import kotlinx.coroutines.delay

class MemoryGameState {

    var cards by mutableStateOf(listOf<MemoryCard>())
    var firstSelected by mutableStateOf<MemoryCard?>(null)
    var secondSelected by mutableStateOf<MemoryCard?>(null)
    var moves by mutableStateOf(0)
    var gameWon by mutableStateOf(false)

    init {
        restart()
    }

    fun restart() {
        val icons = listOf("🐶","🐱","🐭","🐹","🦊","🐻","🐼","🐸")
        val pairList = (icons + icons)
            .shuffled()
            .mapIndexed { index, icon ->
                MemoryCard(index, icon)
            }

        cards = pairList
        firstSelected = null
        secondSelected = null
        moves = 0
        gameWon = false
    }

    fun onCardClick(card: MemoryCard) {
        if (card.isFlipped || card.isMatched || secondSelected != null) return

        card.isFlipped = true

        if (firstSelected == null) {
            firstSelected = card
        } else {
            secondSelected = card
            moves++
        }
    }

    suspend fun checkMatch() {
        val first = firstSelected
        val second = secondSelected ?: return

        delay(700)

        if (first?.content == second.content) {
            first.isMatched = true
            second.isMatched = true
        } else {
            first?.isFlipped = false
            second.isFlipped = false
        }

        firstSelected = null
        secondSelected = null

        if (cards.all { it.isMatched }) {
            gameWon = true
        }
    }
}

