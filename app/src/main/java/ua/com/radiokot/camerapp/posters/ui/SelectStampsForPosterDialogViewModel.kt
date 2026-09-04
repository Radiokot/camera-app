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

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import ua.com.radiokot.camerapp.stamps.domain.Stamp
import ua.com.radiokot.camerapp.stamps.domain.StampRepository
import ua.com.radiokot.camerapp.stamps.domain.StampSelections
import ua.com.radiokot.camerapp.stamps.ui.StampsGridItem
import ua.com.radiokot.camerapp.util.eventSharedFlow
import ua.com.radiokot.camerapp.util.lazyLogger

@Stable
class SelectStampsForPosterDialogViewModel(
    private val stampRepository: StampRepository,
    private val stampComparator: Comparator<Stamp>,
    private val parameters: Parameters,
) : ViewModel() {

    private val log by lazyLogger("SelectStampsForPosterDialogVM")

    private val selectedStampIds: MutableStateFlow<PersistentSet<String>> =
        MutableStateFlow(persistentSetOf())

    val events: SharedFlow<Event>
        field = eventSharedFlow()

    val items: StateFlow<ImmutableList<StampsGridItem>> = runBlocking {
        stampRepository
            .getStampsFlow()
            .map { stamps ->
                stamps
                    .sortedWith(stampComparator)
                    .map(::StampsGridItem)
                    .toPersistentList()
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope)
    }
    val selectedItemKeys: StateFlow<ImmutableSet<String>> =
        selectedStampIds

    fun onStampClicked(
        item: StampsGridItem,
    ) {
        val stampId = item.key

        log.debug {
            "onStampClicked(): toggling selection of the stamp:" +
                    "\nstampId=$stampId"
        }

        selectedStampIds.update { selectedStampIds ->
            if (stampId in selectedStampIds)
                selectedStampIds.removing(stampId)
            else if (selectedStampIds.size < parameters.maxCount)
                selectedStampIds.adding(stampId)
            else {
                log.debug {
                    "onStampClicked(): too many stamps, not selecting"
                }

                events.tryEmit(Event.ShowTooManyStampsWarning)

                selectedStampIds
            }
        }
    }

    fun onAddSelectedAction() {
        val selectionIndex = StampSelections + selectedStampIds.value

        log.debug {
            "onAddSelectedAction(): done with selection:" +
                    "\nselectionIndex=$selectionIndex"
        }

        events.tryEmit(
            Event.Done(
                selectionIndex = selectionIndex,
            )
        )
    }

    data class Parameters(
        val maxCount: Int,
    )

    sealed interface Event {

        class Done(
            val selectionIndex: Int,
        ) : Event

        object ShowTooManyStampsWarning : Event
    }
}
