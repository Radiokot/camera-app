package ua.com.radiokot.camerapp.stamps.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ua.com.radiokot.camerapp.stamps.domain.GetStampCollectionsWithSamplesUseCase
import ua.com.radiokot.camerapp.stamps.domain.StampCollectionRepository
import ua.com.radiokot.camerapp.stamps.domain.StampCollectionWithSamples
import ua.com.radiokot.camerapp.util.eventSharedFlow
import ua.com.radiokot.camerapp.util.lazyLogger
import java.text.Collator
import java.util.Locale

class CollectionsScreenViewModel(
    private val collectionRepository: StampCollectionRepository,
    private val getStampCollectionsWithSamplesUseCase: GetStampCollectionsWithSamplesUseCase,
) : ViewModel() {

    private val log by lazyLogger("CollectionsScreenVM")

    private val _events: MutableSharedFlow<Event> = eventSharedFlow()
    val events: SharedFlow<Event> = _events

    // First goes the primary collection, then other collections
    // sorted alphabetically by name.
    private val collator = Collator.getInstance(Locale.ROOT)
    private val comparator: Comparator<StampCollectionWithSamples> =
        compareByDescending<StampCollectionWithSamples> { it.collection.isPrimary }
            .then { a, b -> collator.compare(a.collection.name, b.collection.name) }

    val items: StateFlow<ImmutableList<CollectionListItem>> = runBlocking {
        getStampCollectionsWithSamplesUseCase()
            .map { collectionsWithSamples ->
                collectionsWithSamples
                    .sortedWith(comparator)
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
        val collectionId = item.key

        log.debug {
            "onItemClicked(): proceeding to collection:" +
                    "\ncollectionId = $collectionId"
        }

        _events.tryEmit(
            Event.ProceedToCollection(
                collectionId = collectionId,
            )
        )
    }

    fun onNewStampAction() {
        log.debug {
            "onNewStampAction(): proceeding to new stamp creation"
        }

        _events.tryEmit(Event.ProceedToNewStamp)
    }

    private var addCollectionJob: Job? = null
    fun onNewCollectionAction() {
        if (addCollectionJob?.isActive == true) {
            return
        }

        addCollectionJob = viewModelScope.launch {
            addCollection()
        }
    }

    private suspend fun addCollection() {
        log.debug {
            "addCollection(): adding new collection"
        }

        val addedCollectionId =
            collectionRepository.addStampCollection(
                name = "New Collection",
            )

        log.debug {
            "addCollection(): proceeding to the newly added collection:" +
                    "\naddedCollectionId=$addedCollectionId"
        }
        log.info {
            "Added a collection $addedCollectionId"
        }

        _events.emit(
            Event.ProceedToCollection(
                collectionId = addedCollectionId,
            )
        )
    }

    sealed interface Event {
        class ProceedToCollection(
            val collectionId: String,
        ) : Event

        object ProceedToNewStamp : Event
    }
}
