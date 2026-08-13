package ua.com.radiokot.camerapp.posters.domain

import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.fastRoundToInt
import ua.com.radiokot.camerapp.stamps.ui.UiStampShape
import ua.com.radiokot.camerapp.ui.AppColors
import ua.com.radiokot.camerapp.ui.DarkAppColors
import ua.com.radiokot.camerapp.ui.LightAppColors
import ua.com.radiokot.camerapp.ui.PodkovaFamily
import kotlin.math.abs
import kotlin.math.min

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

        override val rect: Rect by derivedStateOf {
            val size = Size(
                width = 5.4f * shape.size.width.value * scale,
                height = 5.4f * shape.size.height.value * scale,
            )
            Rect(
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
        var appearance: Appearance by mutableStateOf(Appearance())

        override val rect: Rect by derivedStateOf {
            val size = textLayout.size.toSize()

            Rect(
                offset = center - size.center,
                size = size,
            )
        }

        val textLayout: TextLayoutResult by derivedStateOf {
            posterTextMeasurer.measure(
                // Without `this` it reads the constructor param.
                text = this.text,
                style = TextStyle(
                    fontFamily = PodkovaFamily,
                    fontSize = 72.sp * scale,
                    textAlign = TextAlign.Center,
                ),
                constraints = Constraints(
                    maxWidth = (StampPosterWidth * scale).fastRoundToInt(),
                    maxHeight = (StampPosterHeight * scale).fastRoundToInt(),
                ),
            )
        }

        val backgroundPath: Path by derivedStateOf {
            // Each line gets its rectangle with padding,
            // then overlapped rectangles are joined into a path
            // like it is Advent of Code. When 2D maze?

            val paddingVertical = 15f * scale
            val paddingHorizontal = 20f * scale
            val textLines = Array(textLayout.multiParagraph.lineCount) { lineIndex ->
                Rect(
                    topLeft = Offset(
                        x = textLayout.getLineLeft(lineIndex) - paddingHorizontal,
                        y = textLayout.getLineTop(lineIndex) - paddingVertical,
                    ),
                    bottomRight = Offset(
                        x = textLayout.getLineRight(lineIndex) + paddingHorizontal,
                        y = textLayout.getLineBottom(lineIndex) + paddingVertical,
                    ),
                )
            }

            // If adjacent lines differ only slightly in width,
            // make them the same width. Otherwise, it doesn't look pretty.
            for (i in (1 until textLines.size)) {
                val rectAbove = textLines[i - 1]
                val rect = textLines[i]
                if (abs(rect.width - rectAbove.width) < 2 * paddingHorizontal) {
                    val betterLeft = min(rect.left, rectAbove.left)
                    val betterRight = maxOf(rect.right, rectAbove.right)

                    textLines[i - 1] = Rect(
                        left = betterLeft,
                        top = rectAbove.top,
                        right = betterRight,
                        bottom = rectAbove.bottom,
                    )
                    textLines[i] = Rect(
                        left = betterLeft,
                        top = rect.top,
                        right = betterRight,
                        bottom = rect.bottom,
                    )
                }
            }

            Path().apply {
                // Top down along left edge.
                for (i in textLines.indices) {
                    val rectAbove = textLines.getOrNull(i - 1)
                    val rect = textLines[i]
                    val rectBelow = textLines.getOrNull(i + 1)

                    if (i != 0) {
                        lineTo(
                            x = rect.left,
                            y =
                                if (rectAbove == null || rectAbove.left > rect.left)
                                    rect.top
                                else
                                    rectAbove.bottom,
                        )
                    } else {
                        moveTo(
                            x = rect.left,
                            y = rect.top,
                        )

                    }
                    lineTo(
                        x = rect.left,
                        y =
                            if (rectBelow == null || rectBelow.left > rect.left)
                                rect.bottom
                            else
                                rectBelow.top,
                    )
                }

                // Bottom up along right edge.
                for (i in textLines.indices.reversed()) {
                    val rectAbove = textLines.getOrNull(i - 1)
                    val rect = textLines[i]
                    val rectBelow = textLines.getOrNull(i + 1)

                    lineTo(
                        x = rect.right,
                        y =
                            if (rectBelow == null || rectBelow.right < rect.right)
                                rect.bottom
                            else
                                rectBelow.top,
                    )
                    lineTo(
                        x = rect.right,
                        y =
                            if (rectAbove == null || rectAbove.right < rect.right)
                                rect.top
                            else
                                rectAbove.bottom,
                    )
                }

                close()
                translate(rect.topLeft)
            }
        }

        @Immutable
        data class Appearance(
            val background: Background? = null,
            val alignment: Alignment = Alignment.Center,
        )

        enum class Background(
            val colors: AppColors,
        ) {
            Light(LightAppColors),
            Dark(DarkAppColors),
            ;
        }

        enum class Alignment(
            val textAlign: TextAlign,
        ) {
            Center(TextAlign.Center),
            Left(TextAlign.Left),
            Right(TextAlign.Right),
            ;
        }
    }
}
