package com.alpha.cicdlearning.MemoryCardMatchGame.domain.models

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue



data class MemoryCard(
    val id: Int,
    val content: String,
    var isFlippedInit: Boolean = false,
    var isMatchedInit: Boolean = false
) {
    var isFlipped by mutableStateOf(isFlippedInit)
    var isMatched by mutableStateOf(isMatchedInit)
}
