package ua.com.radiokot.camerapp

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CaptureScreen(
    useCases: Array<UseCase?>,
    surfaceRequest: SurfaceRequest?,
    frameImage: ImageBitmap?,
    onCaptureClicked: (Size, Rect) -> Unit,
    modifier: Modifier = Modifier,
) = Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(useCases, lifecycleOwner) {
        ProcessCameraProvider
            .awaitInstance(context)
            .bindToLifecycle(
                lifecycleOwner = lifecycleOwner,
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
                useCases = useCases,
            )
    }

    var frameLayoutCoordinates by remember {
        mutableStateOf<LayoutCoordinates?>(null)
    }

    if (surfaceRequest != null) {
        CameraXViewfinder(
            surfaceRequest = surfaceRequest,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    enabled = frameLayoutCoordinates != null,
                    onClick = {
                        val viewfinderSize =
                            frameLayoutCoordinates!!
                                .parentLayoutCoordinates!!
                                .size
                                .toSize()
                        val frameRect = frameLayoutCoordinates!!.boundsInParent()

                        onCaptureClicked(viewfinderSize, frameRect)
                    },
                )
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

    Box(
        modifier = Modifier
            .size(FrameSize * 1.5f)
            .border(
                width = 2.dp,
                color = Color.Red,
            )
            .onPlaced { layoutCoordinates ->
                frameLayoutCoordinates = layoutCoordinates
            }
    ) {
        if (frameImage != null) {
            Image(
                bitmap = frameImage,
                contentDescription = null,
                modifier =
                    Modifier
                        .fillMaxSize()
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun CaptureScreenPreview(

) {
    CaptureScreen(
        useCases = emptyArray(),
        surfaceRequest = null,
        frameImage = null,
        onCaptureClicked = { _, _ -> },
        modifier = Modifier
            .fillMaxSize()
    )
}
