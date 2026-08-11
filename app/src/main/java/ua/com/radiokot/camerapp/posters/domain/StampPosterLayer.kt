package ua.com.radiokot.camerapp.posters.domain

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.fastRoundToInt
import ua.com.radiokot.camerapp.stamps.ui.UiStampShape
import ua.com.radiokot.camerapp.ui.PodkovaFamily

@Stable
sealed class StampPosterLayer {

    /**
     * Anchor point of this layer,
     * in full-size poster coordinates.
     */
    var center: Offset by mutableStateOf(StampPosterRect.center)

    /**
     * XY scale from [center].
     */
    var scale: Float by mutableFloatStateOf(1f)

    /**
     * Positive for clockwise rotation around [center].
     */
    var rotationDegrees: Float by mutableFloatStateOf(0f)

    val isOutOfBounds: Boolean
        get() = !rect.overlaps(StampPosterRect)

    /**
     * In full-size poster coordinates, with center at [center].
     */
    abstract val rect: Rect

    @Stable
    class Stamp(
        val imageBitmap: ImageBitmap?,
        val shape: UiStampShape,
    ) : StampPosterLayer() {

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
        fontFamilyResolver: FontFamily.Resolver,
    ) : StampPosterLayer() {

        private val posterTextMeasurer = TextMeasurer(
            defaultDensity = StampPosterDensity,
            defaultFontFamilyResolver = fontFamilyResolver,
            defaultLayoutDirection = LayoutDirection.Ltr,
        )

        var text: String by mutableStateOf(text)

        /**
         * @param drawDensity the actual density at which the poster
         * is currently being drawn, for sharp text.
         */
        fun getTextLayoutToDraw(
            drawDensity: Float,
        ): TextLayoutResult =
            posterTextMeasurer.measure(
                text = text,
                style = TextStyle(
                    fontFamily = PodkovaFamily,
                    fontSize = 72.sp * scale,
                    textAlign = TextAlign.Center,
                ),
                constraints = Constraints(
                    maxWidth = (StampPosterWidth * drawDensity * scale).fastRoundToInt(),
                    maxHeight = (StampPosterHeight * drawDensity * scale).fastRoundToInt(),
                ),
                density = Density(
                    density = drawDensity,
                    fontScale = StampPosterDensity.fontScale,
                ),
            )

        override val rect: Rect by derivedStateOf {
            val textLayout = getTextLayoutToDraw(
                drawDensity = 1f,
            )
            val size = textLayout.size.toSize()

            Rect(
                offset = center - size.center,
                size = size,
            )
        }
    }
}
