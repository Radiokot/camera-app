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

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun Modifier.optionalSharedElement(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    contentKey: String,
    zIndexInOverlay: Float = 0f,
): Modifier =
    if (sharedTransitionScope == null || animatedVisibilityScope == null)
        this
    else
        this.then(with(sharedTransitionScope) {
            val sharedContentState = rememberSharedContentState(contentKey)
            remember(contentKey, zIndexInOverlay) {
                Modifier.sharedElement(
                    sharedContentState = sharedContentState,
                    animatedVisibilityScope = animatedVisibilityScope,
                    zIndexInOverlay = zIndexInOverlay,
                )
            }
        })

@Composable
fun Modifier.optionalSharedBounds(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    contentKey: String,
    zIndexInOverlay: Float = 0f,
    exit: ExitTransition = fadeOut(),
): Modifier =
    if (sharedTransitionScope == null || animatedVisibilityScope == null)
        this
    else
        this.then(with(sharedTransitionScope) {
            val sharedContentState = rememberSharedContentState(contentKey)
            remember(contentKey, zIndexInOverlay, exit) {
                Modifier.sharedBounds(
                    sharedContentState = sharedContentState,
                    animatedVisibilityScope = animatedVisibilityScope,
                    zIndexInOverlay = zIndexInOverlay,
                    exit = exit,
                )
            }
        })
