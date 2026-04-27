package ua.com.radiokot.camerapp.stamps.ui

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class CollectionListItem(
    val name: String,
    val someStamps: ImmutableList<StampSampleItem>,
    val key: String,
) {
    @Immutable
    data class StampSampleItem(
        val imageUri: String,
        val key: String,
    )
}
