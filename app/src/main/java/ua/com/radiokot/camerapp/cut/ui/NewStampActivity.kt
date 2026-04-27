package ua.com.radiokot.camerapp.cut.ui

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.asIntState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class NewStampActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.light(0, 0),
        )
        super.onCreate(savedInstanceState)

        val collectionId: String? = intent.getStringExtra(COLLECTION_ID_EXTRA)

        setContent {
            SharedTransitionLayout(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                StampCutNavHost(
                    collectionId = collectionId,
                    onDidSave = ::finish,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }

    companion object {
        private const val COLLECTION_ID_EXTRA = "collectionId"

        /**
         * @param collectionId ID of a specific collection to save the stamp into,
         * if not specified it's saved into the primary collection.
         */
        fun getBundle(
            collectionId: String?,
        ) = Bundle().apply {
            putString(COLLECTION_ID_EXTRA, collectionId)
        }
    }
}

@Composable
private fun SharedTransitionScope.StampCutNavHost(
    modifier: Modifier = Modifier,
    collectionId: String?,
    onDidSave: () -> Unit,
) {
    val navController = rememberNavController()
    var stampImageBitmapToSave by remember {
        mutableStateOf<Bitmap?>(null)
    }

    NavHost(
        navController = navController,
        startDestination = CutDestination,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        modifier = modifier
    ) {
        composable(
            route = CutDestination,
        ) {
            val viewModel: StampCutScreenViewModel = koinViewModel()
            val surfaceRequest by viewModel.surfaceRequest.collectAsState()
            val cutImage by viewModel.cutImage.collectAsState()

            StampCutScreen(
                useCases = viewModel.useCases,
                surfaceRequest = surfaceRequest,
                cutImage = cutImage,
                onCutAction = viewModel::onCutAction,
                sharedTransitionScope = this@StampCutNavHost,
                animatedVisibilityScope = this@composable,
                modifier = Modifier
                    .fillMaxSize()
            )

            LaunchedEffect(viewModel) {
                viewModel.events.collect { event ->
                    when (event) {

                        is StampCutScreenViewModel.Event.DidCut -> {
                            stampImageBitmapToSave?.recycle()
                            stampImageBitmapToSave = event.stampImageBitmap

                            navController.navigate(
                                route = SaveDestination,
                            ) {
                                launchSingleTop = true
                            }
                        }
                    }
                }
            }

            DisposableEffect(viewModel) {
                onDispose {
                    viewModel.onScreenDisposed()
                }
            }
        }

        composable(
            route = SaveDestination,
        ) {
            val viewModel: StampSaveScreenViewModel = koinViewModel {
                parametersOf(
                    StampSaveScreenViewModel.Parameters(
                        collectionId = collectionId,
                        stampImageBitmap =
                            stampImageBitmapToSave
                                ?: error("Can't open the save screen without the bitmap to save")
                    )
                )
            }
            val imageAdjustmentsControllerViewModel: ImageAdjustmentsControllerViewModel =
                viewModel.imageAdjustmentsControllerViewModel

            StampSaveScreen(
                captionInputState = viewModel.captionInput.collectAsState(),
                onCaptionInputChanged = viewModel::onCaptionInputChanged,
                imageState = viewModel.adjustedStampImage.collectAsState(),
                onSaveAction = viewModel::onSaveAction,
                adjustmentsControllerItems = imageAdjustmentsControllerViewModel.items,
                currentAdjustmentsControllerItemState =
                    imageAdjustmentsControllerViewModel.currentItem.collectAsState(),
                onCurrentAdjustmentsControllerItemChanged =
                    imageAdjustmentsControllerViewModel::onCurrentItemChanged,
                adjustmentsControllerValueState =
                    imageAdjustmentsControllerViewModel
                        .currentValue
                        .collectAsState()
                        .asIntState(),
                sharedTransitionScope = this@StampCutNavHost,
                animatedVisibilityScope = this@composable,
                onAdjustmentsControllerValueChanged =
                    imageAdjustmentsControllerViewModel::onValueChanged,
                modifier = Modifier
                    .fillMaxSize(),
            )

            LaunchedEffect(viewModel) {
                viewModel.events.collect { event ->
                    when (event) {
                        StampSaveScreenViewModel.Event.DidSave -> {
                            onDidSave()
                        }
                    }
                }
            }
        }
    }
}

private const val CutDestination = "cut"
private const val SaveDestination = "save"
