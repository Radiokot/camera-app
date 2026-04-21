package ua.com.radiokot.camerapp

import android.graphics.Bitmap
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.Flow
import java.util.Optional

interface StampRepository {

    suspend fun getStamps(): PersistentList<Stamp>

    fun getStampsFlow(): Flow<PersistentList<Stamp>>

    suspend fun getStamp(
        id: String,
    ): Stamp?

    suspend fun addStamp(
        imageBitmap: Bitmap,
        caption: String?,
    )

    suspend fun updateStamp(
        stamp: Stamp,
        newCaption: Optional<String>?,
    )

    suspend fun deleteStamp(
        stamp: Stamp,
    )

    data class Parameters(
        val collectionId: String,
    )
}
