package ua.com.radiokot.camerapp

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SaveScreen(
    frameImage: ImageBitmap?,
    onSaveClicked: () -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    modifier: Modifier = Modifier,
) = Box(
    modifier = modifier
        .paperBackground()
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f, true)
        ) {
            Box(
                modifier = Modifier
                    .size(StampSize * 2f)
                    .dropShadow(
                        shape = RectangleShape,
                        shadow = Shadow(
                            radius = 16.dp,
                            color = Color(0x7447525E),
                        )
                    )
            ) {
                if (frameImage != null) {
                    Image(
                        bitmap = frameImage,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .run {
                                if (sharedTransitionScope == null || animatedVisibilityScope == null) {
                                    return@run this
                                }

                                with(sharedTransitionScope) {
                                    sharedElement(
                                        sharedContentState = rememberSharedContentState("image"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                    )
                                }
                            }
                    )
                }
            }
        }

        val buttonShape = RoundedCornerShape(
            percent = 50,
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(24.dp)
                .height(56.dp)
                .fillMaxWidth()
                .dropShadow(
                    shape = buttonShape,
                    shadow = Shadow(
                        radius = 8.dp,
                        spread = 2.dp,
                        color = Color(0x414A6585),
                    )
                )
                .clip(buttonShape)
                .background(
                    color = Color(0xffe5920f),
                )
                .clickable(
                    onClick = onSaveClicked,
                )
        ) {
            BasicText(
                text = "Save to Gallery",
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF212121),
                ),
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
private fun SaveScreenPreview(

) {
    SaveScreen(
        frameImage = null,
        onSaveClicked = { },
        sharedTransitionScope = null,
        animatedVisibilityScope = null,
        modifier = Modifier
            .fillMaxSize()
    )
}
