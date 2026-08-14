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
import android.widget.Toast
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
import kotlinx.coroutines.flow.combine
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ua.com.radiokot.camerapp.cut.ui.showToast
import ua.com.radiokot.camerapp.discardchanges.ui.ConfirmDiscardChangesContract
import ua.com.radiokot.camerapp.ui.LocalColors

fun NavGraphBuilder.createPosterDestination(
    editStampPosterTextContract: EditStampPosterTextContract,
    confirmDiscardChangesContract: ConfirmDiscardChangesContract,
    selectStampsForPosterContract: SelectStampsForPosterContract,
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
                        currentAppearance = event.currentAppearance,
                    )
                }

                is CreateStampPosterScreenViewModel.Event.ProceedToSelectStampsToAdd -> {
                    selectStampsForPosterContract.proceedToSelectStamps(
                        maxCount = event.maxCount,
                    )
                }

                is CreateStampPosterScreenViewModel.Event.ShowLayerDeletedMessage -> {
                    showToast(
                        context = context,
                        text = "${event.layerName} removed",
                        colors = colors,
                        durationMs = 900,
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
        canAddStamps = viewModel.canAddStamps.collectAsState().value,
        onAddStampsAction = viewModel::onAddStampsAction,
        modifier = Modifier
            .fillMaxSize()
    )

    LaunchedEffect(editStampPosterTextContract, viewModel, navEntry) {
        combine(
            editStampPosterTextContract
                .getEditedTextFlow(navEntry),
            editStampPosterTextContract
                .getEditedAppearanceFlow(navEntry),
            transform = ::Pair,
        ).collect { (text, appearance) ->
            viewModel.onDoneEditingText(
                text = text,
                appearance = appearance,
            )
        }
    }

    LaunchedEffect(selectStampsForPosterContract, viewModel, navEntry) {
        selectStampsForPosterContract
            .getStampSelectionIndexFlow(navEntry)
            .collect(viewModel::onSelectedStampsToAdd)
    }

    BackHandler(
        enabled = isDiscardConfirmationRequired,
    ) {
        confirmDiscardChangesContract.proceedToConfirmDiscardChanges(
            message = "Discard this poster?",
        )
    }

    LaunchedEffect(navEntry, onDone, confirmDiscardChangesContract) {
        confirmDiscardChangesContract
            .getDiscardChangesDecisionFlow(navEntry)
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
