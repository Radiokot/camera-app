package ua.com.radiokot.camerapp

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class CollectionListItem(
    val name: String,
    val someStamps: ImmutableList<StampSampleItem>,
    val key: String,
) {
    class StampSampleItem(
        val thumbnailUrl: String,
        val key: String,
    )
}
