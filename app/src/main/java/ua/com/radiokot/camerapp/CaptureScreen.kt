package ua.com.radiokot.camerapp

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInQuad
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun CaptureScreen(
    useCases: Array<UseCase?>,
    surfaceRequest: SurfaceRequest?,
    frameImage: ImageBitmap?,
    onCaptureClicked: (Size, Rect) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    modifier: Modifier = Modifier,
) = Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val processCameraProvider by produceState<ProcessCameraProvider?>(null) {
        value = ProcessCameraProvider.awaitInstance(context).also {
            it.bindToLifecycle(
                lifecycleOwner = lifecycleOwner,
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
                useCases = useCases,
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            processCameraProvider?.unbindAll()
        }
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
            .onPlaced { layoutCoordinates ->
                frameLayoutCoordinates = layoutCoordinates
            }
    ) {
        Image(
            painter = painterResource(
                if (frameImage == null)
                    R.drawable.stamp_a_stroke
                else
                    R.drawable.stamp_a
            ),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
        )

        if (frameImage != null) {
            val rotation = remember {
                Animatable(0f)
            }
            val offsetX = remember {
                Animatable(0f)
            }
            val offsetY = remember {
                Animatable(0f)
            }
            val scale = remember {
                Animatable(1f)
            }

            Image(
                bitmap = frameImage,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = rotation.value
                        translationX = offsetX.value
                        translationY = offsetY.value
                        scaleX = scale.value
                        scaleY = scale.value
                    }
                    .run {
                        if (sharedTransitionScope == null || animatedVisibilityScope == null) {
                            return@run this
                        }

                        with(sharedTransitionScope) {
                            sharedElement(
                                sharedContentState = rememberSharedContentState("image"),
                                animatedVisibilityScope = animatedVisibilityScope,
                            )
                        }
                    }
            )

            LaunchedEffect(Unit) {
                coroutineScope {
                    launch {
                        rotation.animateTo(
                            targetValue = Random
                                .nextInt(-10, 10)
                                .toFloat(),
                        )
                    }

                    launch {
                        offsetX.animateTo(
                            targetValue = Random
                                .nextInt(-200, 200)
                                .toFloat(),
                        )
                    }

                    launch {
                        offsetY.animateTo(
                            targetValue = Random
                                .nextInt(-200, 200)
                                .toFloat(),
                        )
                    }

                    launch {
                        scale.animateTo(
                            targetValue = 1.1f,
                            animationSpec = tween(
                                durationMillis = 120,
                                easing = EaseInQuad,
                            ),
                        )
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = 120,
                                easing = EaseOutQuad,
                            ),
                        )
                    }
                }
            }
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
        sharedTransitionScope = null,
        animatedVisibilityScope = null,
        modifier = Modifier
            .fillMaxSize()
    )
}
