package ua.com.radiokot.camerapp

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.Surface
import androidx.activity.OnBackPressedCallback
import androidx.annotation.FloatRange
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
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.util.fastCoerceIn
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.times
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.round


@Immutable
class CaptureAndSaveViewModel(
    private val stampRepository: StampRepository,
    val imageAdjustmentsControllerViewModel: ImageAdjustmentsControllerViewModel,
    application: Application,
) : AndroidViewModel(application) {

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

    private val _captureFrameBitmap: MutableStateFlow<Bitmap?> = MutableStateFlow(null)
    val captureFrameImage: StateFlow<ImageBitmap?> =
        _captureFrameBitmap
            .map(viewModelScope) { bitmap ->
                bitmap?.asImageBitmap()
            }

    private val _saveFrameBitmap: MutableStateFlow<Bitmap?> = MutableStateFlow(null)
    val saveFrameImage: StateFlow<ImageBitmap?> =
        combine(
            _saveFrameBitmap.filterNotNull(),
            combine(
                imageAdjustmentsControllerViewModel.contrastValue,
                imageAdjustmentsControllerViewModel.brightnessValue,
                imageAdjustmentsControllerViewModel.vibranceValue,
                transform = ::Triple
            ),
            transform = ::Pair
        )
            .map { (originalBitmap, adjustments) ->
                val width = originalBitmap.width
                val height = originalBitmap.height

                val pixels = IntArray(width * height)
                originalBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                applyImageAdjustments(
                    pixels = pixels,
                    contrast = adjustments.first / 100f,
                    brightness = adjustments.second / 100f,
                    vibrance = adjustments.third / 100f,
                )

                val resultBitmap = createBitmap(
                    width = width,
                    height = height,
                    config = originalBitmap.config!!,
                )
                resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

                resultBitmap.asImageBitmap()
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _captionInput: MutableStateFlow<String> = MutableStateFlow("")
    val captionInput: StateFlow<String> = _captionInput

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Capture)
    val state: StateFlow<State> = _state

    val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            _state.value = State.Capture
            isEnabled = false
        }
    }

    init {
        state.onEach { newState ->
            backPressedCallback.isEnabled = newState == State.Save

            if (newState == State.Capture) {
                _captureFrameBitmap.value?.recycle()
                _captureFrameBitmap.value = null
            }
        }.launchIn(viewModelScope)

        var prevSaveFrameBitmap: Bitmap? = null
        _saveFrameBitmap.onEach {
            prevSaveFrameBitmap?.recycle()
            prevSaveFrameBitmap = it
        }.launchIn(viewModelScope)
    }

    fun onCaptureRequested(
        visibleViewfinderSize: Size,
        visibleFrameRect: Rect,
    ) {
        capture(
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

    private var captureJob: Job? = null

    @SuppressLint("RestrictedApi")
    private fun capture(
        visibleViewfinderRect: RectF,
        visibleFrameRect: RectF,
    ) {
        captureJob?.cancel()
        captureJob = viewModelScope.launch(Dispatchers.Default) {
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
                    _captureFrameBitmap.value = lqResultBitmap

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

            imageAdjustmentsControllerViewModel.reset()
            _captionInput.value = ""
            _saveFrameBitmap.value = frameImage(
                image = rotatedImageBitmap,
                visibleViewfinderRect = visibleViewfinderRect,
                visibleFrameRect = visibleFrameRect,
            )
            _state.value = State.Save

            imageProxy.close()
            imageBitmap.recycle()
            rotatedImageBitmap.recycle()
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

        val resultBitmap = createBitmap(
            width = scaledFrameRect.width().toInt(),
            height = scaledFrameRect.height().toInt(),
        )

        val imageShaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = BitmapShader(
                image,
                Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP,
            ).apply {
                setLocalMatrix(Matrix().apply {
                    setTranslate(
                        -(image.width - scaledViewfinderRect.width()) / 2f
                                - scaledFrameRect.left,
                        -(image.height - scaledViewfinderRect.height()) / 2f
                                - scaledFrameRect.top,
                    )
                })
            }
            isAntiAlias = true
        }

        val stampPath =
            StampShapeA
                .path
                .toPath()
                .asAndroidPath()
                .apply {
                    transform(Matrix().apply {
                        val scale = resultBitmap.width / StampShapeA.size.width.value
                        setScale(scale, scale)
                    })
                }

        resultBitmap.applyCanvas {
            drawPath(stampPath, imageShaderPaint)
        }

        return resultBitmap
    }

    fun onCaptionInputChanged(
        newInput: String,
    ) {
        _captionInput.value = newInput
    }

    fun onSaveClicked() {
        val imageBitmap =
            saveFrameImage
                .value
                ?.asAndroidBitmap()
                ?: return

        saveStamp(imageBitmap)
    }

    private var saveJob: Job? = null

    private fun saveStamp(
        imageBitmap: Bitmap,
    ) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            val caption =
                _captionInput
                    .value
                    .trim()
                    .takeIf(String::isNotEmpty)

            stampRepository.addStamp(
                imageBitmap = imageBitmap,
                caption = caption,
            )

            _state.value = State.Capture
        }
    }

    private fun applyImageAdjustments(
        pixels: IntArray,
        @FloatRange(-1.0, 1.0)
        contrast: Float,
        @FloatRange(-1.0, 1.0)
        brightness: Float,
        @FloatRange(-1.0, 1.0)
        vibrance: Float,
    ) {
        // Contrast:
        // https://github.com/fabricjs/fabric.js/blob/e4cd1530ce2e684575e8db5d0a299d23c0c258e8/src/filters/Contrast.ts#L51
        // Brightness:
        // https://github.com/fabricjs/fabric.js/blob/e4cd1530ce2e684575e8db5d0a299d23c0c258e8/src/filters/Brightness.ts#L48
        // Vibrance:
        // https://github.com/fabricjs/fabric.js/blob/e4cd1530ce2e684575e8db5d0a299d23c0c258e8/src/filters/Vibrance.ts#L49

        var contrast = floor(contrast * 255)
        contrast = (259 * (contrast + 255)) / (255 * (259 - contrast))

        val brightness = round(brightness * 255)

        for (pixelIndex in pixels.indices) {
            val pixel = pixels[pixelIndex]

            val alpha = Color.alpha(pixel)
            if (alpha == 0) {
                continue
            }

            var red = Color.red(pixel).toFloat()
            var green = Color.green(pixel).toFloat()
            var blue = Color.blue(pixel).toFloat()

            val max = maxOf(red, green, blue)
            val avg = (red + green + blue) / 3
            val amt = ((abs(max - avg) * 2) / 255) * -vibrance

            red += if (max != red) (max - red) * amt else 0f
            red = contrast * (red - 128) + 128
            red += brightness

            green += if (max != green) (max - green) * amt else 0f
            green = contrast * (green - 128) + 128
            green += brightness

            blue += if (max != blue) (max - blue) * amt else 0f
            blue = contrast * (blue - 128) + 128
            blue += brightness

            pixels[pixelIndex] = Color.argb(
                alpha,
                red.toInt().fastCoerceIn(0, 255),
                green.toInt().fastCoerceIn(0, 255),
                blue.toInt().fastCoerceIn(0, 255),
            )
        }
    }

    enum class State {
        Capture,
        Save,
        ;
    }
}
