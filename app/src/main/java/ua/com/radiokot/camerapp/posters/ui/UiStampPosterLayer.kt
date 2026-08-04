package ua.com.radiokot.camerapp.posters.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import ua.com.radiokot.camerapp.stamps.ui.UiStampShape
import ua.com.radiokot.camerapp.ui.PodkovaFamily

@Stable
sealed class UiStampPosterLayer {

    /**
     * Anchor point of this layer,
     * in full-size poster coordinates.
     */
    var center: Offset by mutableStateOf(
        Offset(
            x = StampPosterWidth / 2f,
            y = StampPosterHeight / 2f,
        )
    )

    /**
     * XY scale from [center].
     */
    var scale: Float by mutableFloatStateOf(1f)

    /**
     * Positive for clockwise rotation around [center].
     */
    var rotationDegrees: Float by mutableFloatStateOf(360f)
    //                                                starts at 360 and kept positive for convenience.

    /**
     * In full-size poster coordinates, with center at [center].
     */
    abstract val rect: Rect

    @Stable
    class Stamp(
        val imageBitmap: ImageBitmap?,
        val shape: UiStampShape,
    ) : UiStampPosterLayer() {

        override val rect: Rect
            get() {
                val size = Size(
                    width = 5.4f * shape.size.width.value * scale,
                    height = 5.4f * shape.size.height.value * scale,
                )
                return Rect(
                    offset = center - size.center,
                    size = size,
                )
            }
    }

    @Stable
    class Text(
        text: String,
    ) : UiStampPosterLayer() {

        var text: String by mutableStateOf(text)
        var textMeasurer: TextMeasurer? = null

        /**
         * [TextLayoutResult] is density-dependant,
         * while [Rect] remains in full-size poster coordinates,
         * with center at [center].
         */
        val rectAndLayout: Pair<Rect, TextLayoutResult>
            get() {
                val textMeasurer = textMeasurer
                    ?: error("textMeasurer with the actual density must be set")

                val textLayout = textMeasurer.measure(
                    text = text,
                    style = TextStyle(
                        fontFamily = PodkovaFamily,
                        fontSize = 72.sp * scale,
                        textAlign = TextAlign.Center,
                    ),
                    constraints = Constraints(),
                )
                val size = textLayout.size.toSize() / textLayout.layoutInput.density.density

                return Pair(
                    Rect(
                        offset = center - size.center,
                        size = size,
                    ),
                    textLayout,
                )
            }

        override val rect: Rect
            get() = rectAndLayout.first
    }
}
