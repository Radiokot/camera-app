package ua.com.radiokot.camerapp.stamps.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import ua.com.radiokot.camerapp.stamps.domain.GetStampCollectionsWithSamplesUseCase
import ua.com.radiokot.camerapp.util.lazyLogger

class CollectionActionsScreenViewModel(
    getStampCollectionsWithSamplesUseCase: GetStampCollectionsWithSamplesUseCase,
    parameters: Parameters,
) : ViewModel() {

    private val log by lazyLogger("CollectionActionsScreenVM")

    val collection: CollectionListItem = runBlocking {
        getStampCollectionsWithSamplesUseCase(
            singleCollectionId = parameters.collectionId,
        )
            .first()
            .find { it.collection.id == parameters.collectionId }
            ?.let(::CollectionListItem)
            ?: error("Stamp collection ${parameters.collectionId} not found")
    }

    data class Parameters(
        val collectionId: String,
    )
}
