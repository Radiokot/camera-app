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

package ua.com.radiokot.camerapp.collectionselection.ui

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import ua.com.radiokot.camerapp.util.getResultFlow
import ua.com.radiokot.camerapp.util.setResult

class SelectDestinationCollectionContract(
    private val navController: NavController,
) {
    fun proceedToCollectionSelection(
        request: SelectDestinationCollectionRequest,
    ) {
        navController
            .navigate(
                route = SelectDestinationCollectionRoute(
                    request = request,
                )
            ) {
                launchSingleTop = true
            }
    }

    fun onCollectionSelected(
        collectionId: String,
    ) {
        navController
            .previousBackStackEntry
            ?.savedStateHandle
            ?.setResult(
                key = SELECTED_COLLECTION_ID,
                value = collectionId,
            )
        navController.navigateUp()
    }

    fun onCancel() {
        navController.navigateUp()
    }

    fun getSelectedCollectionIdFlow(
        requestor: NavBackStackEntry,
    ): Flow<String> =
        requestor
            .savedStateHandle
            .getResultFlow(SELECTED_COLLECTION_ID)

    private companion object {
        private const val SELECTED_COLLECTION_ID = "SDCCCollectionId"
    }
}
