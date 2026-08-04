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

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ua.com.radiokot.camerapp.stamps.ui.UiStampShapeA
import ua.com.radiokot.camerapp.ui.AppColors
import ua.com.radiokot.camerapp.ui.DarkAppColors
import ua.com.radiokot.camerapp.ui.LightAppColors
import ua.com.radiokot.camerapp.ui.LocalColors
import ua.com.radiokot.camerapp.util.StableHolder
import ua.com.radiokot.camerapp.util.rotateBy
import kotlin.math.min

@Composable
fun CreateStampPosterScreen(
    modifier: Modifier = Modifier,
    isDark: Boolean,
    layersState: State<ImmutableList<UiStampPosterLayer>>,
) = BoxWithConstraints(
    modifier = modifier,
) {
    val canvasScale = min(
        maxWidth.value / StampPosterWidth,
        maxHeight.value / StampPosterHeight,
    ) * 0.85f
    val realDensity = LocalDensity.current.density
    val canvasDensity = Density(
        density = realDensity * canvasScale,
        fontScale = 1f,
    )
    val canvasShape = RoundedCornerShape(10.dp)

    Box(
        modifier = Modifier
            .border(
                width = 2.dp,
                color = LocalColors.current.componentStroke,
                shape = canvasShape,
            )
            .clip(canvasShape)
            .align(Alignment.TopCenter)
    ) {
        CompositionLocalProvider(
            LocalDensity provides canvasDensity,
        ) {
            StampPosterEditor(
                layersState = layersState,
                colors = if (isDark) DarkAppColors else LightAppColors,
            )
        }
    }
}

@Composable
fun StampPosterEditor(
    modifier: Modifier = Modifier,
    layersState: State<ImmutableList<UiStampPosterLayer>>,
    colors: AppColors,
) {
    var activeLayer: UiStampPosterLayer? by retain { mutableStateOf(null) }
    val density by rememberUpdatedState(LocalDensity.current.density)
    val textMeasurer = rememberTextMeasurer()

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
                                degrees = -layer.rotationDegrees.floatValue,
                                pivot = layer.center.value,
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

                        activeLayer.center.value += pan / density
                        activeLayer.scale.floatValue *= zoom
                        activeLayer.rotationDegrees.floatValue += rotation
                    }
                )
            }
    ) {
        drawStampPoster(
            layers = layersState.value,
            colors = colors,
            textMeasurer = textMeasurer
        )
    }
}

@PreviewLightDark
@Composable
private fun CreateStampPosterScreenPreview() {
    val layersState: State<ImmutableList<UiStampPosterLayer>> = remember {
        mutableStateOf(
            persistentListOf(
                UiStampPosterLayer.Stamp(
                    imageUri = StableHolder(Uri.EMPTY),
                    shape = UiStampShapeA,
                    center = mutableStateOf(
                        Offset(
                            400f,
                            900f,
                        )
                    ),
                    scale = mutableFloatStateOf(1f),
                    rotationDegrees = mutableFloatStateOf(0f),
                ),
                UiStampPosterLayer.Text(
                    text = mutableStateOf("OLEG!"),
                    center = mutableStateOf(
                        Offset(
                            300f,
                            300f,
                        )
                    ),
                    scale = mutableFloatStateOf(1f),
                    rotationDegrees = mutableFloatStateOf(45f),
                ),
                UiStampPosterLayer.Text(
                    text = mutableStateOf("жжот"),
                    center = mutableStateOf(
                        Offset(
                            300f,
                            400f,
                        )
                    ),
                    scale = mutableFloatStateOf(2f),
                    rotationDegrees = mutableFloatStateOf(0f),
                )
            )
        )
    }

    CreateStampPosterScreen(
        layersState = layersState,
        isDark = false,
        modifier = Modifier
            .fillMaxSize()
            .background(LocalColors.current.screenBackground)
    )
}
