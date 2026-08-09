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

package ua.com.radiokot.camerapp.discardchanges.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument

fun NavGraphBuilder.confirmDiscardChangesDestination(
    contract: ConfirmDiscardChangesContract,
) = dialog(
    route = ConfirmDiscardChangesRoute,
    arguments = listOf(
        navArgument(Message) {
            type = androidx.navigation.NavType.StringType
        }
    ),
) { navEntry ->
    ConfirmDiscardChangesDialog(
        confirmationMessage =
            navEntry
                .arguments
                ?.getString(Message)
                ?: error("No $Message argument passed"),
        onDecision = contract::onDiscardChangesDecision,
    )
}

private const val Message = "message"

const val ConfirmDiscardChangesRoute = "confirmDiscardChanges?message={$Message}"

fun ConfirmDiscardChangesRoute(
    message: String,
): String =
    "confirmDiscardChanges?message=$message"
