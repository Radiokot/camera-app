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

package ua.com.radiokot.camerapp.posters.ui

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import ua.com.radiokot.camerapp.posters.domain.StampPosterLayer
import ua.com.radiokot.camerapp.util.getResultFlow
import ua.com.radiokot.camerapp.util.setResult

class EditStampPosterTextContract(
    private val navController: NavController,
) {
    fun proceedToEditText(
        currentText: String?,
        currentAppearance: StampPosterLayer.Text.Appearance,
    ) {
        navController
            .navigate(
                route = EditPosterTextRoute(
                    currentText = currentText,
                    currentAppearance = currentAppearance,
                )
            ) {
                launchSingleTop = true
            }
    }

    fun onDoneEditing(
        text: String?,
        appearance: StampPosterLayer.Text.Appearance,
    ) {
        navController
            .previousBackStackEntry
            ?.savedStateHandle
            ?.setResult(
                key = EDITED_TEXT,
                value = text,
            )
        navController
            .previousBackStackEntry
            ?.savedStateHandle
            ?.setResult(
                key = EDITED_APPEARANCE,
                value = appearance,
            )
        navController.navigateUp()
    }

    fun getEditedTextFlow(
        requestor: NavBackStackEntry,
    ): Flow<String?> =
        requestor
            .savedStateHandle
            .getResultFlow(EDITED_TEXT)

    fun getEditedAppearanceFlow(
        requestor: NavBackStackEntry,
    ): Flow<StampPosterLayer.Text.Appearance> =
        requestor
            .savedStateHandle
            .getResultFlow(EDITED_APPEARANCE)

    private companion object {
        private const val EDITED_TEXT = "ESPTCEditedText"
        private const val EDITED_APPEARANCE = "ESPTCEditedAppearance"
    }
}
