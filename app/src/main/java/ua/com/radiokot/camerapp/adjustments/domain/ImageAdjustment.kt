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

@file:Suppress("NOTHING_TO_INLINE")

package ua.com.radiokot.camerapp.adjustments.domain

import androidx.annotation.FloatRange
import androidx.compose.ui.util.fastCoerceIn
import kotlin.math.abs

class ImageAdjustment(
    @get:FloatRange(from = -1.0, to = 1.0)
    val value: Float,
    val kind: Kind,
) {
    // Weird but fast!
    // Doing it with classes instead cuts FPS in half.
    inline fun applyToImage(pixels: IntArray) {
        if (value == 0f) {
            return
        }

        val rgb = IntArray(3)
        val warmFilter = intArrayOf(237, 138, 0)
        val processed = IntArray(3)

        for (pixelIndex in pixels.indices) {
            val pixel = pixels[pixelIndex]

            val alpha = pixel shr 24
            if (alpha == 0) {
                continue
            }

            rgb[0] = (pixel shr 16) and 0xFF
            rgb[1] = (pixel shr 8) and 0xFF
            rgb[2] = pixel and 0xFF

            when (kind) {
                Kind.Brightness -> {
                    rgb[0] = (rgb[0] * (1 + value)).toInt().fastCoerceIn(0, 255)
                    rgb[1] = (rgb[1] * (1 + value)).toInt().fastCoerceIn(0, 255)
                    rgb[2] = (rgb[2] * (1 + value)).toInt().fastCoerceIn(0, 255)
                }

                Kind.Contrast -> {
                    val contrast = 1 + value
                    rgb[0] = (((rgb[0] - 128) * contrast) + 128).toInt().fastCoerceIn(0, 255)
                    rgb[1] = (((rgb[1] - 128) * contrast) + 128).toInt().fastCoerceIn(0, 255)
                    rgb[2] = (((rgb[2] - 128) * contrast) + 128).toInt().fastCoerceIn(0, 255)
                }

                Kind.Glitch -> {
                    val intensity = abs(value)

                    // Derive a deterministic "glitch key" from the pixel's own values.
                    val seed = (rgb[0] * 0x1F + rgb[1] * 0x3D + rgb[2] * 0x7) and 0xFF
                    val xorMask = (intensity * 255f).toInt()

                    rgb[0] = (rgb[0] xor (seed and xorMask)).fastCoerceIn(0, 255)
                    rgb[1] = (rgb[1] xor ((seed shr 1) and xorMask)).fastCoerceIn(0, 255)
                    rgb[2] = (rgb[2] xor ((seed shr 2) and xorMask)).fastCoerceIn(0, 255)
                }

                Kind.Temperature -> {
                    for (i in rgb.indices) {
                        val base = rgb[i]
                        val blend = warmFilter[i]

                        processed[i] =
                            if (base < 128)
                                2 * base * blend / 255
                            else
                                255 - 2 * (255 - base) * (255 - blend) / 255
                    }

                    if (value >= 0f) {
                        val strength = (value * 256f + 0.5f).toInt()
                        val inverse = 256 - strength

                        for (i in rgb.indices) {
                            val v = (rgb[i] * inverse + processed[i] * strength + 128) shr 8
                            rgb[i] = v.fastCoerceIn(0, 255)
                        }
                    } else {
                        val coldStrength = -value

                        val scaleR = ((1f - 0.3f * coldStrength) * 256f + 0.5f).toInt()
                        val scaleG = ((1f - 0.1f * coldStrength) * 256f + 0.5f).toInt()
                        val scaleB = (0.6f * coldStrength * 256f + 0.5f).toInt()

                        rgb[0] =
                            (rgb[0] * scaleR + 128 shr 8)
                                .fastCoerceIn(0, 255)
                        rgb[1] =
                            (rgb[1] * scaleG + 128 shr 8)
                                .fastCoerceIn(0, 255)
                        rgb[2] =
                            ((rgb[2] * 256 + (255 - rgb[2]) * scaleB + 128) shr 8)
                                .fastCoerceIn(0, 255)
                    }
                }

                Kind.Vibrance -> {
                    val (r, g, b) = rgb
                    val max = maxOf(r, g, b)
                    val min = minOf(r, g, b)
                    val chroma = (max - min) / 255f
                    val luma = (r * 0.299f + g * 0.587f + b * 0.114f).toInt()

                    // Muted pixels get a strong boost, vivid pixels get little/none.
                    val multiplier = 1f + value * (1f - chroma)

                    rgb[0] = (luma + (r - luma) * multiplier).toInt().fastCoerceIn(0, 255)
                    rgb[1] = (luma + (g - luma) * multiplier).toInt().fastCoerceIn(0, 255)
                    rgb[2] = (luma + (b - luma) * multiplier).toInt().fastCoerceIn(0, 255)
                }
            }

            pixels[pixelIndex] =
                (alpha shl 24) or
                        (rgb[0] shl 16) or
                        (rgb[1] shl 8) or
                        rgb[2]
        }
    }

    enum class Kind {
        Brightness,
        Contrast,
        Vibrance,
        Temperature,
        Glitch,
        ;
    }
}
