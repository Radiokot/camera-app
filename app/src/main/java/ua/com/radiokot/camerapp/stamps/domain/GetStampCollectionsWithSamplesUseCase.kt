@file:OptIn(ExperimentalCoroutinesApi::class)

package ua.com.radiokot.camerapp.stamps.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import ua.com.radiokot.camerapp.stamps.domain.StampCollectionWithSamples

class GetStampCollectionsWithSamplesUseCase(
    private val collectionRepository: StampCollectionRepository,
    private val stampRepository: StampRepository,
) {
    operator fun invoke(): Flow<List<StampCollectionWithSamples>> =
        collectionRepository
            .getStampCollectionsFlow()
            .flatMapLatest { collections ->
                stampRepository
                    .getStampsFlow()
                    .map { allStamps ->
                        allStamps
                            .groupBy(Stamp::collectionId)
                            .mapValues { (_, collectionStamps) ->
                                collectionStamps
                                    .sortedByDescending(Stamp::takenAtLocal)
                                    .take(3)
                            }
                    }
                    .distinctUntilChanged()
                    .map { samplesByCollectionId ->
                        collections.map { collection ->
                            StampCollectionWithSamples(
                                collection = collection,
                                samples = samplesByCollectionId[collection.id] ?: emptyList(),
                            )
                        }
                    }
            }
}
