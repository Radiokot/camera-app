package ua.com.radiokot.camerapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CollectionsScreenViewModel(
    private val collectionRepository: StampCollectionRepository,
) : ViewModel() {

    val items: StateFlow<ImmutableList<CollectionListItem>> = runBlocking {
        collectionRepository
            .getStampCollectionsFlow()
            .map { collections ->
                collections
                    .map { collection ->
                        CollectionListItem(
                            key = collection.id,
                            name = collection.id,
                            someStamps = persistentListOf(),
                        )
                    }
                    .toPersistentList()
            }
            .stateIn(coroutineScopeThatCancelsWith(viewModelScope))
    }
}
