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

package ua.com.radiokot.camerapp.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rotate
import androidx.compose.ui.graphics.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntSize
import com.skydoves.landscapist.core.ImageRequest
import com.skydoves.landscapist.core.cache.CacheKey
import com.skydoves.landscapist.core.model.ImageResult
import ua.com.radiokot.camerapp.stamps.ui.UiStampShape
import ua.com.radiokot.camerapp.util.LocalLandscapist
import ua.com.radiokot.camerapp.util.StableHolder
import ua.com.radiokot.camerapp.util.memoryCache

@Composable
fun StampImage(
    modifier: Modifier = Modifier,
    uri: StableHolder<Uri>,
    shape: UiStampShape,
    use: StampImageUse,
    shadowRadiusDp: Float,
    rotationDegrees: Float = 0f,
    scale: (() -> Float)? = null,
) {
    val uri = uri.value
    val density = LocalDensity.current.density
    val decodeSize = use.getImageDecodeSize(
        shape = shape,
        density = density,
    )
    val landscapist = LocalLandscapist.current


    // If there's the exact bitmap in the cache – render it from the first frame,
    // do not start async loading.
    //
    // If there's instead a bitmap of suitable alternative size, like grid size for standalone use,
    // render it from the first frame and start async loading.
    //
    // Otherwise, just start the async loading.

    val exactCachedBitmap: ImageBitmap? = remember(decodeSize, uri) {
        if (uri == Uri.EMPTY) {
            return@remember null
        }

        checkNotNull(landscapist) {
            "Missing local Landscapist"
        }

        @Suppress("ReplaceGetOrSet")
        val cachedBitmap =
            landscapist
                .memoryCache
                .get(
                    CacheKey(
                        url = uri.toString(),
                        width = decodeSize.width,
                        height = decodeSize.height,
                    )
                )
                ?.data as? Bitmap

        cachedBitmap?.asImageBitmap()
    }
    val alternativeSizeCachedBitmap: ImageBitmap? = remember(uri, use, density) {
        if (uri == Uri.EMPTY || exactCachedBitmap != null) {
            return@remember null
        }

        val suitableAlternativeDecodeSize: IntSize? = when (use) {
            StampImageUse.Standalone ->
                StampImageUse.Grid.getImageDecodeSize(
                    shape = shape,
                    density = density,
                )

            StampImageUse.Grid -> null
        }

        if (suitableAlternativeDecodeSize == null) {
            return@remember null
        }

        checkNotNull(landscapist) {
            "Missing local Landscapist"
        }

        @Suppress("ReplaceGetOrSet")
        val cachedBitmap =
            landscapist
                .memoryCache
                .get(
                    CacheKey(
                        url = uri.toString(),
                        width = suitableAlternativeDecodeSize.width,
                        height = suitableAlternativeDecodeSize.height,
                    )
                )
                ?.data as? Bitmap

        cachedBitmap?.asImageBitmap()
    }
    var drawBitmap: ImageBitmap? by remember {
        mutableStateOf(exactCachedBitmap ?: alternativeSizeCachedBitmap)
    }
    var drawColor: Color? by remember(uri) {
        mutableStateOf(
            if (uri == Uri.EMPTY)
                Color.Yellow
            else
                null
        )
    }

    val shadowPaint = getStampShadowPaint(
        color = LocalColors.current.stampShadow,
        radiusPx = shadowRadiusDp * density,
    )

    LaunchedEffect(exactCachedBitmap, uri, decodeSize) {
        if (uri == Uri.EMPTY || exactCachedBitmap != null) {
            return@LaunchedEffect
        }

        checkNotNull(landscapist) {
            "Missing local Landscapist"
        }

        landscapist
            .load(
                ImageRequest.builder()
                    .size(decodeSize.width, decodeSize.height)
                    .model(uri)
                    .build()
            )
            .collect { result ->
                when (result) {
                    is ImageResult.Failure -> {
                        drawColor = Color.Red
                    }

                    is ImageResult.Success -> {
                        val bitmap = (result.data as? Bitmap)?.asImageBitmap()
                        if (bitmap != null) {
                            bitmap.prepareToDraw()
                            drawBitmap = bitmap
                        } else {
                            drawColor = Color.Red
                        }
                    }

                    ImageResult.Loading -> {
                        // Do nothing.
                    }
                }
            }
    }

    Canvas(
        modifier = modifier,
        contentDescription = "Stamp",
    ) {
        val drawColor = drawColor
        val drawBitmap = drawBitmap
        val scale = scale?.invoke() ?: 1f

        drawContext.canvas.rotate(
            degrees = rotationDegrees,
            pivotX = center.x,
            pivotY = center.y,
        )
        drawContext.canvas.scale(
            sx = scale,
            pivotX = center.x,
            pivotY = center.y,
        )

        drawContext.canvas.nativeCanvas.drawRect(
            0f,
            0f,
            size.width,
            size.height,
            shadowPaint,
        )

        if (drawBitmap != null) {
            drawImage(
                image = drawBitmap,
                dstSize = size.toIntSize(),
            )
        } else if (drawColor != null) {
            drawRect(drawColor)
        }

        drawContext.canvas.rotate(
            degrees = -rotationDegrees,
            pivotX = center.x,
            pivotY = center.y,
        )
        drawContext.canvas.scale(
            sx = 1f / density,
            pivotX = center.x,
            pivotY = center.y,
        )
    }
}

enum class StampImageUse {
    Grid,
    Standalone,
    ;

    fun getImageDecodeSize(
        shape: UiStampShape,
        density: Float,
    ): IntSize = when (this) {

        Grid -> IntSize(
            width = (shape.size.width.value * density).toInt(),
            height = (shape.size.height.value * density).toInt(),
        )

        Standalone -> IntSize(
            width = (shape.size.width.value * 2f * density).toInt(),
            height = (shape.size.height.value * 2f * density).toInt(),
        )
    }
}
