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
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ua.com.radiokot.camerapp.stamps.ui.UiStampShapeA
import ua.com.radiokot.camerapp.ui.AppColors
import ua.com.radiokot.camerapp.ui.DarkAppColors
import ua.com.radiokot.camerapp.ui.LightAppColors
import ua.com.radiokot.camerapp.ui.LocalColors
import ua.com.radiokot.camerapp.ui.drawPaperBackground
import ua.com.radiokot.camerapp.util.StableHolder

@Composable
fun CreateStampPosterScreen(
    modifier: Modifier = Modifier,
    isDark: Boolean,
    layersState: State<ImmutableList<UiStampPosterLayer>>,
) = Box(
    modifier = modifier,
) {
    val canvasDensity = Density(
        density = 1f,
        fontScale = 1f,
    )
    CompositionLocalProvider(
        LocalDensity provides canvasDensity,
    ) {
        val canvasShape = RoundedCornerShape(10.dp)

        StampPosterCanvas(
            layersState = layersState,
            colors = if (isDark) DarkAppColors else LightAppColors,
            modifier = Modifier
                .border(
                    width = 2.dp,
                    color = LocalColors.current.componentStroke,
                    shape = canvasShape,
                )
                .clip(canvasShape)
        )
    }
}

@Composable
fun StampPosterCanvas(
    modifier: Modifier = Modifier,
    layersState: State<ImmutableList<UiStampPosterLayer>>,
    colors: AppColors,
) {
    var activeLayer: UiStampPosterLayer? by retain { mutableStateOf(null) }

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
                        if (down.position in layer.rect) {
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

                        activeLayer.center.value += pan
                        activeLayer.scale.floatValue *= zoom
                        activeLayer.rotationDegrees.floatValue += rotation
                    }
                )
            }
    ) {
        drawPaperBackground(
            lineColor = colors.paperBackgroundLine,
            backgroundColor = colors.componentBackground,
            gridSizePx = 57,
            gridThicknessPx = 3.6f,
            verticalOffsetPx = 0,
        )

        for (layer in layersState.value) {
            when (layer) {
                is UiStampPosterLayer.Stamp -> {
                    val rect = layer.rect
                    drawRect(
                        color = Color.Magenta,
                        topLeft = rect.topLeft,
                        size = rect.size,
                    )
                }

                is UiStampPosterLayer.Text -> {
                    val (rect, textLayout) = layer.rectAndLayout
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = rect.topLeft,
                        color = colors.textPrimary,
                    )
                }
            }

            drawCircle(
                color = Color.Cyan,
                center = layer.center.value,
                radius = 12f,
            )
        }
    }
}

@Composable
private fun StampPosterStampLayer(
    stampLayer: UiStampPosterLayer.Stamp,
) = Spacer(
    modifier = Modifier
        .offset()
        .layout { m, _ ->
            val center = stampLayer.center.value
            val scale = stampLayer.scale.floatValue
            val shapeSize = stampLayer.shape.size

            val widthF = shapeSize.width.value * scale
            val heightF = shapeSize.height.value * scale

            val self = m.measure(
                Constraints.fixed(
                    width = widthF.fastRoundToInt(),
                    height = heightF.fastRoundToInt(),
                )
            )

            layout(self.width, self.height) {
                self.place(
                    x = (center.x - widthF / 2).fastRoundToInt(),
                    y = (center.y - heightF / 2).fastRoundToInt(),
                )
            }
        }
        .background(Color.Magenta)
        .graphicsLayer {
            rotationZ = stampLayer.rotationDegrees.floatValue
        }
        .draggable2D(
            state = rememberDraggable2DState { delta ->
                stampLayer.center.value += delta
            },
        )
)

@PreviewLightDark
@Composable
private fun CreateStampPosterScreenPreview() {
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val textMeasurer = remember {
        TextMeasurer(
            defaultFontFamilyResolver = fontFamilyResolver,
            defaultDensity = Density(1f, 1f),
            defaultLayoutDirection = layoutDirection,
        )
    }
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
                    text = "OLEG!",
                    center = mutableStateOf(
                        Offset(
                            300f,
                            300f,
                        )
                    ),
                    scale = mutableFloatStateOf(1f),
                    rotationDegrees = mutableFloatStateOf(0f),
                    textMeasurer = textMeasurer,
                ),
                UiStampPosterLayer.Text(
                    text = "жжот",
                    center = mutableStateOf(
                        Offset(
                            300f,
                            400f,
                        )
                    ),
                    scale = mutableFloatStateOf(2f),
                    rotationDegrees = mutableFloatStateOf(0f),
                    textMeasurer = textMeasurer,
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
