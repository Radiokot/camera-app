package ua.com.radiokot.camerapp.ui

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FloatExponentialDecaySpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.generateDecayAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.snapFlingBehavior
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity

@Composable
fun rememberSnapFlingBehavior(
    lazyListState: LazyListState,
    snapPosition: SnapPosition = SnapPosition.Center,
    snapAnimationSpec: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
    frictionMultiplier: Float = 1f,
): FlingBehavior {
    val snapLayoutInfoProvider = remember(lazyListState) {
        SnapLayoutInfoProvider(lazyListState, snapPosition)
    }
    val density = LocalDensity.current
    val decayAnimationSpec = remember(frictionMultiplier) {
        FloatExponentialDecaySpec(
            frictionMultiplier = frictionMultiplier,
        ).generateDecayAnimationSpec<Float>()
    }
    return remember(snapLayoutInfoProvider, decayAnimationSpec, density) {
        snapFlingBehavior(
            snapLayoutInfoProvider = snapLayoutInfoProvider,
            decayAnimationSpec = decayAnimationSpec,
            snapAnimationSpec = snapAnimationSpec,
        )
    }
}
