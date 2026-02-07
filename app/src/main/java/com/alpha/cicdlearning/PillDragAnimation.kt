package com.alpha.cicdlearning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PillDragAnimation() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ){
        Box(
            modifier = Modifier
                .padding(top = 24.dp)
                .align(Alignment.TopCenter)
                .width(180.dp)
                .heightIn(56.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(Color.White)
        ){

        }
    }
}