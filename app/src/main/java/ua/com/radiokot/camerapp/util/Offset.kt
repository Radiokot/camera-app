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

package ua.com.radiokot.camerapp.util

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

/**
 * @param degrees positive for clockwise rotation.
 */
fun Offset.rotateBy(
    degrees: Float,
    pivot: Offset = Offset.Zero,
): Offset {
    if (degrees == 0f) {
        return this
    }

    val radians = Math.toRadians(degrees.toDouble())
    val cosAngle = cos(radians).toFloat()
    val sinAngle = sin(radians).toFloat()

    val translatedX = this.x - pivot.x
    val translatedY = this.y - pivot.y

    val rotatedX = translatedX * cosAngle - translatedY * sinAngle
    val rotatedY = translatedX * sinAngle + translatedY * cosAngle

    return Offset(
        x = rotatedX + pivot.x,
        y = rotatedY + pivot.y
    )
}
