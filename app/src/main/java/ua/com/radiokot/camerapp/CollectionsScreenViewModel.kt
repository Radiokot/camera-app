package ua.com.radiokot.camerapp

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

class CollectionsScreenViewModel(
    private val collectionRepository: StampCollectionRepository,
    private val getStampCollectionsWithSamplesUseCase: GetStampCollectionsWithSamplesUseCase,
) : ViewModel() {

    private val _events: MutableSharedFlow<Event> = eventSharedFlow()
    val events: SharedFlow<Event> = _events

    val items: StateFlow<ImmutableList<CollectionListItem>> = runBlocking {
        getStampCollectionsWithSamplesUseCase()
            .map { collectionsWithSamples ->
                collectionsWithSamples
                    .map { (collection, samples) ->
                        CollectionListItem(
                            name = collection.name,
                            someStamps =
                                samples
                                    .map { stamp ->
                                        CollectionListItem.StampSampleItem(
                                            imageUri = stamp.imageUri,
                                            key = stamp.id,
                                        )
                                    }
                                    .toPersistentList(),
                            key = collection.id,
                        )
                    }
                    .toPersistentList()
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope)
    }

    fun onItemClicked(
        item: CollectionListItem,
    ) {
        _events.tryEmit(
            Event.ProceedToCollection(
                collectionId = item.key,
            )
        )
    }

    sealed interface Event {
        class ProceedToCollection(
            val collectionId: String,
        ) : Event
    }
}
