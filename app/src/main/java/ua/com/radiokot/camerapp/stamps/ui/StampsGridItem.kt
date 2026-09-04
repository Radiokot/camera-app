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

package ua.com.radiokot.camerapp.stamps.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import kotlinx.collections.immutable.ImmutableList
import ua.com.radiokot.camerapp.stamps.domain.Stamp
import ua.com.radiokot.camerapp.ui.StampImage
import ua.com.radiokot.camerapp.ui.StampImageUse
import kotlin.math.absoluteValue

@Immutable
data class StampsGridItem(
    val imageUri: Uri,
    val shape: UiStampShape,
    val isSelected: Boolean,
    val key: String,
) {
    constructor(
        stamp: Stamp,
        selectedStampIds: Set<String>,
    ) : this(
        imageUri = stamp.imageUri.toUri(),
        shape = UiStampShape.fromShape(stamp.shape),
        isSelected = stamp.id in selectedStampIds,
        key = stamp.id,
    )
}

val StampGridItemRotationAngles = floatArrayOf(4f, 3f, 2f, -2f, -3f, -4f)

fun LazyGridScope.stampItems(
    items: ImmutableList<StampsGridItem>,
    onClicked: (StampsGridItem) -> Unit,
    onLongClicked: (StampsGridItem) -> Unit,
    selectionAnimationSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium,
    ),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) = items(
    items = items,
    key = StampsGridItem::key,
) { stamp ->
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(StampContainerBaseSize.height * 1.2f)
            .animateItem()
    ) {
        val rotation =
            (StampGridItemRotationAngles[stamp.key.hashCode().absoluteValue % StampGridItemRotationAngles.size])
        val selectionAnimationProgressState = animateFloatAsState(
            targetValue =
                if (stamp.isSelected)
                    1f
                else
                    0f,
            animationSpec = selectionAnimationSpec,
        )

        StampImage(
            uri = stamp.imageUri,
            shape = stamp.shape,
            use = StampImageUse.Grid,
            shadowRadiusDp = 4f,
            rotationDegrees = rotation,
            scale = { 1f - 0.1f * selectionAnimationProgressState.value },
            modifier = Modifier
                .size(stamp.shape.size * stamp.shape.fitContainerSizeScale)
                .run {
                    if (sharedTransitionScope == null || animatedVisibilityScope == null) {
                        return@run this
                    }

                    with(sharedTransitionScope) {
                        sharedElement(
                            sharedContentState = rememberSharedContentState(stamp.key),
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                    }
                }
                .selectionEnvelope(
                    animationProgressState = selectionAnimationProgressState,
                    heightFraction = when (stamp.shape) {
                        UiStampShapeOneStampSquare -> 0.7f
                        UiStampShapeOneStampLandscape -> 0.8f
                        else -> 0.6f
                    },
                )
                .combinedClickable(
                    indication = null,
                    interactionSource = null,
                    onClick = {
                        onClicked(stamp)
                    },
                    onLongClick = {
                        onLongClicked(stamp)
                    },
                )
        )
    }
}
