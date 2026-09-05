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

@file:OptIn(ExperimentalAtomicApi::class)

package ua.com.radiokot.camerapp.util

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

fun interface NavRequestLauncher<in Request> {
    fun launch(request: Request): Any?
}

class NavResultContract<in Request, Result>(
    private val navController: NavController,
    private val launcher: NavRequestLauncher<Request>,
) : NavRequestLauncher<Request> by launcher {

    private val resultKey =
        resultKeyCounter
            .incrementAndFetch()
            .toString()

    fun setResult(result: Result) =
        navController
            .previousBackStackEntry
            ?.savedStateHandle
            ?.setResult(
                key = resultKey,
                value = result,
            )

    fun setResultAndNavigateUp(result: Result) {
        setResult(result)
        navController.navigateUp()
    }

    fun getResultFlow(
        requestor: NavBackStackEntry,
    ): Flow<Result> =
        requestor
            .savedStateHandle
            .getResultFlow(resultKey)

    private companion object {
        private val resultKeyCounter = AtomicInt(0)
    }
}
