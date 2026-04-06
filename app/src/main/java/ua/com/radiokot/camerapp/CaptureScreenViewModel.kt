package ua.com.radiokot.camerapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.Surface
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.core.takePicture
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.times
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.min


@Immutable
class CaptureScreenViewModel : ViewModel() {

    val previewUseCase =
        Preview.Builder().build()

    val surfaceRequest: StateFlow<SurfaceRequest?> =
        callbackFlow {
            previewUseCase.setSurfaceProvider(::trySend)
            awaitClose { previewUseCase.surfaceProvider = null }
        }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

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

    private val _frameBitmap: MutableStateFlow<Bitmap?> = MutableStateFlow(null)
    private var previousFrameBitmap: Bitmap? = null
    val frameImage: StateFlow<ImageBitmap?> =
        _frameBitmap
            .map(viewModelScope) { bitmap ->
                previousFrameBitmap?.recycle()
                previousFrameBitmap = bitmap
                bitmap?.asImageBitmap()
            }

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

    @SuppressLint("RestrictedApi")
    private fun takePicture(
        visibleViewfinderRect: RectF,
        visibleFrameRect: RectF,
    ) {
        takePictureJob?.cancel()
        takePictureJob = viewModelScope.launch(Dispatchers.Default) {
            val resolutionInfo = previewUseCase.resolutionInfo!!
            val previewImageBitmap =
                createBitmap(
                    width =
                        if (resolutionInfo.rotationDegrees == 0 || resolutionInfo.rotationDegrees == 180)
                            resolutionInfo.resolution.width
                        else
                            resolutionInfo.resolution.height,
                    height =
                        if (resolutionInfo.rotationDegrees == 0 || resolutionInfo.rotationDegrees == 180)
                            resolutionInfo.resolution.height
                        else
                            resolutionInfo.resolution.width,
                )
            PixelCopy.request(
                surfaceRequest.value!!.deferrableSurface.surface.get()!!,
                previewImageBitmap,
                {
                    val lqResultBitmap =
                        frameImage(
                            image = previewImageBitmap,
                            visibleViewfinderRect = visibleViewfinderRect,
                            visibleFrameRect = visibleFrameRect,
                        )
                    _frameBitmap.value = lqResultBitmap

                    previewImageBitmap.recycle()
                },
                Handler(Looper.getMainLooper()),
            )

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

            val resultBitmap =
                frameImage(
                    image = rotatedImageBitmap,
                    visibleViewfinderRect = visibleViewfinderRect,
                    visibleFrameRect = visibleFrameRect,
                )
            _frameBitmap.value = resultBitmap

            imageProxy.close()
            imageBitmap.recycle()
            rotatedImageBitmap.recycle()

            println("OOLEG done $resultBitmap")
        }
    }

    private fun frameImage(
        image: Bitmap,
        visibleViewfinderRect: RectF,
        visibleFrameRect: RectF,
    ): Bitmap {
        val frameScale = min(
            image.width / visibleViewfinderRect.width(),
            image.height / visibleViewfinderRect.height(),
        )
        val scaledViewfinderRect = visibleViewfinderRect * frameScale
        val scaledFrameRect = visibleFrameRect * frameScale

        val resultBitmap =
            createBitmap(
                width = scaledFrameRect.width().toInt(),
                height = scaledFrameRect.height().toInt(),
            ).applyCanvas {
                drawBitmap(
                    image,
                    -(image.width - scaledViewfinderRect.width()) / 2f
                            - scaledFrameRect.left,
                    -(image.height - scaledViewfinderRect.height()) / 2f
                            - scaledFrameRect.top,
                    Paint(Paint.ANTI_ALIAS_FLAG),
                )
            }

        return resultBitmap
    }
}
