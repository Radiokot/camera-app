package ua.com.radiokot.camerapp

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: CaptureAndSendViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val state by viewModel.state.collectAsState()

            AnimatedContent(
                targetState = state,
                modifier = Modifier
                    .fillMaxSize()
            ) { state ->
                when (state) {
                    CaptureAndSendViewModel.State.Capture -> {
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
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }

                    is CaptureAndSendViewModel.State.Send -> {
                        SendScreen(
                            frameImage = state.frameImage,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    CaptureAndSendViewModel.State.Capture -> {
                        WindowInsetsControllerCompat(window, window.decorView)
                            .hide(WindowInsetsCompat.Type.statusBars())
                    }

                    is CaptureAndSendViewModel.State.Send -> {
                        WindowInsetsControllerCompat(window, window.decorView)
                            .show(WindowInsetsCompat.Type.statusBars())
                    }
                }
            }
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        onBackPressedDispatcher.addCallback {
            finish()
        }
        onBackPressedDispatcher.addCallback(viewModel.backPressedCallback)
    }
}
