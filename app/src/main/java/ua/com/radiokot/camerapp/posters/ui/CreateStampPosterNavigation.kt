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
import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ua.com.radiokot.camerapp.cut.ui.showToast
import ua.com.radiokot.camerapp.discardchanges.ui.ConfirmDiscardChangesContract
import ua.com.radiokot.camerapp.ui.LocalColors

fun NavGraphBuilder.createPosterDestination(
    editStampPosterTextContract: EditStampPosterTextContract,
    confirmDiscardChangesContract: ConfirmDiscardChangesContract,
    onDone: () -> Unit,
) = composable(
    route = CreatePosterRoute,
    arguments = listOf(
        navArgument(FirstStampId) {
            type = NavType.StringType
            nullable = true
        },
        navArgument(StampSelectionIndex) {
            type = NavType.StringType
            nullable = true
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
                        ?.getString(FirstStampId),
                stampSelectionIndex =
                    navEntry
                        .arguments
                        ?.getString(StampSelectionIndex)
                        ?.toInt(),
            )
        )
    }
    val isDiscardConfirmationRequired by viewModel.isDiscardConfirmationRequired.collectAsState()

    val context = LocalContext.current
    val colors = LocalColors.current

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

                is CreateStampPosterScreenViewModel.Event.ShowTooManyStampsWarning -> {
                    showToast(
                        context = context,
                        text = "That's too many for a poster",
                        colors = colors,
                    )
                }

                is CreateStampPosterScreenViewModel.Event.ShowLayerDeletedMessage -> {
                    showToast(
                        context = context,
                        text = "${event.layerName} deleted",
                        colors = colors,
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
        onEndInteractionWithLayer = viewModel::onEndInteractionWithLayer,
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

    BackHandler(
        enabled = isDiscardConfirmationRequired,
    ) {
        confirmDiscardChangesContract.proceedToConfirmDiscardChanges(
            message = "Discard this poster?",
        )
    }

    LaunchedEffect(Unit) {
        confirmDiscardChangesContract
            .getDiscardChangesDecisionFlow()
            .collect { toDiscard ->
                if (toDiscard) {
                    onDone()
                }
            }
    }
}

private const val FirstStampId = "firstStampId"
private const val StampSelectionIndex = "stampSelectionIndex"

const val CreatePosterRoute = "createPoster?firstStampId={$FirstStampId}" +
        "&stampSelectionIndex={$StampSelectionIndex}"

fun CreatePosterRoute(
    firstStampId: String,
) =
    "createPoster?firstStampId=$firstStampId"

fun CreatePosterRoute(
    stampSelectionIndex: Int,
) =
    "createPoster?stampSelectionIndex=$stampSelectionIndex"
