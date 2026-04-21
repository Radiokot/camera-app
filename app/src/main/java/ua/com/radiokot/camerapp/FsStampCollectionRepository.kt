package ua.com.radiokot.camerapp

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File

class FsStampCollectionRepository(
    private val stampDirectory: File,
) : StampCollectionRepository {

    init {
        require(stampDirectory.exists()) {
            "Provided file doesn't exist: $stampDirectory"
        }

        require(stampDirectory.isDirectory) {
            "Provided file is not a directory: $stampDirectory"
        }

        require(stampDirectory.canRead()) {
            "Can't read the directory: $stampDirectory"
        }
    }

    override suspend fun getStampCollections(

    ): PersistentList<StampCollection> = withContext(Dispatchers.IO) {

        val directories =
            stampDirectory
                .listFiles { file, _ -> file.isDirectory }
                ?: error("Can't access the directory: $stampDirectory")

        return@withContext directories
            .map(File::toStampCollection)
            .toPersistentList()
    }

    private val sharedCacheFlow: MutableStateFlow<PersistentList<StampCollection>?> =
        MutableStateFlow(null)

    override fun getStampCollectionsFlow(): Flow<PersistentList<StampCollection>> =
        if (sharedCacheFlow.value != null)
            sharedCacheFlow.filterNotNull()
        else
            flow {
                sharedCacheFlow.value = getStampCollections()
                sharedCacheFlow
                    .filterNotNull()
                    .collect(this)
            }

    override suspend fun addStampCollection(

    ): Unit = withContext(Dispatchers.IO) {

        val id = System.currentTimeMillis().toString()
        val directory = getStampCollectionDirectory(
            id = id,
        )
        directory.mkdirs()

        sharedCacheFlow.update {
            it?.add(
                StampCollection(
                    id = id,
                )
            )
        }
    }

    override suspend fun deleteStampCollection(
        collection: StampCollection,
    ): Unit = withContext(Dispatchers.IO) {

        val directory = getStampCollectionDirectory(
            id = collection.id
        )

        if (directory.exists()) {
            directory.deleteRecursively()
        }

        sharedCacheFlow.update { it?.remove(collection) }
    }

    private fun getStampCollectionDirectory(
        id: String,
    ) = File(
        stampDirectory,
        id
    )
}

private fun File.toStampCollection(): StampCollection {
    return StampCollection(
        id = nameWithoutExtension,
    )
}
