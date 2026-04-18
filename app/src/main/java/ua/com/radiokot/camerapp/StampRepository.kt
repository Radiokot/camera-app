package ua.com.radiokot.camerapp

import android.graphics.Bitmap

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
}
