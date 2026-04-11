package ua.com.radiokot.camerapp

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.skydoves.landscapist.image.LocalLandscapist
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class StampsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        setContent {
            CompositionLocalProvider(
                LocalLandscapist provides koinInject(),
            ) {
                SharedTransitionLayout {
                    StampsNavHost(
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun SharedTransitionScope.StampsNavHost(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val stampGridState = rememberLazyGridState()

    NavHost(
        navController = navController,
        startDestination = StampsDestination,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        modifier = modifier
            .paperBackground(
                verticalOffset = { -stampGridState.firstVisibleItemScrollOffset }
            )
    ) {
        composable(
            route = StampsDestination,
        ) {
            val viewModel: StampsScreenViewModel = koinViewModel()
            val stamps = viewModel.stamps.collectAsState()

            StampsScreen(
                stamps = stamps,
                gridState = stampGridState,
                onStampClicked = viewModel::onStampClicked,
                sharedTransitionScope = this@StampsNavHost,
                animatedVisibilityScope = this@composable,
                modifier = Modifier
                    .fillMaxSize()
            )

            LaunchedEffect(viewModel) {
                viewModel.events.collect { event ->
                    when (event) {
                        is StampsScreenViewModel.Event.ProceedToStamp -> {
                            navController.navigate(
                                route = "${StampsDestination}/${event.stampId}",
                            )
                        }
                    }
                }
            }
        }

        composable(
            route = "${StampsDestination}/{stampId}",
            arguments = listOf(
                navArgument("stampId") {
                    type = NavType.StringType
                }
            ),
        ) { navEntry ->
            val viewModel: StampScreenViewModel = koinViewModel {
                parametersOf(
                    StampScreenViewModel.Parameters(
                        stampId = navEntry.arguments?.getString("stampId")
                            ?: error("No ID argument passed"),
                    )
                )
            }

            StampScreen(
                stampId = viewModel.stampId,
                thumbnailUrl = viewModel.thumbnailUrl,
                sharedTransitionScope = this@StampsNavHost,
                animatedVisibilityScope = this@composable,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}

private const val StampsDestination = "stamps"
