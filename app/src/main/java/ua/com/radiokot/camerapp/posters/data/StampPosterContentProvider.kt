/* Copyright 2026 Oleg Koretsky

   This file is part of the Press-Cut,
   a digital postage stamp cutter Android app.

   Press-Cut is free software: you can redistribute it
   and/or modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation, either version 3 of the License,
   or (at your option) any later version.

   Press-Cut is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   See the GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Press-Cut. If not, see <http://www.gnu.org/licenses/>.
*/

package ua.com.radiokot.camerapp.posters.data

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.toIntSize
import androidx.compose.ui.unit.toSize
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import ua.com.radiokot.camerapp.BuildConfig
import ua.com.radiokot.camerapp.posters.domain.SendStampPosterOptions
import ua.com.radiokot.camerapp.posters.domain.StampPosterDensity
import ua.com.radiokot.camerapp.posters.domain.StampPosterSize
import ua.com.radiokot.camerapp.posters.domain.drawStampPoster
import ua.com.radiokot.camerapp.util.MatrixCursor
import ua.com.radiokot.camerapp.util.lazyLogger
import ua.com.radiokot.camerapp.util.openPipeHelper
import java.io.OutputStream
import kotlin.time.measureTime

class StampPosterContentProvider :
    ContentProvider(),
    KoinComponent {

    private val log by lazyLogger("StampPosterCP")

    override fun onCreate(): Boolean {
        return true
    }

    override fun openFile(
        uri: Uri,
        mode: String,
    ): ParcelFileDescriptor {

        val options = optionsByUri[uri]
            ?: error("Requested URI is not provided")

        return openPipeHelper { outputStream ->
            val elapsed = measureTime {
                createAndSendPoster(
                    options = options,
                    outputStream = outputStream,
                )
            }

            log.debug {
                "openFile(): poster sent:" +
                        "\ntook=$elapsed"
            }
        }
    }

    private suspend fun createAndSendPoster(
        options: SendStampPosterOptions,
        outputStream: OutputStream,
    ) = withContext(Dispatchers.Default) {

        val size = StampPosterSize.toIntSize()

        val imageReader = ImageReader.newInstance(
            size.width,
            size.height,
            PixelFormat.RGBA_8888,
            2,
        )

        val rawBitmap = imageReader.use { imageReader ->
            val hardwareCanvas = imageReader.surface.lockHardwareCanvas()

            CanvasDrawScope().draw(
                density = StampPosterDensity,
                layoutDirection = LayoutDirection.Ltr,
                canvas = Canvas(hardwareCanvas),
                size = size.toSize(),
            ) {
                drawStampPoster(
                    layers = options.layers,
                    isDark = options.isDark,
                )
            }

            imageReader.surface.unlockCanvasAndPost(hardwareCanvas)

            val image = imageReader.awaitImage()
            val imagePlane = image.planes.first()

            createBitmap(
                width = imagePlane.rowStride / imagePlane.pixelStride,
                height = size.height,
                config = Bitmap.Config.ARGB_8888,
            ).apply {
                copyPixelsFromBuffer(imagePlane.buffer)
            }
        }

        val resultBitmap = createBitmap(
            width = size.width,
            height = size.height,
            config = Bitmap.Config.RGB_565,
        )
        val resultRect =
            Rect(0, 0, resultBitmap.width, resultBitmap.height)
        android.graphics.Canvas(resultBitmap).drawBitmap(
            rawBitmap,
            resultRect,
            resultRect,
            null,
        )
        rawBitmap.recycle()

        resultBitmap.compress(
            Bitmap.CompressFormat.PNG,
            100,
            outputStream,
        )
        resultBitmap.recycle()
    }

    suspend fun ImageReader.awaitImage(

    ): Image = suspendCancellableCoroutine { continuation ->

        val handler = Handler(Looper.getMainLooper())

        setOnImageAvailableListener({
            setOnImageAvailableListener(null, null)
            continuation.resume(acquireLatestImage()!!) { _, image, _ ->
                image.close()
            }
        }, handler)

        continuation.invokeOnCancellation {
            setOnImageAvailableListener(null, null)
        }
    }

    override fun getType(
        uri: Uri,
    ): String =
        POSTER_FILE_CONTENT_TYPE

    override fun query(
        uri: Uri,
        projection: Array<out String?>?,
        selection: String?,
        selectionArgs: Array<out String?>?,
        sortOrder: String?,
    ): Cursor {

        val options = optionsByUri[uri]
            ?: error("Requested URI is not provided")

        return MatrixCursor(
            valuesByColumnName = mapOf(
                OpenableColumns.DISPLAY_NAME to getPosterFileName(options),
                // Gmail WANTS this!
                // It is actually shown in the attachment section.
                // Doesn't need to be the exact size though.
                OpenableColumns.SIZE to 500 * 1024,
            ),
        )
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String?>?): Int =
        error("Deletions are not allowed")

    override fun insert(p0: Uri, p1: ContentValues?): Uri =
        error("Inserts are not allowed")

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String?>?): Int =
        error("Updates are not allowed")

    companion object {
        const val AUTHORITY = BuildConfig.stampPosterContentProviderAuthority
        const val POSTER_FILE_CONTENT_TYPE = "image/png"
        private const val PNG_EXTENSION = "png"

        private val optionsByUri = mutableMapOf<Uri, SendStampPosterOptions>()

        fun provide(
            options: SendStampPosterOptions,
        ): Uri {
            val uri = "content://$AUTHORITY/${getPosterFileName(options)}".toUri()
            optionsByUri[uri] = options
            return uri
        }

        private fun getPosterFileName(
            options: SendStampPosterOptions,
        ): String =
            "${options.id}.$PNG_EXTENSION"
    }
}
