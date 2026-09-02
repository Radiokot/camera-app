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

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import java.util.Objects

fun DrawScope.drawRectangleShadow(
    color: Color,
    radiusDp: Float,
    size: Size = this.size,
    topLeft: Offset = Offset.Zero,
) {
    drawRectangleShadowOnCanvas(
        drawContext.canvas.nativeCanvas,
        color.toArgb(),
        radiusDp * density,
        topLeft.x,
        topLeft.y,
        topLeft.x + size.width,
        topLeft.y + size.height,
    )
}

private val drawRectangleShadowOnCanvas: (
    canvas: Canvas,
    color: Int,
    radiusPx: Float,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
) -> Unit =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        ::drawHardwareAcceleratedRectangleShadow
    else
        ::drawApproximatedRectangleShadow

private val hardwareAcceleratedShadowPaints =
    mutableMapOf<Int, Paint>()

private fun drawHardwareAcceleratedRectangleShadow(
    canvas: Canvas,
    color: Int,
    radiusPx: Float,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
) {
    val paint = hardwareAcceleratedShadowPaints.getOrPut(Objects.hash(color, radiusPx)) {
        Paint().apply {
            setColor(color)
            style = Paint.Style.FILL
            maskFilter = BlurMaskFilter(radiusPx, BlurMaskFilter.Blur.NORMAL)
        }
    }
    canvas.drawRect(
        left,
        top,
        right,
        bottom,
        paint
    )
}

fun drawApproximatedRectangleShadow(
    canvas: Canvas,
    color: Int,
    radiusPx: Float,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
) {
    val layerCount =
        ((right - left) / 25f)
            .fastRoundToInt()
            .fastCoerceIn(8, 16)

    val colorAlpha = android.graphics.Color.alpha(color)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // All the magic multipliers are here to match
    // the appearance of the modern shadow.

    val radiusPx = radiusPx * 1.4f
    var layerCornerRadius = radiusPx * 1.5f
    var layerLeft = left - radiusPx
    var layerTop = top - radiusPx
    var layerRight = right + radiusPx
    var layerBottom = bottom + radiusPx

    val sizeStep = radiusPx / layerCount
    val cornerRadiusStep = sizeStep * 0.5f

    repeat(layerCount) { step ->
        val layerAlpha =
            colorAlpha * 1.1f * (step + 1f) / (layerCount * layerCount)
        paint.color =
            (layerAlpha.toInt() shl 24) or (color and 0x00FFFFFF)

        canvas.drawRoundRect(
            layerLeft,
            layerTop,
            layerRight,
            layerBottom,
            layerCornerRadius,
            layerCornerRadius,
            paint
        )

        layerLeft += sizeStep
        layerTop += sizeStep
        layerRight -= sizeStep
        layerBottom -= sizeStep
        layerCornerRadius -= cornerRadiusStep
    }
}
