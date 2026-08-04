package ua.com.radiokot.camerapp.posters.ui

import android.net.Uri
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
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

        override val rect: Rect
            get() {
                val size = Size(
                    width = 5.4f * shape.size.width.value * scale.floatValue,
                    height = 5.4f * shape.size.height.value * scale.floatValue,
                )
                return Rect(
                    offset = center.value - size.center,
                    size = size,
                )
            }
    }

    @Stable
    class Text(
        val text: MutableState<String>,
        center: MutableState<Offset>,
        scale: MutableFloatState,
        rotationDegrees: MutableFloatState,
    ) : UiStampPosterLayer(center, scale, rotationDegrees) {

        var textMeasurer: TextMeasurer? = null

        val rectAndLayout: Pair<Rect, TextLayoutResult>
            get() {
                val textMeasurer = textMeasurer
                    ?: error("textMeasurer with the actual density must be set")

                val textLayout = textMeasurer.measure(
                    text = text.value,
                    style = TextStyle(
                        fontFamily = PodkovaFamily,
                        fontSize = 72.sp * scale.floatValue,
                        textAlign = TextAlign.Center,
                    ),
                    constraints = Constraints(),
                )
                val size = textLayout.size.toSize() / textLayout.layoutInput.density.density

                return Pair(
                    Rect(
                        offset = center.value - size.center,
                        size = size,
                    ),
                    textLayout,
                )
            }

        override val rect: Rect
            get() = rectAndLayout.first
    }
}
