package ua.com.radiokot.camerapp

import android.content.Intent
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
                            onSendClicked = viewModel::onSendClicked,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                }
            }
        }

        viewModel.state.onEach { state ->
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
        }.launchIn(lifecycleScope)

        viewModel.events.onEach { event ->
            when (event) {
                CaptureAndSendViewModel.Event.ShareWebp -> {
                    shareWebp()
                }
            }
        }.launchIn(lifecycleScope)

        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        onBackPressedDispatcher.addCallback {
            finish()
        }
        onBackPressedDispatcher.addCallback(viewModel.backPressedCallback)
    }

    private fun shareWebp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            setPackage("org.telegram.messenger.web")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(
                ShareContentProvider.WEBP_URI,
                ShareContentProvider.WEBP_CONTENT_TYPE,
            )
            putExtra(
                Intent.EXTRA_STREAM,
                ShareContentProvider.WEBP_URI,
            )
        }
        startActivity(intent)
    }
}
