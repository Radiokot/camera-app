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

package ua.com.radiokot.camerapp.discardchanges.ui

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import ua.com.radiokot.camerapp.util.getResultFlow
import ua.com.radiokot.camerapp.util.setResult

class ConfirmDiscardChangesContract(
    private val navController: NavController,
) {
    fun proceedToConfirmDiscardChanges(
        message: String,
    ) {
        navController
            .navigate(
                route = ConfirmDiscardChangesRoute(
                    message = message,
                )
            ) {
                launchSingleTop = true
            }
    }

    fun onDiscardChangesDecision(
        toDiscard: Boolean,
    ) {
        navController
            .previousBackStackEntry
            ?.savedStateHandle
            ?.setResult(
                key = TO_DISCARD,
                value = toDiscard,
            )
        navController.navigateUp()
    }

    fun getDiscardChangesDecisionFlow(
        requestor: NavBackStackEntry,
    ): Flow<Boolean> =
        requestor
            .savedStateHandle
            .getResultFlow(TO_DISCARD)

    private companion object {
        private const val TO_DISCARD = "CDCCToDiscard"
    }
}
