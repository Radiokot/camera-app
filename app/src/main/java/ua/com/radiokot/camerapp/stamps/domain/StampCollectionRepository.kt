package ua.com.radiokot.camerapp.stamps.domain

import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.Flow

interface StampCollectionRepository {

    suspend fun getStampCollections(): PersistentList<StampCollection>

    fun getStampCollectionsFlow(): Flow<PersistentList<StampCollection>>

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
}
