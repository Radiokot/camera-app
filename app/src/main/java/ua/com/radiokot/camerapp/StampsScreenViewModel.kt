package ua.com.radiokot.camerapp

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.stateIn

@Immutable
class StampsScreenViewModel(
    private val stampRepository: StampRepository,
) : ViewModel() {

    val stamps: StateFlow<ImmutableList<StampListItem>> =
        suspend {
            stampRepository
                .getStamps(
                    asc = false,
                )
                .map(::StampListItem)
                .toPersistentList()
        }
            .asFlow()
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
