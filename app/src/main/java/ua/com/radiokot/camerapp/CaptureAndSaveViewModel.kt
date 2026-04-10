package ua.com.radiokot.camerapp

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.Surface
import androidx.activity.OnBackPressedCallback
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.times
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
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
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min


@Immutable
class CaptureAndSaveViewModel(
    private val stampDirectory: File,
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

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Capture)
    val state: StateFlow<State> = _state

    val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            _state.value = State.Capture
            isEnabled = false
        }
    }

    init {
        viewModelScope.launch {
            state.collect { newState ->
                backPressedCallback.isEnabled = newState is State.Save

                if (newState is State.Capture) {
                    _captureFrameBitmap.value?.recycle()
                    _captureFrameBitmap.value = null
                }
            }
        }
    }

    fun onCaptureClicked(
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

            _state.value = State.Save(
                frameImage =
                    frameImage(
                        image = rotatedImageBitmap,
                        visibleViewfinderRect = visibleViewfinderRect,
                        visibleFrameRect = visibleFrameRect,
                    ).asImageBitmap(),
            )

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
        val shapeAlphaBitmap =
            ContextCompat
                .getDrawable(application, R.drawable.stamp_a)!!
                .toBitmap(
                    resultBitmap.width,
                    resultBitmap.height,
                    Bitmap.Config.ALPHA_8
                )

        resultBitmap.applyCanvas {
            drawBitmap(shapeAlphaBitmap, 0f, 0f, imageShaderPaint)
        }

        shapeAlphaBitmap.recycle()

        return resultBitmap
    }

    fun onSaveClicked() {
        val state = state.value
        if (state is State.Save) {
            saveImage(
                imageBitmap = state.frameImage.asAndroidBitmap(),
            )
        }
    }

    private var sendJob: Job? = null

    private fun saveImage(
        imageBitmap: Bitmap,
    ) {
        sendJob?.cancel()
        sendJob = viewModelScope.launch(Dispatchers.Default) {
            val outputFile = File(
                stampDirectory,
                "${System.currentTimeMillis()}.webp"
            )

            FileOutputStream(outputFile).use { stream ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    imageBitmap.compress(
                        Bitmap.CompressFormat.WEBP_LOSSY,
                        100,
                        stream,
                    )
                } else {
                    imageBitmap.compress(
                        Bitmap.CompressFormat.WEBP,
                        100,
                        stream,
                    )
                }
            }

            _state.value = State.Capture
        }
    }

    sealed interface State {
        object Capture : State
        class Save(
            val frameImage: ImageBitmap,
        ) : State
    }
}
