package ua.com.radiokot.camerapp

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.koin.androidx.viewmodel.ext.android.viewModel

class StampsActivity : ComponentActivity() {
    private val viewModel: StampsScreenViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        setContent {
            val stamps = viewModel.stamps.collectAsState()

            StampsScreen(
                stamps = stamps,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}
