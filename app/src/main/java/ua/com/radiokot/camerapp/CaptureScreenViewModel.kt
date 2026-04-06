package ua.com.radiokot.camerapp

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.view.Surface
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageCapture
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.core.takePicture
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.times
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.min


@Immutable
class CaptureScreenViewModel : ViewModel() {

    @ExperimentalZeroShutterLag
    val captureUseCase =
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG)
            .setTargetRotation(Surface.ROTATION_0)
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            android.util.Size(1920, 1920),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER,
                        )
                    )
                    .build()
            )
            .build()

    fun onCaptureClicked(
        visibleViewfinderSize: Size,
        visibleFrameRect: Rect,
    ) {
        takePicture(
            visibleViewfinderRect =
                RectF(0f, 0f, visibleViewfinderSize.width, visibleViewfinderSize.height),
            visibleFrameRect =
                RectF(
                    visibleFrameRect.left,
                    visibleFrameRect.top,
                    visibleFrameRect.right,
                    visibleFrameRect.bottom
                ),
        )
    }

    private var takePictureJob: Job? = null
    private fun takePicture(
        visibleViewfinderRect: RectF,
        visibleFrameRect: RectF,
    ) {
        takePictureJob?.cancel()
        takePictureJob = viewModelScope.launch(Dispatchers.Default) {
            val imageProxy = captureUseCase.takePicture()
            val imageBitmap = imageProxy.toBitmap()
            val rotatedImageBitmap = Bitmap.createBitmap(
                imageBitmap,
                0, 0,
                imageProxy.width,
                imageProxy.height,
                Matrix().apply {
                    setRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                },
                true
            )

            val frameScale = min(
                rotatedImageBitmap.width / visibleViewfinderRect.width(),
                rotatedImageBitmap.height / visibleViewfinderRect.height(),
            )
            val scaledViewfinderRect = visibleViewfinderRect * frameScale
            val scaledFrameRect = visibleFrameRect * frameScale

            val resultBitmap =
                createBitmap(
                    width = scaledFrameRect.width().toInt(),
                    height = scaledFrameRect.height().toInt(),
                ).applyCanvas {
                    drawBitmap(
                        rotatedImageBitmap,
                        -(rotatedImageBitmap.width - scaledViewfinderRect.width()) / 2f
                                - scaledFrameRect.left,
                        -(rotatedImageBitmap.height - scaledViewfinderRect.height()) / 2f
                                - scaledFrameRect.top,
                        Paint(Paint.ANTI_ALIAS_FLAG),
                    )
                }

            imageProxy.close()
            imageBitmap.recycle()
            rotatedImageBitmap.recycle()

            println("OOLEG done $resultBitmap")
        }
    }
}
