package ua.com.radiokot.camerapp.stamps.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.text.Collator
import java.util.Locale

class GetSortedStampCollectionsUseCase(
    private val collectionRepository: StampCollectionRepository,
    private val ensurePrimaryStampCollectionUseCase: EnsurePrimaryStampCollectionUseCase,
) {
    // First goes the primary collection, then other collections
    // sorted alphabetically by name.
    private val collator = Collator.getInstance(Locale.ROOT)
    private val comparator: Comparator<StampCollection> =
        compareByDescending(StampCollection::isPrimary)
            .then { a, b -> collator.compare(a.name, b.name) }

    operator fun invoke(): Flow<List<StampCollection>> = flow {

        ensurePrimaryStampCollectionUseCase()

        collectionRepository
            .getStampCollectionsFlow()
            .map { collections ->
                collections.sortedWith(comparator)
            }
            .collect(this)
    }
}
