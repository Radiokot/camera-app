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

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.toIntSize
import androidx.compose.ui.unit.toSize
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import ua.com.radiokot.camerapp.BuildConfig
import ua.com.radiokot.camerapp.posters.domain.CreateSendStampPosterIntent
import ua.com.radiokot.camerapp.posters.domain.StampPosterDensity
import ua.com.radiokot.camerapp.posters.domain.StampPosterLayer
import ua.com.radiokot.camerapp.posters.domain.StampPosterSize
import ua.com.radiokot.camerapp.posters.domain.drawStampPoster
import java.io.File
import java.io.OutputStream
import kotlin.io.outputStream
import kotlin.use

class CpCreateSendStampPosterIntent(
    private val context: Context,
    private val stampPosterProviderDirectory: File,
) : CreateSendStampPosterIntent {

    override suspend fun invoke(
        layers: Collection<StampPosterLayer>,
        isDark: Boolean,
    ): Intent {

        if (!stampPosterProviderDirectory.exists()) {
            stampPosterProviderDirectory.mkdirs()
        }

        val posterFile = File(stampPosterProviderDirectory, "poster.png")

        posterFile.outputStream().use { outputStream ->
            renderPosterAsPng(
                layers = layers,
                isDark = isDark,
                outputStream = outputStream,
            )
        }

        val uri = FileProvider.getUriForFile(
            context,
            BuildConfig.stampPosterContentProviderAuthority,
            posterFile,
            posterFile.name,
        )

        return Intent(Intent.ACTION_SEND)
            .setDataAndType(uri, "image/png")
            .putExtra(Intent.EXTRA_STREAM, uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    private suspend fun renderPosterAsPng(
        layers: Collection<StampPosterLayer>,
        isDark: Boolean,
        outputStream: OutputStream,
    ) = withContext(Dispatchers.IO) {

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
                    layers = layers,
                    isDark = isDark,
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

    private suspend fun ImageReader.awaitImage(

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
}
