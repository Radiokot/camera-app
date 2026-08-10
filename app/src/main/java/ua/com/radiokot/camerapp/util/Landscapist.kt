package ua.com.radiokot.camerapp.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.skydoves.landscapist.components.ImagePluginComponent
import com.skydoves.landscapist.core.ImageRequest
import com.skydoves.landscapist.core.Landscapist
import com.skydoves.landscapist.core.LandscapistConfig
import com.skydoves.landscapist.core.decoder.DecodeResult
import com.skydoves.landscapist.core.decoder.ImageDecoder
import com.skydoves.landscapist.core.model.DataSource
import com.skydoves.landscapist.core.network.FetchResult
import com.skydoves.landscapist.core.network.ImageFetcher
import java.io.File
import kotlin.math.max

val EmptyImageComponent = ImagePluginComponent()

private fun interface FileDecoder {
    operator fun invoke(
        filePath: String,
        targetWidth: Int?,
        targetHeight: Int?,
    ): Bitmap
}

@RequiresApi(Build.VERSION_CODES.P)
private class ImageDecoderFileDecoder : FileDecoder {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun invoke(
        filePath: String,
        targetWidth: Int?,
        targetHeight: Int?,
    ): Bitmap {
        val source = android.graphics.ImageDecoder.createSource(File(filePath))
        return android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_HARDWARE
            if (targetWidth != null && targetHeight != null) {
                decoder.setTargetSize(targetWidth, targetHeight)
            }
        }
    }
}

private class BitmapFactoryFileDecoder : FileDecoder {

    override fun invoke(
        filePath: String,
        targetWidth: Int?,
        targetHeight: Int?,
    ): Bitmap {
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(filePath, boundsOptions)

        val originalWidth = boundsOptions.outWidth
        val originalHeight = boundsOptions.outHeight

        if (originalWidth <= 0 || originalHeight <= 0) {
            error("Failed to decode image dimensions")
        }

        val sampleSize = calculateSampleSize(
            originalWidth = originalWidth,
            originalHeight = originalHeight,
            targetWidth = targetWidth ?: originalWidth,
            targetHeight = targetHeight ?: originalHeight,
        )

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.HARDWARE
        }

        return BitmapFactory.decodeFile(filePath, decodeOptions)
    }

    private fun calculateSampleSize(
        originalWidth: Int,
        originalHeight: Int,
        targetWidth: Int,
        targetHeight: Int,
    ): Int {
        var sampleSize = 1

        while (
            originalWidth / (sampleSize * 2) >= targetWidth &&
            originalHeight / (sampleSize * 2) >= targetHeight
        ) {
            sampleSize *= 2
        }

        return max(1, sampleSize)
    }
}

class FileUriDecodingImageFetcher : ImageFetcher {

    private val fileDecoder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoderFileDecoder()
        } else {
            BitmapFactoryFileDecoder()
        }

    override suspend fun fetch(
        request: ImageRequest,
    ): FetchResult {
        val uriPath = (request.model as Uri).path
            ?: return FetchResult.Error(
                IllegalStateException("The URI has no path")
            )

        val decoded = try {
            fileDecoder(
                filePath = uriPath,
                targetWidth = request.targetWidth,
                targetHeight = request.targetHeight,
            )
        } catch (e: Exception) {
            return FetchResult.Error(e)
        }

        return FetchResult.Decoded(
            image = decoded,
            width = decoded.width,
            height = decoded.height,
            dataSource = DataSource.DISK,
        )
    }

    override fun canHandle(model: Any?): Boolean {
        return model is Uri && model.scheme == ContentResolver.SCHEME_FILE
    }
}

class NoOpImageDecoder : ImageDecoder {
    override suspend fun decode(
        data: ByteArray,
        mimeType: String?,
        targetWidth: Int?,
        targetHeight: Int?,
        config: LandscapistConfig,
    ): DecodeResult {
        return DecodeResult.Error(
            IllegalStateException("No-op decoder can't decode")
        )
    }
}

fun createLandscapistForPreview() =
    Landscapist.Builder()
        .fetcher(FileUriDecodingImageFetcher())
        .decoder(NoOpImageDecoder())
        .build()
