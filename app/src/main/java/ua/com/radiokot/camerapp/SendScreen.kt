package ua.com.radiokot.camerapp

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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
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
fun SendScreen(
    frameImage: ImageBitmap?,
    onSendClicked: () -> Unit,
    modifier: Modifier = Modifier,
) = Box(
    modifier = modifier
        .drawWithCache {
            val backgroundColor = Color(0xfffef6eb)
            val lineColor = Color(0xFFEEEDE6)
            val gridSize = 20.dp.roundToPx()
            val gridThickness = 1.dp.toPx()

            onDrawWithContent {
                drawRect(backgroundColor)

                for (y in ((0 - gridSize / 2)..size.height.toInt() step gridSize)) {
                    drawLine(
                        color = lineColor,
                        start = Offset(
                            x = 0f,
                            y = y.toFloat(),
                        ),
                        end = Offset(
                            x = size.width,
                            y = y.toFloat(),
                        ),
                        strokeWidth = gridThickness,
                    )
                }
                for (x in (0..size.width.toInt() step gridSize)) {
                    drawLine(
                        color = lineColor,
                        start = Offset(
                            x = x.toFloat(),
                            y = 0f,
                        ),
                        end = Offset(
                            x = x.toFloat(),
                            y = size.height,
                        ),
                        strokeWidth = gridThickness,
                    )
                }

                drawContent()
            }
        }
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
                    .size(FrameSize * 1.5f)
                    .dropShadow(
                        shape = RectangleShape,
                        shadow = Shadow(
                            radius = 16.dp,
                            spread = 8.dp,
                            color = Color(0x10000000),
                        )
                    )
            ) {
                if (frameImage != null) {
                    Image(
                        bitmap = frameImage,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
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
                        spread = 6.dp,
                        color = Color(0x20000000),
                    )
                )
                .clip(buttonShape)
                .background(
                    color = Color(0xffe5920f),
                )
                .clickable(
                    onClick = onSendClicked,
                )
        ) {
            BasicText(
                text = "Send",
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
private fun SendScreenPreview(

) {
    SendScreen(
        frameImage = null,
        onSendClicked = { },
        modifier = Modifier
            .fillMaxSize()
    )
}
