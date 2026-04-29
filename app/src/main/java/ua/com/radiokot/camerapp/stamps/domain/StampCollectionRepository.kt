package ua.com.radiokot.camerapp.stamps.domain

import kotlinx.coroutines.flow.Flow

interface StampCollectionRepository {

    suspend fun getStampCollections(): List<StampCollection>

    fun getStampCollectionsFlow(): Flow<List<StampCollection>>

    suspend fun getStampCollection(
        collectionId: String,
    ): StampCollection?

    /**
     * @return added collection ID
     */
    suspend fun addStampCollection(
        id: String = System.currentTimeMillis().toString(),
        name: String,
    ): String

    suspend fun deleteStampCollection(
        collection: StampCollection,
    )

    suspend fun updateStampCollection(
        collection: StampCollection,
        newName: String?,
    )
}
