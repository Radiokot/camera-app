package ua.com.radiokot.camerapp

import android.graphics.Bitmap
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
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
        val outputFile = File(
            stampDirectory,
            "$id.$EXTENSION_WEBP"
        )

        FileOutputStream(outputFile).use { stream ->
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
        }

        // TODO append caption
    }

    private fun toStamp(file: File): Stamp {
        val path = file.toPath()
        val attributes = Files.readAttributes(path, BasicFileAttributes::class.java)

        return Stamp(
            id = file.nameWithoutExtension,
            imageUri = "file://${path.absolutePathString()}",
            // TODO read from the metadata
            caption = null,
            takenAtLocal =
                attributes
                    .creationTime()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
        )
    }

    private companion object {
        private const val EXTENSION_WEBP = "webp"
        private val EXTENSIONS = setOf(
            EXTENSION_WEBP,
        )
    }
}
