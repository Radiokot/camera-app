package ua.com.radiokot.camerapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.absolutePathString
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
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
                    a.takenAt.compareTo(b.takenAt)
                else
                    b.takenAt.compareTo(a.takenAt)
            }
    }

    private fun toStamp(file: File): Stamp {
        val path = file.toPath()
        val attributes = Files.readAttributes(path, BasicFileAttributes::class.java)

        return Stamp(
            id = file.nameWithoutExtension,
            thumbnailUrl = "file://${path.absolutePathString()}",
            takenAt = Instant.fromEpochMilliseconds(
                attributes.creationTime().toMillis()
            ),
        )
    }

    private companion object {
        private val EXTENSIONS = setOf(
            "webp",
        )
    }
}
