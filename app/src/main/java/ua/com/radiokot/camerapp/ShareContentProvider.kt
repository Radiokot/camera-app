package ua.com.radiokot.camerapp

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.FileNotFoundException

class ShareContentProvider : ContentProvider() {

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(
            WEBP_URI.authority!!,
            WEBP_URI!!.path!!,
            MATCH_WEBP,
        )
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        when (uriMatcher.match(uri)) {
            MATCH_WEBP -> {
                val (pipeOutput, pipeInput) = ParcelFileDescriptor.createPipe()
                ParcelFileDescriptor.AutoCloseOutputStream(pipeInput).use { stream ->
                    stream.write(SHARED_WEBP)
                }
                return pipeOutput
            }

            else ->
                throw FileNotFoundException("URI '$uri' not matched")
        }

    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            MATCH_WEBP ->
                WEBP_CONTENT_TYPE

            else ->
                throw FileNotFoundException("URI '$uri' not matched")
        }
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String?>?,
    ): Int = 0

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? = null

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String?>?,
        selection: String?,
        selectionArgs: Array<out String?>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String?>?,
    ): Int = 0

    companion object {
        val WEBP_URI = Uri.Builder()
            .scheme("content")
            .authority("ua.com.radiokot.camerapp.shared")
            .path("content.webp")
            .appendQueryParameter("displayName", "content.webp")
            .build()
        val WEBP_CONTENT_TYPE = "image/webp"

        var SHARED_WEBP = byteArrayOf()
        private const val MATCH_WEBP = 1
    }
}
