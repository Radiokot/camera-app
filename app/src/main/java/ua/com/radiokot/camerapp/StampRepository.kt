package ua.com.radiokot.camerapp

import android.graphics.Bitmap
import java.util.Optional

interface StampRepository {

    suspend fun getStamps(
        asc: Boolean,
    ): List<Stamp>

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
}
