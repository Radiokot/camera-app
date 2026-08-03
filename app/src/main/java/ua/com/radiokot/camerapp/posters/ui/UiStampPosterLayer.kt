package ua.com.radiokot.camerapp.posters.ui

import android.net.Uri
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import ua.com.radiokot.camerapp.stamps.ui.UiStampShape
import ua.com.radiokot.camerapp.ui.PodkovaFamily
import ua.com.radiokot.camerapp.util.StableHolder

@Stable
sealed class UiStampPosterLayer(
    val center: MutableState<Offset>,
    val scale: MutableFloatState,
    val rotationDegrees: MutableFloatState,
) {
    abstract val rect: Rect

    @Stable
    class Stamp(
        val imageUri: StableHolder<Uri>,
        val shape: UiStampShape,
        center: MutableState<Offset>,
        scale: MutableFloatState,
        rotationDegrees: MutableFloatState,
    ) : UiStampPosterLayer(center, scale, rotationDegrees) {

        override val rect: Rect by derivedStateOf {
            val shapeSize = shape.size * 5.4f
            val size = Size(
                width = shapeSize.width.value * scale.floatValue,
                height = shapeSize.height.value * scale.floatValue,
            )
            Rect(
                offset = center.value - size.center,
                size = size,
            )
        }
    }

    @Stable
    class Text(
        val text: String,
        private val textMeasurer: TextMeasurer,
        center: MutableState<Offset>,
        scale: MutableFloatState,
        rotationDegrees: MutableFloatState,
    ) : UiStampPosterLayer(center, scale, rotationDegrees) {

        val rectAndLayout: Pair<Rect, TextLayoutResult> by derivedStateOf {
            val textLayout = textMeasurer.measure(
                text = text,
                style = TextStyle(
                    fontFamily = PodkovaFamily,
                    fontSize = 72.sp * scale.floatValue,
                    textAlign = TextAlign.Center,
                ),
                constraints = Constraints(),
            )
            val size = textLayout.size.toSize()
            Pair(
                Rect(
                    offset = center.value - size.center,
                    size = size,
                ),
                textLayout,
            )
        }

        override val rect: Rect by derivedStateOf {
            rectAndLayout.first
        }
    }
}
