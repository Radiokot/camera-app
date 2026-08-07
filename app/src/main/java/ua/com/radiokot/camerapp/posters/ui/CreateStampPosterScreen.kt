/* Copyright 2026 Oleg Koretsky

   This file is part of the Press-Cut,
   a digital postage stamp cutter Android app.

   Press-Cut is free software: you can redistribute it
   and/or modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation, either version 3 of the License,
   or (at your option) any later version.

   Press-Cut is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   See the GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Press-Cut. If not, see <http://www.gnu.org/licenses/>.
*/

package ua.com.radiokot.camerapp.posters.ui

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ua.com.radiokot.camerapp.posters.domain.StampPosterHeight
import ua.com.radiokot.camerapp.posters.domain.StampPosterLayer
import ua.com.radiokot.camerapp.posters.domain.StampPosterWidth
import ua.com.radiokot.camerapp.posters.domain.drawStampPoster
import ua.com.radiokot.camerapp.stamps.ui.UiStampShapeA
import ua.com.radiokot.camerapp.ui.AppTheme
import ua.com.radiokot.camerapp.ui.DarkAppColors
import ua.com.radiokot.camerapp.ui.LeTextButton
import ua.com.radiokot.camerapp.ui.LightAppColors
import ua.com.radiokot.camerapp.ui.LocalColors
import ua.com.radiokot.camerapp.ui.PodkovaFamily
import ua.com.radiokot.camerapp.ui.paperBackground
import ua.com.radiokot.camerapp.util.rotateBy
import kotlin.math.absoluteValue
import kotlin.math.min

@Composable
fun CreateStampPosterScreen(
    modifier: Modifier = Modifier,
    layersState: State<ImmutableList<StampPosterLayer>>,
    isDarkState: State<Boolean>,
    onToggleIsDarkAction: () -> Unit,
    onAddTextAction: () -> Unit,
    onSendAction: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .background(LocalColors.current.screenBackground)
                // IME is handled in the composition.
                .safeGesturesPadding()
                .displayCutoutPadding()
                .padding(24.dp)
        ) {
            CreateStampPosterScreenLayoutContent(
                row = this,
                column = null,
                isDarkState = isDarkState,
                onToggleIsDarkAction = onToggleIsDarkAction,
                layersState = layersState,
                onSendAction = onSendAction,
                onAddTextAction = onAddTextAction,
            )
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .background(LocalColors.current.screenBackground)
                // IME is handled in the composition.
                .safeGesturesPadding()
                .displayCutoutPadding()
                .padding(24.dp)
        ) {
            CreateStampPosterScreenLayoutContent(
                row = null,
                column = this,
                isDarkState = isDarkState,
                onToggleIsDarkAction = onToggleIsDarkAction,
                layersState = layersState,
                onSendAction = onSendAction,
                onAddTextAction = onAddTextAction,
            )
        }
    }
}

@Composable
private fun CreateStampPosterScreenLayoutContent(
    row: RowScope?,
    column: ColumnScope?,
    isDarkState: State<Boolean>,
    onToggleIsDarkAction: () -> Unit,
    layersState: State<ImmutableList<StampPosterLayer>>,
    onSendAction: () -> Unit,
    onAddTextAction: () -> Unit,
) {
    val editorShape = RoundedCornerShape(10.dp)

    BoxWithConstraints(
        modifier = Modifier
            .run {
                if (column != null) {
                    with(column) {
                        weight(1f)
                    }
                } else {
                    with(row!!) {
                        weight(1f)
                    }
                }
            }
            .padding(
                top = 0.dp,
                start = if (row != null) 24.dp else 0.dp,
                end = if (row != null) 24.dp else 0.dp,
                bottom = if (column != null) 24.dp else 0.dp,
            )
    ) {
        val editorScale = min(
            maxWidth.value / StampPosterWidth,
            maxHeight.value / StampPosterHeight,
        )
        val realDensity = LocalDensity.current.density
        val editorDensity = Density(
            density = realDensity * editorScale,
            fontScale = 1f,
        )

        Box(
            modifier = Modifier
                .border(
                    width = 2.dp,
                    color = LocalColors.current.componentStroke,
                    shape = editorShape,
                )
                .clip(editorShape)
                .align(Alignment.Center)
        ) {
            CompositionLocalProvider(
                LocalDensity provides editorDensity,
            ) {
                StampPosterEditor(
                    isDarkState = isDarkState,
                    layersState = layersState,
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .run {
                if (row != null) {
                    this
                        .fillMaxWidth(0.65f)
                        .padding(
                            horizontal = 24.dp,
                        )
                } else {
                    this
                }
            }
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                space = 16.dp,
                alignment = Alignment.CenterHorizontally,
            ),
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth()
        ) {
            DarkLightButton(
                isDarkState = isDarkState,
                onToggleIsDarkAction = onToggleIsDarkAction,
            )

            AddTextButton(
                onAddTextAction = onAddTextAction,
            )
        }

        Spacer(
            modifier = Modifier
                .height(24.dp)
        )

        LeTextButton(
            text = "Send",
            onClick = onSendAction,
        )
    }
}

@Composable
private fun posterActionButtonTextStyle() =
    TextStyle(
        fontFamily = PodkovaFamily,
        color = LocalColors.current.textPrimary,
        fontSize = 22.sp,
    )

@Composable
private fun PosterActionButton(
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) = Box(
    contentAlignment = Alignment.Center,
    content = content,
    modifier = Modifier
        .size(48.dp)
        .clip(
            shape = RoundedCornerShape(8.dp),
        )
        .clickable(
            onClick = onClick,
        )
        .border(
            width = 2.dp,
            color = LocalColors.current.componentStroke,
            shape = RoundedCornerShape(8.dp),
        )
)

@Composable
private fun DarkLightButton(
    isDarkState: State<Boolean>,
    onToggleIsDarkAction: () -> Unit,
) = PosterActionButton(
    onClick = onToggleIsDarkAction,
) {
    CompositionLocalProvider(
        LocalColors provides if (isDarkState.value) DarkAppColors else LightAppColors,
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .paperBackground(
                    drawBackgroundColor = true,
                    gridSize = 10.dp,
                )
        )

        BasicText(
            text = if (isDarkState.value) "D" else "L",
            style = posterActionButtonTextStyle(),
        )
    }
}

@Composable
private fun AddTextButton(
    onAddTextAction: () -> Unit,
) = PosterActionButton(
    onClick = onAddTextAction,
) {
    BasicText(
        text = "Aa",
        style = posterActionButtonTextStyle(),
    )
}

@Composable
private fun StampPosterEditor(
    modifier: Modifier = Modifier,
    isDarkState: State<Boolean>,
    layersState: State<ImmutableList<StampPosterLayer>>,
) {
    var activeLayer: StampPosterLayer? by retain { mutableStateOf(null) }
    val density by rememberUpdatedState(LocalDensity.current.density)
    val hapticFeedback = LocalHapticFeedback.current

    Canvas(
        modifier = modifier
            .requiredSize(
                width = StampPosterWidth.dp,
                height = StampPosterHeight.dp,
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()

                    for (layer in layersState.value.asReversed()) {
                        val relativePosition =
                            (down.position / density).rotateBy(
                                degrees = -layer.rotationDegrees,
                                pivot = layer.center,
                            )

                        if (relativePosition in layer.rect) {
                            activeLayer = layer
                            down.consume()
                            return@awaitEachGesture
                        }
                    }
                    activeLayer = null
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures(
                    onGesture = onGesture@{ _, pan, zoom, rotation ->
                        val activeLayer = activeLayer
                            ?: return@onGesture

                        if (pan != Offset.Zero) {
                            var newCenterX = activeLayer.center.x + pan.x / density
                            var newCenterY = activeLayer.center.y + pan.y / density

                            val posterCenterDx = newCenterX - StampPosterWidth / 2f
                            val posterCenterDy = newCenterY - StampPosterHeight / 2f

                            // Snap to center lines.
                            if (posterCenterDx.absoluteValue < 4f) {
                                newCenterX -= posterCenterDx
                                if (newCenterX != activeLayer.center.x) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                }
                            }
                            if (posterCenterDy.absoluteValue < 4f) {
                                newCenterY -= posterCenterDy
                                if (newCenterY != activeLayer.center.y) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                }
                            }

                            activeLayer.center = Offset(
                                x = newCenterX,
                                y = newCenterY,
                            )
                        }

                        activeLayer.scale *= zoom

                        if (rotation != 0f) {
                            var newRotation =
                                360f + (activeLayer.rotationDegrees + rotation) % 360f

                            // Snap to quarters.
                            val rem90 = newRotation % 90f
                            if (rem90 > 89.5f) {
                                newRotation += (90f - rem90)
                                if (newRotation != activeLayer.rotationDegrees) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                }
                            } else if (rem90 < 0.5f) {
                                newRotation -= rem90
                                if (newRotation != activeLayer.rotationDegrees) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                }
                            }

                            activeLayer.rotationDegrees = newRotation
                        }
                    }
                )
            }
    ) {
        drawStampPoster(
            layers = layersState.value,
            isDark = isDarkState.value,
        )
    }
}

@PreviewLightDark
@Composable
private fun CreateStampPosterScreenPreview() {
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val layersState: State<ImmutableList<StampPosterLayer>> = remember {
        mutableStateOf(
            persistentListOf(
                StampPosterLayer.Stamp(
                    imageBitmap = null,
                    shape = UiStampShapeA,
                ).apply {
                    center = Offset(
                        400f,
                        900f,
                    )
                },
                StampPosterLayer.Text(
                    text = "OLEG!",
                    fontFamilyResolver = fontFamilyResolver,
                ).apply {
                    center = Offset(
                        300f,
                        300f,
                    )
                    rotationDegrees = 45f
                },
                StampPosterLayer.Text(
                    text = "жжот",
                    fontFamilyResolver = fontFamilyResolver,
                ).apply {
                    center = Offset(
                        300f,
                        400f,
                    )
                    scale = 2f
                }
            )
        )
    }

    AppTheme {
        CreateStampPosterScreen(
            layersState = layersState,
            onSendAction = {},
            isDarkState = false.let(::mutableStateOf),
            onToggleIsDarkAction = {},
            onAddTextAction = {},
            modifier = Modifier
                .fillMaxSize()
        )
    }
}
