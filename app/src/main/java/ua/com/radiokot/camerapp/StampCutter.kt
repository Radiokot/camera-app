package ua.com.radiokot.camerapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
fun StampCutter(
    modifier: Modifier = Modifier,
    frameSize: DpSize,
    onPressed: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        val stampPainter = painterResource(R.drawable.stamp_a)
        val stampStrokePainter = painterResource(R.drawable.stamp_a_stroke)
        val strokeColor = Color(0xFF6B624B)

        Spacer(
            modifier = Modifier
                .fillMaxSize(0.8f)
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithCache {
                    val stampPaint = Paint().apply {
                        blendMode = BlendMode.DstOut
                    }
                    val stampSize = frameSize.toSize()
                    val stampRect = Rect(
                        offset = Offset.Zero,
                        size = stampSize,
                    )
                    val stampOffset = Offset(
                        x = (size.width - stampSize.width) / 2,
                        y = (size.height - stampSize.height) / 2,
                    )
                    val brush = Brush.linearGradient(
                        0f to Color(0xFFABA59C),
                        0.3f to Color(0xFFE3DDD3),
                        0.6f to Color(0xFF8C827B),
                        start = Offset.Zero,
                        end = Offset(
                            x = 0f,
                            y = size.height,
                        )
                    )
                    val strokeColorFilter = ColorFilter.tint(strokeColor)

                    onDrawBehind {
                        drawRect(brush)
                        drawIntoCanvas { canvas ->
                            canvas.translate(stampOffset.x, stampOffset.y)
                            canvas.withSaveLayer(stampRect, stampPaint) {
                                with(stampPainter) {
                                    draw(stampSize)
                                }
                            }
                            with(stampStrokePainter) {
                                draw(
                                    size = stampSize,
                                    colorFilter = strokeColorFilter,
                                )
                            }
                            canvas.translate(-stampOffset.x, -stampOffset.y)
                        }
                    }
                }
        )

        val outerCornerRadius = 24.dp

        LeButton(
            innerColor = Color.Transparent,
            cornerRadius = outerCornerRadius,
            depth = 18.dp,
            onClick = onPressed,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithCache {
                        val outerColor = Color(0xFFfff9eb)
                        val outerCornerRadius = CornerRadius(
                            x = outerCornerRadius.toPx(),
                            y = outerCornerRadius.toPx(),
                        )
                        val outerPadding = Offset(
                            x = 1.dp.toPx(),
                            y = 1.dp.toPx(),
                        )
                        val outerSize = Size(
                            width = size.width - outerPadding.x * 2,
                            height = size.height - outerPadding.y * 2,
                        )
                        val innerCornerRadius = CornerRadius(
                            x = 6.dp.toPx(),
                            y = 6.dp.toPx(),
                        )
                        val innerPadding = outerPadding + Offset(
                            x = size.width * 0.15f,
                            y = size.height * 0.15f,
                        )
                        val innerSize = Size(
                            width = size.width - innerPadding.x * 2,
                            height = size.height - innerPadding.y * 2,
                        )
                        val outerDrawStyle = Stroke(
                            width = 2.dp.toPx(),
                        )

                        onDrawBehind {
                            drawRoundRect(
                                color = outerColor,
                                cornerRadius = outerCornerRadius,
                                topLeft = outerPadding,
                                size = outerSize,
                            )
                            drawRoundRect(
                                color = Color.Black,
                                cornerRadius = innerCornerRadius,
                                topLeft = innerPadding,
                                size = innerSize,
                                blendMode = BlendMode.Clear,
                            )
                            drawRoundRect(
                                color = strokeColor,
                                cornerRadius = innerCornerRadius,
                                topLeft = innerPadding,
                                size = innerSize,
                                style = outerDrawStyle,
                            )
                        }
                    }
            )

            BasicText(
                text = "press and hold to cut",
                style = TextStyle(
                    fontFamily = podkovaFamily,
                    color = Color(0xFFcbc4bb),
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = 18.dp,
                    )
            )
        }
    }
}

@Preview
@Composable
private fun StampCutterPreview(

) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .paperBackground()
    ) {
        StampCutter(
            frameSize = StampSize * 1.5f,
            onPressed = {},
            modifier = Modifier
                .requiredWidth(StampSize.width * 2.5f)
                .requiredHeight(StampSize.height * 2.8f)
        )
    }
}
