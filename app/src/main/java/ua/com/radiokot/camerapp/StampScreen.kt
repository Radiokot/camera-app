package ua.com.radiokot.camerapp

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.skydoves.landscapist.image.LandscapistImage
import kotlinx.coroutines.delay
import java.time.LocalDate

@Composable
fun StampScreen(
    modifier: Modifier = Modifier,
    stampId: String,
    caption: String?,
    imageUri: String,
    takenAt: LocalDate,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) = Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
) {
    val detailsAlpha = remember {
        Animatable(0f)
    }
    LaunchedEffect(Unit) {
        delay(100)
        detailsAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(400),
        )
    }

    BasicText(
        text = caption ?: "",
        style = TextStyle(
            fontFamily = podkovaFamily,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .graphicsLayer {
                alpha = detailsAlpha.value
            }
    )

    LandscapistImage(
        imageModel = { imageUri.toUri() },
        modifier = Modifier
            .size(StampSize * 2f)
            .run {
                if (sharedTransitionScope == null || animatedVisibilityScope == null) {
                    return@run this
                }

                with(sharedTransitionScope) {
                    sharedElement(
                        sharedContentState = rememberSharedContentState(stampId),
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                }
            }
            .dropShadow(
                shape = RectangleShape,
                shadow = Shadow(
                    radius = 16.dp,
                    color = Color(0x7447525E),
                )
            )
            .run {
                if (imageUri.isNotEmpty()) {
                    return@run this
                }

                background(Color.Yellow)
            }
    )

    BasicText(
        text = takenAt.toString(),
        style = TextStyle(
            fontFamily = podkovaFamily,
            fontSize = 16.sp,
            color = Color(0xFFB9AC8C),
            textAlign = TextAlign.Center,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .graphicsLayer {
                alpha = detailsAlpha.value
            }
    )
}

@Preview
@Composable
private fun StampScreenPreview(

) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paperBackground()
    ) {
        StampScreen(
            stampId = "",
            caption = "My stamp",
            imageUri = "",
            takenAt = LocalDate.now(),
            sharedTransitionScope = null,
            animatedVisibilityScope = null,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}
