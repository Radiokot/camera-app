package ua.com.radiokot.camerapp

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : ComponentActivity() {
    private val viewModel: CaptureAndSaveViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            SharedTransitionLayout(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val state by viewModel.state.collectAsState()

                AnimatedContent(
                    targetState = state,
                    transitionSpec = {
                        ContentTransform(
                            targetContentEnter = fadeIn(),
                            initialContentExit = ExitTransition.KeepUntilTransitionsFinished,
                            sizeTransform = null,
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                ) { state ->
                    when (state) {
                        CaptureAndSaveViewModel.State.Capture -> {
                            val surfaceRequest by viewModel.surfaceRequest.collectAsState()
                            val frameImage by viewModel.captureFrameImage.collectAsState()

                            CaptureScreen(
                                useCases = arrayOf(
                                    viewModel.previewUseCase,
                                    viewModel.captureUseCase,
                                ),
                                surfaceRequest = surfaceRequest,
                                frameImage = frameImage,
                                onCaptureClicked = viewModel::onCaptureClicked,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }

                        is CaptureAndSaveViewModel.State.Save -> {
                            SaveScreen(
                                frameImage = state.frameImage,
                                onSaveClicked = viewModel::onSaveClicked,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        viewModel.state.onEach { state ->
            when (state) {
                CaptureAndSaveViewModel.State.Capture -> {
                    WindowInsetsControllerCompat(window, window.decorView)
                        .hide(WindowInsetsCompat.Type.statusBars())
                }

                is CaptureAndSaveViewModel.State.Save -> {
                    WindowInsetsControllerCompat(window, window.decorView)
                        .show(WindowInsetsCompat.Type.statusBars())
                }
            }
        }.launchIn(lifecycleScope)

        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        onBackPressedDispatcher.addCallback {
            finish()
        }
        onBackPressedDispatcher.addCallback(viewModel.backPressedCallback)
    }
}
