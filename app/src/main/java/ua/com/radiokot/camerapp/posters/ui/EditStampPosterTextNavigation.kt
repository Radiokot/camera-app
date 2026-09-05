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

import android.net.Uri
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import ua.com.radiokot.camerapp.posters.domain.StampPosterLayer
import ua.com.radiokot.camerapp.ui.DarkAppColors
import ua.com.radiokot.camerapp.ui.LocalColors
import ua.com.radiokot.camerapp.util.DarkStatusAndNavigationBars

fun NavGraphBuilder.editPosterTextDestination(
    onDone: (EditStampPosterTextResult) -> Unit,
) = dialog(
    route = EditPosterTextRoute,
    arguments = listOf(
        navArgument(CurrentTextEncoded) {
            type = NavType.StringType
            nullable = true
        },
        navArgument(Alignment) {
            type = NavType.StringType
        },
        navArgument(Background) {
            type = NavType.StringType
            nullable = true
        }
    ),
    dialogProperties = DialogProperties(
        dismissOnClickOutside = false,
        dismissOnBackPress = false,
        usePlatformDefaultWidth = false,
        decorFitsSystemWindows = false,
    )
) { navEntry ->

    DarkStatusAndNavigationBars()

    val inputState = retain {
        TextFieldState(
            initialText =
                navEntry
                    .arguments
                    ?.getString(CurrentTextEncoded)
                    ?.let(Uri::decode)
                    ?: ""
        )
    }
    var appearance by retain {
        mutableStateOf(
            StampPosterLayer.Text.Appearance(
                alignment = navEntry
                    .arguments
                    ?.getString(Alignment)
                    ?.let(StampPosterLayer.Text.Alignment::valueOf)
                    ?: error("No $Alignment argument passed"),
                background = navEntry
                    .arguments
                    ?.getString(Background)
                    ?.let(StampPosterLayer.Text.Background::valueOf),
            )
        )
    }

    CompositionLocalProvider(
        LocalColors provides DarkAppColors
    ) {
        EditStampPosterTextDialog(
            inputState = inputState,
            appearance = appearance,
            onChangeAlignmentAction = {
                appearance = appearance.copy(
                    alignment = when (appearance.alignment) {
                        StampPosterLayer.Text.Alignment.Center -> StampPosterLayer.Text.Alignment.Left
                        StampPosterLayer.Text.Alignment.Left -> StampPosterLayer.Text.Alignment.Right
                        StampPosterLayer.Text.Alignment.Right -> StampPosterLayer.Text.Alignment.Center
                    }
                )
            },
            onChangeBackgroundAction = {
                appearance = appearance.copy(
                    background = when (appearance.background) {
                        StampPosterLayer.Text.Background.Light -> StampPosterLayer.Text.Background.Dark
                        StampPosterLayer.Text.Background.Dark -> null
                        null -> StampPosterLayer.Text.Background.Light
                    }
                )
            },
            onDone = {
                onDone(
                    EditStampPosterTextResult(
                        text =
                            inputState
                                .text
                                .takeIf(CharSequence::isNotEmpty)
                                ?.toString(),
                        appearance = appearance,
                    )
                )
            },
        )
    }
}

private const val CurrentTextEncoded = "currentTextEncoded"
private const val Alignment = "alignment"
private const val Background = "background"

const val EditPosterTextRoute =
    "editPosterText" +
            "?currentText={$CurrentTextEncoded}" +
            "&alignment={$Alignment}" +
            "&background={$Background}"

fun EditPosterTextRoute(
    currentText: String?,
    currentAppearance: StampPosterLayer.Text.Appearance,
) =
    "editPosterText" +
            "?currentText=${currentText?.let(Uri::encode)}" +
            "&alignment=${currentAppearance.alignment.name}" +
            "&background=${currentAppearance.background?.name}"
