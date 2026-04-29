package ua.com.radiokot.camerapp.stamps.ui

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import ua.com.radiokot.camerapp.stamps.domain.StampCollectionWithSamples

@Immutable
data class CollectionListItem(
    val name: String,
    val someStamps: ImmutableList<StampSampleItem>,
    val key: String,
) {
    constructor(
        collectionWithSamples: StampCollectionWithSamples,
    ) : this(
        name = collectionWithSamples.collection.name,
        someStamps =
            collectionWithSamples.samples
                .map { stamp ->
                    StampSampleItem(
                        imageUri = stamp.imageUri,
                        key = stamp.id,
                    )
                }
                .toPersistentList(),
        key = collectionWithSamples.collection.id,
    )

    @Immutable
    data class StampSampleItem(
        val imageUri: String,
        val key: String,
    )
}
