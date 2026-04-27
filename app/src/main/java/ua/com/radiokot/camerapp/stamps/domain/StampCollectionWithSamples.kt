package ua.com.radiokot.camerapp.stamps.domain

data class StampCollectionWithSamples(
    val collection: StampCollection,
    val samples: List<Stamp>,
)
