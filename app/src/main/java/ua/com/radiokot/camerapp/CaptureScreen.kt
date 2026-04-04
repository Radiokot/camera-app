package ua.com.radiokot.camerapp

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CaptureScreen(
    modifier: Modifier = Modifier,
) = Box(
    modifier = modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var surfaceRequest by remember {
        mutableStateOf<SurfaceRequest?>(null)
    }

    LaunchedEffect(Unit) {
        val provider = ProcessCameraProvider.awaitInstance(context)
        val previewUseCase = Preview.Builder().build().apply {
            setSurfaceProvider {
                surfaceRequest = it
            }
        }
        provider.bindToLifecycle(
            lifecycleOwner = lifecycleOwner,
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
            useCases = arrayOf(
                previewUseCase,
            ),
        )
    }

    if (surfaceRequest != null) {
        CameraXViewfinder(
            surfaceRequest = surfaceRequest!!,
            modifier = Modifier
                .fillMaxSize()
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            BasicText(
                text = "Opening the camera…",
                style = TextStyle(
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun CaptureScreenPreview(

) {
    CaptureScreen(
        modifier = Modifier
            .fillMaxSize()
    )
}
