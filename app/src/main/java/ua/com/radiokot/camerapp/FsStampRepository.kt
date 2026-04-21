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
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Optional
import kotlin.io.path.absolutePathString
import kotlin.jvm.optionals.getOrNull

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

    override suspend fun getStamps(): PersistentList<Stamp> = withContext(Dispatchers.IO) {

        val files =
            stampDirectory
                .listFiles { file ->
                    file.extension.lowercase() in EXTENSIONS
                }
                ?: error("Can't access the directory: $stampDirectory")

        return@withContext files
            .map(File::toStamp)
            .toPersistentList()
    }

    private val sharedCacheFlow: MutableStateFlow<PersistentList<Stamp>?> =
        MutableStateFlow(null)

    override fun getStampsFlow(): Flow<PersistentList<Stamp>> =
        if (sharedCacheFlow.value != null)
            sharedCacheFlow.filterNotNull()
        else
            flow {
                sharedCacheFlow.value = getStamps()
                sharedCacheFlow
                    .filterNotNull()
                    .collect(this)
            }

    override suspend fun getStamp(
        id: String,
    ): Stamp? = withContext(Dispatchers.IO) {

        val cached =
            sharedCacheFlow
                .value
                ?.find { it.id == id }

        if (cached != null) {
            return@withContext cached
        }

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

        return@withContext files.first().toStamp()
    }

    override suspend fun addStamp(
        imageBitmap: Bitmap,
        caption: String?,
    ): Unit = withContext(Dispatchers.IO) {

        val id = System.currentTimeMillis().toString()
        val takenAtLocal = LocalDateTime.now()

        val outputFile = getStampFile(
            id = id,
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

        val xmpMeta = XMPMetaFactory.create().setStampDetails(
            caption = caption,
            takenAtLocal = takenAtLocal,
        )

        WebPWriter.writeImage(
            byteReader = ByteArrayByteReader(webpBytes),
            byteWriter = OutputStreamByteWriter(
                FileOutputStream(outputFile)
            ),
            xmp = XMPMetaFactory.serializeToString(xmpMeta),
            exifBytes = null,
        )

        sharedCacheFlow.update {
            it?.add(
                Stamp(
                    id = id,
                    caption = caption,
                    imageUri = outputFile.toPath().toImageUri(),
                    takenAtLocal = takenAtLocal,
                    isReadOnly = false,
                )
            )
        }
    }

    override suspend fun updateStamp(
        stamp: Stamp,
        newCaption: Optional<String>?,
    ) = withContext(Dispatchers.IO) {

        val file = getStampFile(
            id = stamp.id,
        )
        val captionToSet =
            if (newCaption != null)
                newCaption.getOrNull()
            else
                stamp.caption

        val webpChunks =
            WebPImageParser
                .readChunks(
                    AndroidInputStreamByteReader(
                        inputStream = file.inputStream().buffered(),
                        contentLength = file.length(),
                    )
                )

        val xmpMeta =
            WebPImageParser
                .parseMetadataFromChunks(webpChunks)
                .xmp
                ?.let(XMPMetaFactory::parseFromString)
                ?: XMPMetaFactory.create()
        xmpMeta.setStampDetails(
            caption = captionToSet,
            takenAtLocal = stamp.takenAtLocal,
        )

        WebPWriter
            .writeImage(
                chunks = webpChunks,
                byteWriter = OutputStreamByteWriter(
                    FileOutputStream(file)
                ),
                xmp = XMPMetaFactory.serializeToString(xmpMeta),
                exifBytes = null,
            )

        val updatedStamp = stamp.copy(
            newCaption = captionToSet,
        )

        sharedCacheFlow.update { it?.remove(stamp) }
        sharedCacheFlow.update { it?.add(updatedStamp) }
    }

    override suspend fun deleteStamp(
        stamp: Stamp,
    ): Unit = withContext(Dispatchers.IO) {

        val file = getStampFile(
            id = stamp.id,
        )

        if (file.exists()) {
            file.delete()
        }

        sharedCacheFlow.update { it?.remove(stamp) }
    }

    private fun getStampFile(
        id: String,
    ) = File(
        stampDirectory,
        "$id.$EXTENSION_WEBP"
    )

    private companion object {
        private const val EXTENSION_WEBP = "webp"
        private val EXTENSIONS = setOf(
            EXTENSION_WEBP,
        )
    }
}

private fun XMPMeta.setStampDetails(
    caption: String?,
    takenAtLocal: LocalDateTime,
) = apply {
    setTitle(caption)
    setDateTimeOriginal(takenAtLocal.toString())
}

private fun File.toStamp(): Stamp {
    val path = toPath()
    val xmpMeta: XMPMeta? =
        WebPImageParser
            .parseMetadata(
                AndroidInputStreamByteReader(
                    inputStream = inputStream().buffered(),
                    contentLength = length(),
                )
            )
            .xmp
            ?.let(XMPMetaFactory::parseFromString)

    return Stamp(
        id = nameWithoutExtension,
        imageUri = path.toImageUri(),
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
                    .toLocalDateTime(),
        isReadOnly = !canWrite(),
    )
}

private fun Path.toImageUri(): String =
    "file://${absolutePathString()}"
