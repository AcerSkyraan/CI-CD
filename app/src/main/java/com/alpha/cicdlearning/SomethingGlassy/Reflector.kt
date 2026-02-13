package com.alpha.cicdlearning.SomethingGlassy

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.alpha.cicdlearning.R
import kotlin.math.absoluteValue

@Composable
fun Reflectable(
    modifier: Modifier = Modifier,
    reflectionAlpha: Float = 0.4f,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {

        // Original Content
        content()

        // Reflection
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleY = -1f
                    alpha = reflectionAlpha
                }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black
                            )
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        ) {
            content()
        }
    }
}



@Composable
fun ReflectionSingleDemo1() {

    var pagerState = rememberPagerState (pageCount = {10})

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        HorizontalPager(
            pagerState
        ) {
            page ->
            Reflectable(reflectionAlpha = 0.35f) {

                Card(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .width(120.dp)
                        .height(260.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Image(
                            painter = painterResource(id = R.drawable.colorone),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "Compose UI",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Reflection Demo",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReflectionSingleDemo() {

    val pagerState = rememberPagerState(pageCount = { 10 })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {

        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(180.dp), // IMPORTANT
            contentPadding = PaddingValues(horizontal = 120.dp),
            pageSpacing = 20.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->

            val pageOffset =
                ((pagerState.currentPage - page) +
                        pagerState.currentPageOffsetFraction).absoluteValue

            val fraction = 1f - pageOffset.coerceIn(0f, 1f)

            val scale = lerp(0.75f, 1f, fraction)
            val height = lerp(190.dp, 260.dp, fraction)
            val alpha = lerp(0.5f, 1f, fraction)

            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                        clip = false
                    },
                contentAlignment = Alignment.Center
            ) {

                Reflectable(reflectionAlpha = 0.35f) {

                    Card(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .width(140.dp)
                            .height(height),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {

                            Image(
                                painter = painterResource(R.drawable.colorone),
                                contentDescription = null,
                                modifier = Modifier.size(80.dp)
                            )

                            Spacer(Modifier.height(10.dp))

                            Text(
                                "Compose UI",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "Reflection",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CubeReflectionCarousel() {

    val pagerState = rememberPagerState(pageCount = { 10 })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {

        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->

            val pageOffset =
                (pagerState.currentPage - page) +
                        pagerState.currentPageOffsetFraction

            val absOffset = kotlin.math.abs(pageOffset)

            val rotation = pageOffset * 50f
            val scale = 1f - absOffset.coerceIn(0f, 1f) * 0.25f
            val alpha = 1f - absOffset.coerceIn(0f, 1f) * 0.4f

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        rotationY = rotation
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                        cameraDistance = 12 * density
                        transformOrigin =
                            TransformOrigin(
                                pivotFractionX = if (pageOffset > 0) 0f else 1f,
                                pivotFractionY = 0.5f
                            )
                    },
                contentAlignment = Alignment.Center
            ) {

                Reflectable(reflectionAlpha = 0.3f) {

                    Card(
                        modifier = Modifier
                            .width(160.dp)
                            .height(260.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(10.dp)
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {

                            Image(
                                painter = painterResource(R.drawable.colorone),
                                contentDescription = null,
                                modifier = Modifier.size(90.dp)
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                "Compose UI",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "Cube Carousel",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}






