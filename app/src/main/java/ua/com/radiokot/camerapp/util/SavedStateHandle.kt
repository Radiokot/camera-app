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

package ua.com.radiokot.camerapp.util

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

fun <T> SavedStateHandle.setResult(
    key: String,
    value: T,
) =
    set(key, value to System.currentTimeMillis())

fun <T> SavedStateHandle.getResultFlow(
    key: String,
): Flow<T> =
    getStateFlow<Pair<T, Long>?>(
        key = key,
        initialValue = null
    )
        .filterNotNull()
        .map { (value, _) ->
            remove<Pair<T, Long>>(key)
            value
        }
