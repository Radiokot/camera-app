package ua.com.radiokot.camerapp

data class StampListItem(
    val thumbnailUrl: String,
    val key: Any,
) {
    constructor(source: Stamp) : this(
        thumbnailUrl = source.imageUri,
        key = source.id,
    )
}
