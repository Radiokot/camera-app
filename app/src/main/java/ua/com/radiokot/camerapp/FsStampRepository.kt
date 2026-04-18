package ua.com.radiokot.camerapp

import android.graphics.Bitmap
import android.os.Build
import com.ashampoo.kim.format.webp.WebPImageParser
import com.ashampoo.kim.format.webp.WebPWriter
import com.ashampoo.kim.input.AndroidInputStreamByteReader
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.output.OutputStreamByteWriter
import com.ashampoo.xmp.XMPMeta
import com.ashampoo.xmp.XMPMetaFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.io.path.absolutePathString

class FsStampRepository(
    private val stampDirectory: File,
) : StampRepository {

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

    override suspend fun getStamps(
        asc: Boolean,
    ): List<Stamp> = withContext(Dispatchers.IO) {

        val files =
            stampDirectory
                .listFiles { file ->
                    file.extension.lowercase() in EXTENSIONS
                }
                ?: error("Can't access the directory: $stampDirectory")

        return@withContext files
            .map(::toStamp)
            .sortedWith { a, b ->
                if (asc)
                    a.takenAtLocal.compareTo(b.takenAtLocal)
                else
                    b.takenAtLocal.compareTo(a.takenAtLocal)
            }
    }

    override suspend fun getStamp(
        id: String,
    ): Stamp? = withContext(Dispatchers.IO) {

        val files =
            stampDirectory
                .listFiles { file ->
                    file.nameWithoutExtension == id
                            && file.extension.lowercase() in EXTENSIONS
                }
                ?: error("Can't access the directory: $stampDirectory")

        if (files.isEmpty()) {
            return@withContext null
        }

        return@withContext toStamp(files.first())
    }

    override suspend fun addStamp(
        imageBitmap: Bitmap,
        caption: String?,
    ): Unit = withContext(Dispatchers.IO) {

        val id = System.currentTimeMillis().toString()
        val takenAtLocal = LocalDateTime.now()

        val outputFile = File(
            stampDirectory,
            "$id.$EXTENSION_WEBP"
        )

        val webpBytes = ByteArrayOutputStream().use { stream ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                imageBitmap.compress(
                    Bitmap.CompressFormat.WEBP_LOSSY,
                    100,
                    stream,
                )
            } else {
                imageBitmap.compress(
                    Bitmap.CompressFormat.WEBP,
                    100,
                    stream,
                )
            }

            stream.flush()
            stream.toByteArray()
        }

        val xmpMeta = XMPMetaFactory.create().apply {
            setTitle(caption)
            setDateTimeOriginal(takenAtLocal.toString())
        }

        WebPWriter.writeImage(
            byteReader = ByteArrayByteReader(webpBytes),
            byteWriter = OutputStreamByteWriter(
                FileOutputStream(outputFile)
            ),
            xmp = XMPMetaFactory.serializeToString(xmpMeta),
            exifBytes = null,
        )
    }

    private fun toStamp(
        file: File,
    ): Stamp {

        val path = file.toPath()
        val xmpMeta: XMPMeta? =
            WebPImageParser
                .parseMetadata(
                    AndroidInputStreamByteReader(
                        inputStream = file.inputStream().buffered(),
                        contentLength = file.length(),
                    )
                )
                .xmp
                ?.let(XMPMetaFactory::parseFromString)

        return Stamp(
            id = file.nameWithoutExtension,
            imageUri = "file://${path.absolutePathString()}",
            caption = xmpMeta?.getTitle(),
            takenAtLocal =
                xmpMeta
                    ?.getDateTimeOriginal()
                    ?.let(LocalDateTime::parse)
                    ?: Files
                        .readAttributes(path, BasicFileAttributes::class.java)
                        .creationTime()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
        )
    }

    private companion object {
        private const val EXTENSION_WEBP = "webp"
        private val EXTENSIONS = setOf(
            EXTENSION_WEBP,
        )
    }
}
