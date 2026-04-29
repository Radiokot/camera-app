package ua.com.radiokot.camerapp.stamps.ui

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
import ua.com.radiokot.camerapp.stamps.domain.Stamp
import ua.com.radiokot.camerapp.stamps.domain.StampCollectionRepository
import ua.com.radiokot.camerapp.stamps.domain.StampRepository
import ua.com.radiokot.camerapp.util.eventSharedFlow
import ua.com.radiokot.camerapp.util.lazyLogger

@Immutable
class StampsScreenViewModel(
    stampRepository: StampRepository,
    collectionRepository: StampCollectionRepository,
    parameters: Parameters,
) : ViewModel() {

    private val log by lazyLogger("StampsScreenVM")

    private val collection = runBlocking {
        collectionRepository.getStampCollection(parameters.collectionId)
            ?: error("Stamp collection ${parameters.collectionId} not found")
    }
    val collectionId: String =
        collection.id
    val collectionName: String =
        collection.name
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
        val stampId = item.key

        log.debug {
            "onStampClicked(): proceeding to the stamp:" +
                    "\nstampId=$stampId"
        }

        _events.tryEmit(
            Event.ProceedToStamp(
                stampId = stampId,
            )
        )
    }

    fun onNewStampAction() {
        log.debug {
            "onNewStampAction(): proceeding to new stamp creation:" +
                    "\ncollectionId=$collectionId"
        }

        _events.tryEmit(
            Event.ProceedToNewStamp(
                collectionId = collectionId,
            )
        )
    }

    sealed interface Event {
        class ProceedToStamp(
            val stampId: String,
        ) : Event

        class ProceedToNewStamp(
            val collectionId: String,
        ) : Event
    }

    data class Parameters(
        val collectionId: String,
    )
}
