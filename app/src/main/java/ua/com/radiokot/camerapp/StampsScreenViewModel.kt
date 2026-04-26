package ua.com.radiokot.camerapp

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

@Immutable
class StampsScreenViewModel(
    stampRepository: StampRepository,
    parameters: Parameters,
) : ViewModel() {

    val collectionId: String = parameters.collectionId
    val stamps: StateFlow<ImmutableList<StampListItem>> = runBlocking {
        stampRepository
            .getStampsFlow()
            .map { stamps ->
                stamps
                    .filter { it.collectionId == collectionId }
                    .sortedByDescending(Stamp::takenAtLocal)
                    .map(::StampListItem)
                    .toPersistentList()
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope)
    }

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

    data class Parameters(
        val collectionId: String,
    )
}
