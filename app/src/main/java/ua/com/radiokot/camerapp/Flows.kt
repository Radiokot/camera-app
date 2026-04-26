/* Copyright 2025 Oleg Koretsky

   This file is part of the 4Money,
   a budget tracking Android app.

   4Money is free software: you can redistribute it
   and/or modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation, either version 3 of the License,
   or (at your option) any later version.

   4Money is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   See the GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with 4Money. If not, see <http://www.gnu.org/licenses/>.
*/

package ua.com.radiokot.camerapp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * @return [StateFlow] containing the results of applying the given [transform] function
 * to each value of the original [StateFlow].
 */
inline fun <T, R> StateFlow<T>.map(
    coroutineScope: CoroutineScope,
    crossinline transform: (value: T) -> R,
): StateFlow<R> =
    map(transform)
        .stateIn(coroutineScope, SharingStarted.Eagerly, transform(value))

/**
 * @return a buffered [MutableSharedFlow], configured for event publishing.
 */
fun <T> eventSharedFlow() =
    MutableSharedFlow<T>(
        // Controls the number of buffered (therefore delivered) events fired rapidly.
        // For example, when you fire ShowToast and then immediately Close,
        // you want both of them delivered and processed.
        // Yet, rapidly firing too many events is probably a bug.
        extraBufferCapacity = 5,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

fun coroutineScopeThatCancelsWith(
    other: CoroutineScope,
) = CoroutineScope(
    context = requireNotNull(other.coroutineContext[Job]) {
        "The other scope cannot be cancelled because it does not have a job"
    },
)
