package ua.com.radiokot.camerapp

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.skydoves.landscapist.image.LandscapistImage

@Composable
fun StampScreen(
    modifier: Modifier = Modifier,
    stampId: String,
    thumbnailUrl: String,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) = Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
) {
    LandscapistImage(
        imageModel = { thumbnailUrl.toUri() },
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
                if (thumbnailUrl.isNotEmpty()) {
                    return@run this
                }

                background(Color.Yellow)
            }
    )
}

@Preview
@Composable
private fun StampScreenPreview(

) {
    StampScreen(
        stampId = "",
        thumbnailUrl = "",
        sharedTransitionScope = null,
        animatedVisibilityScope = null,
        modifier = Modifier
            .fillMaxSize()
    )
}
