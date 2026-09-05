/* Copyright 2026 Oleg Koretsky

   This file is part of the Press-Cut,
   a digital postage stamp cutter Android app.

   Press-Cut is free software: you can redistribute it
   and/or modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation, either version 3 of the License,
   or (at your option) any later version.

   Press-Cut is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   See the GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Press-Cut. If not, see <http://www.gnu.org/licenses/>.
*/

@file:Suppress("FunctionName")

package ua.com.radiokot.camerapp.stamps.ui

import android.content.Intent
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ua.com.radiokot.camerapp.collectionselection.ui.SelectDestinationCollectionRequest
import ua.com.radiokot.camerapp.util.NavResultContract

fun NavGraphBuilder.stampDestination(
    sharedTransitionScope: SharedTransitionScope?,
    selectDestinationCollectionContract: NavResultContract<SelectDestinationCollectionRequest, String>,
    onProceedToCreatePoster: (stampId: String) -> Unit,
    onDone: () -> Unit,
) = composable(
    route = StampRoute,
    arguments = listOf(
        navArgument(StampId) {
            type = NavType.StringType
        },
    ),
) { navEntry ->
    val viewModel: StampScreenViewModel = koinViewModel {
        parametersOf(
            StampScreenViewModel.Parameters(
                stampId =
                    navEntry
                        .arguments
                        ?.getString(StampId)
                        ?: error("No $StampId argument passed"),
            )
        )
    }
    var isSwipedToExit by remember { mutableStateOf(false) }

    StampScreen(
        stampId = viewModel.stampId,
        captionState = viewModel.caption,
        isCaptionInputEnabled = viewModel.isCaptionInputEnabled.collectAsState(),
        onAddCaptionAction = viewModel::onAddCaptionAction,
        onDeleteAction = viewModel::onDeleteAction,
        onMoveAction = viewModel::onMoveAction,
        onSendAsImageAction = viewModel::onSendAsImageAction,
        onSendAsPosterAction = viewModel::onSendAsPosterAction,
        imageUri = viewModel.imageUri,
        shape = viewModel.shape,
        takenAt = viewModel.takenAt,
        onSwipedToExit = {
            // Prevent firing multiple times during transition.
            if (!isSwipedToExit) {
                isSwipedToExit = true
                onDone()
            }
        },
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = this@composable,
        modifier = Modifier
            .fillMaxSize()
    )

    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is StampScreenViewModel.Event.ProceedToMoveDestinationCollectionSelection -> {
                    selectDestinationCollectionContract.launch(
                        request = SelectDestinationCollectionRequest.MoveStamps(
                            currentCollectionId = event.currentCollectionId,
                            isSingleStamp = true,
                        ),
                    )
                }

                is StampScreenViewModel.Event.ProceedToSendIntent -> {
                    context.startActivity(
                        Intent.createChooser(
                            event.intent,
                            "Send a stamp",
                        )
                    )
                }

                is StampScreenViewModel.Event.ProceedToCreatePoster -> {
                    onProceedToCreatePoster(event.stampId)
                }

                is StampScreenViewModel.Event.Done -> {
                    onDone()
                }
            }
        }
    }

    LaunchedEffect(viewModel, selectDestinationCollectionContract, navEntry) {
        selectDestinationCollectionContract
            .getResultFlow(navEntry)
            .collect(viewModel::onMoveDestinationCollectionSelected)
    }
}

private const val StampId = "stampId"

const val StampRoute = "stamp/{$StampId}"

fun StampRoute(
    stampId: String,
) =
    "stamp/$stampId"
