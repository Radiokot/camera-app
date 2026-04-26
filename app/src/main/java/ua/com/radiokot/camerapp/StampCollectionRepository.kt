package ua.com.radiokot.camerapp

import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.Flow

interface StampCollectionRepository {

    suspend fun getStampCollections(): PersistentList<StampCollection>

    fun getStampCollectionsFlow(): Flow<PersistentList<StampCollection>>

    suspend fun addStampCollection(
        name: String,
    )

    suspend fun deleteStampCollection(
        collection: StampCollection,
    )
}
