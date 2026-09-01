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
import android.graphics.Paint
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntSize
import com.skydoves.landscapist.core.ImageRequest
import com.skydoves.landscapist.core.cache.CacheKey
import com.skydoves.landscapist.core.model.ImageResult
import com.skydoves.landscapist.image.LocalLandscapist
import ua.com.radiokot.camerapp.util.StableHolder
import ua.com.radiokot.camerapp.util.memoryCache

@Composable
fun StampImage(
    modifier: Modifier = Modifier,
    uri: StableHolder<Uri>,
    decodeSize: IntSize,
    shadowRadiusDp: Float,
    rotationDegrees: Float = 0f,
    scale: (() -> Float)? = null,
) {
    val uri = uri.value
    val landscapist = LocalLandscapist.current

    var drawColor: Color? by remember(uri) {
        mutableStateOf(
            if (uri == Uri.EMPTY)
                Color.Yellow
            else
                null
        )
    }
    var drawBitmap: ImageBitmap? by remember {
        if (uri == Uri.EMPTY) {
            return@remember mutableStateOf(null)
        }

        checkNotNull(landscapist) {
            "Missing local Landscapist"
        }

        @Suppress("ReplaceGetOrSet")
        val cachedBitmap =
            landscapist
                .memoryCache
                .get(
                    CacheKey.create(
                        model = uri,
                        width = decodeSize.width,
                        height = decodeSize.height,
                    )
                )
                ?.data as? Bitmap

        mutableStateOf(cachedBitmap?.asImageBitmap())
    }

    val density = LocalDensity.current.density
    val shadowColor = LocalColors.current.stampShadow
    val shadowPaint = remember(density, shadowColor) {
        Paint().apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.TRANSPARENT
            setShadowLayer(
                shadowRadiusDp * density,
                0f,
                0f,
                shadowColor.toArgb()
            )
        }
    }

    LaunchedEffect(drawBitmap, uri, decodeSize) {
        if (uri == Uri.EMPTY || drawBitmap != null) {
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
