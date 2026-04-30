package ua.com.radiokot.camerapp.stamps.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.launch

fun Modifier.holdToDeleteAction(
    roundedCornerRadius: Dp,
    areTopCornersRounded: Boolean,
    onDelete: () -> Unit,
) = composed {
    val deleteAnimationProgress = remember {
        Animatable(0f)
    }
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    this
        // When pressed, a growing red background is drawn.
        .drawWithCache {
            val roundedCornerRadius = CornerRadius(
                x = roundedCornerRadius.toPx(),
                y = roundedCornerRadius.toPx()
            )
            // The top corner radius matches outer corner radius
            // or shrinks to 0 if the divider is above.
            val topCornerRadius =
                if (areTopCornersRounded || deleteAnimationProgress.value < 0.8f)
                    roundedCornerRadius
                else
                    roundedCornerRadius *
                            (1 - deleteAnimationProgress.value) / 0.2f
            val color = Color(0xFFEAB8B8)
            val halfWidth = size.width / 2f
            val path = Path()
            path.addRoundRect(
                RoundRect(
                    rect = Rect(
                        offset = Offset(
                            x = halfWidth - halfWidth * deleteAnimationProgress.value,
                            y = 0f,
                        ),
                        size = Size(
                            width = size.width * deleteAnimationProgress.value,
                            height = size.height,
                        ),
                    ),
                    topRight = topCornerRadius,
                    topLeft = topCornerRadius,
                    bottomRight = roundedCornerRadius,
                    bottomLeft = roundedCornerRadius,
                )
            )

            onDrawBehind {
                drawPath(
                    path = path,
                    color = color,
                    alpha = deleteAnimationProgress.value * 2f,
                )
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    hapticFeedback.performHapticFeedback(
                        HapticFeedbackType.GestureEnd
                    )

                    val pressingJob = coroutineScope.launch {
                        deleteAnimationProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = 1200,
                                easing = EaseInOutQuad,
                            ),
                        )
                        hapticFeedback.performHapticFeedback(
                            HapticFeedbackType.LongPress
                        )
                        onDelete()
                    }

                    awaitRelease()

                    if (!pressingJob.isCompleted) {
                        pressingJob.cancel()
                        coroutineScope.launch {
                            deleteAnimationProgress.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    stiffness = Spring.StiffnessHigh,
                                ),
                            )
                        }
                    }
                }
            )
        }
}
