package ua.com.radiokot.camerapp.stamps.ui

import androidx.compose.runtime.Immutable
import ua.com.radiokot.camerapp.stamps.domain.Stamp

@Immutable
data class StampListItem(
    val thumbnailUrl: String,
    val key: String,
) {
    constructor(source: Stamp) : this(
        thumbnailUrl = source.imageUri,
        key = source.id,
    )
}
