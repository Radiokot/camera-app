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

package ua.com.radiokot.camerapp.posters.ui

import android.content.Intent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.createPosterDestination(
    editStampPosterTextContract: EditStampPosterTextContract,
) = composable(
    route = CreatePosterRoute,
    arguments = listOf(
        navArgument(FirstStampId) {
            type = NavType.StringType
        }
    ),
    enterTransition = {
        fadeIn() + slideInVertically(
            initialOffsetY = { height -> height / 2 },
        )
    },
    exitTransition = { fadeOut() },
) { navEntry ->

    val viewModel: CreateStampPosterScreenViewModel = koinViewModel {
        parametersOf(
            CreateStampPosterScreenViewModel.Parameters(
                firstStampId =
                    navEntry
                        .arguments
                        ?.getString(FirstStampId)
                        ?: error("No $FirstStampId argument passed")
            )
        )
    }

    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateStampPosterScreenViewModel.Event.ProceedToSendIntent -> {
                    context.startActivity(
                        Intent.createChooser(
                            event.intent,
                            "Send a poster",
                        )
                    )
                }

                is CreateStampPosterScreenViewModel.Event.ProceedToEditText -> {
                    editStampPosterTextContract.proceedToEditText(
                        currentText = event.currentText,
                    )
                }
            }
        }
    }

    CreateStampPosterScreen(
        layersState = viewModel.layers.collectAsState(),
        onSendAction = viewModel::onSendAction,
        isDarkState = viewModel.isDark.collectAsState(),
        onBeginInteractionWithLayer = viewModel::onBeginInteractionWithLayer,
        onLayerTap = viewModel::onLayerTap,
        onToggleIsDarkAction = viewModel::onToggleIsDarkAction,
        onAddTextAction = viewModel::onAddTextAction,
        modifier = Modifier
            .fillMaxSize()
    )

    LaunchedEffect(editStampPosterTextContract, viewModel) {
        editStampPosterTextContract
            .getEditedTextFlow()
            .collect(viewModel::onDoneEditingText)
    }
}

private const val FirstStampId = "firstStampId"

const val CreatePosterRoute = "createPoster?firstStampId={$FirstStampId}"

fun CreatePosterRoute(
    firstStampId: String,
) =
    "createPoster?firstStampId=$firstStampId"
