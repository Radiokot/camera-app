package ua.com.radiokot.camerapp

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
class StampsScreenViewModel(
    stampRepository: StampRepository,
) : ViewModel() {

    val stamps: StateFlow<ImmutableList<StampListItem>> =
        stampRepository
            .getStampsFlow()
            .map { stamps ->
                stamps
                    .sortedByDescending(Stamp::takenAtLocal)
                    .map(::StampListItem)
                    .toPersistentList()
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.Eagerly, persistentListOf())

    private val _events: MutableSharedFlow<Event> = eventSharedFlow()
    val events: SharedFlow<Event> = _events

    fun onStampClicked(
        item: StampListItem,
    ) {
        _events.tryEmit(
            Event.ProceedToStamp(
                stampId = item.key.toString(),
            )
        )
    }

    sealed interface Event {
        class ProceedToStamp(
            val stampId: String,
        ) : Event
    }
}
