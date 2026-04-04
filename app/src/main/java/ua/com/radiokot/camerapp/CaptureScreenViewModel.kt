package ua.com.radiokot.camerapp

import android.view.Surface
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageCapture
import androidx.camera.core.takePicture
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Immutable
class CaptureScreenViewModel : ViewModel() {

    @ExperimentalZeroShutterLag
    val captureUseCase =
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG)
            .setTargetRotation(Surface.ROTATION_0)
            .build()

    fun onCaptureClicked() {
        takePicture()
    }

    private var takePictureJob: Job? = null
    private fun takePicture() {
        takePictureJob?.cancel()
        takePictureJob = viewModelScope.launch {
            captureUseCase.takePicture().use { imageProxy ->
                val bitmap = imageProxy.toBitmap()
                println("OOLEG captured, rotate ${imageProxy.imageInfo.rotationDegrees} degrees")
                bitmap.recycle()
            }
        }
    }
}
