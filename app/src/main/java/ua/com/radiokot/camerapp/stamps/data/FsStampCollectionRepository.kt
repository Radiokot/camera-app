package ua.com.radiokot.camerapp.stamps.data

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
import ua.com.radiokot.camerapp.stamps.domain.StampCollection
import ua.com.radiokot.camerapp.stamps.domain.StampCollectionRepository
import ua.com.radiokot.camerapp.util.lazyLogger
import java.io.File

class FsStampCollectionRepository(
    private val stampDirectory: File,
) : StampCollectionRepository {

    private val log by lazyLogger("FsStampCollectionRepo")

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

    override suspend fun getStampCollections(

    ): PersistentList<StampCollection> = withContext(Dispatchers.IO) {

        val directories =
            stampDirectory
                .listFiles(File::isDirectory)
                ?: error("Can't access the directory: $stampDirectory")

        return@withContext directories
            .mapNotNull(::toStampCollection)
            .toPersistentList()
    }

    private val sharedCacheFlow: MutableStateFlow<PersistentList<StampCollection>?> =
        MutableStateFlow(null)

    override fun getStampCollectionsFlow(): Flow<PersistentList<StampCollection>> =
        if (sharedCacheFlow.value != null)
            sharedCacheFlow.filterNotNull()
        else
            flow {
                sharedCacheFlow.value = getStampCollections()
                sharedCacheFlow
                    .filterNotNull()
                    .collect(this)
            }

    override suspend fun addStampCollection(
        id: String,
        name: String,
    ): Unit = withContext(Dispatchers.IO) {

        val directory = getStampCollectionDirectory(
            id = id,
        )
        directory.mkdirs()

        val xmpMeta = XMPMetaFactory.create().setCollectionDetails(
            name = name,
        )
        WebPWriter
            .writeImage(
                byteReader = ByteArrayByteReader(
                    bytes =
                        DETAILS_FILE_STUB_HEX
                            .trimIndent()
                            .replace("\n", "")
                            .hexToByteArray(
                                format = HexFormat.UpperCase,
                            ),
                ),
                byteWriter = OutputStreamByteWriter(
                    outputStream =
                        File(directory, DETAILS_FILE_NAME)
                            .outputStream(),
                ),
                xmp = XMPMetaFactory.serializeToString(xmpMeta),
                exifBytes = null,
            )

        sharedCacheFlow.update {
            it?.add(
                StampCollection(
                    id = id,
                    name = name,
                )
            )
        }
    }

    override suspend fun deleteStampCollection(
        collection: StampCollection,
    ): Unit = withContext(Dispatchers.IO) {

        val directory = getStampCollectionDirectory(
            id = collection.id
        )

        if (directory.exists()) {
            directory.deleteRecursively()
        }

        sharedCacheFlow.update { it?.remove(collection) }
    }

    private fun getStampCollectionDirectory(
        id: String,
    ) = File(
        stampDirectory,
        id
    )

    private fun toStampCollection(
        directory: File,
    ): StampCollection? = runCatching {
        val detailsFile = File(directory, DETAILS_FILE_NAME)
        val xmpMeta =
            WebPImageParser
                .parseMetadata(
                    AndroidInputStreamByteReader(
                        inputStream = detailsFile.inputStream().buffered(),
                        contentLength = detailsFile.length(),
                    )
                )
                .xmp
                ?.let(XMPMetaFactory::parseFromString)

        return StampCollection(
            id = directory.nameWithoutExtension,
            name = xmpMeta?.getTitle() ?: "…",
        )
    }
        .onFailure { error ->
            log.warn(error) {
                "toStampCollection(): failed creating a collection from a directory:" +
                        "\ndirectory=$directory"
            }
        }
        .getOrNull()

    private companion object {
        private const val DETAILS_FILE_NAME = "collection.webp"
        private const val DETAILS_FILE_STUB_HEX = """
            5249464676010000574542505650384C6A0100002F3FC00F100FF018F04BF51F
            057CF4E30172B4FD5324A5174DB9811FC2DD1A5232CF9C7E3AC3460A3B8143EA
            90BACEE01ABAF433550718F9E315FC9FFA7FD82C5788E8FF0430395F7369139A
            65E7A6AD56574DC88E1E7D7E5DD2DC6CCACAB7D628676659FEEA3AFA7D245BF8
            CEEEC8E7B1D9871D37F0972766A7FAFA531F4F3E5C54FE9E3C5F535CEF63FEC0
            899745AD8A9A57D7FD0D774FC6BDDB59D3BEB66614DB6308F76446C7DF733565
            E9A7FE4F255C9BD18ADC2B55F67D48F7C0DD59659640C2A4AB10B178F8BB6FE1
            A23FD403AAB6ACB94DBAE3349DEE0B6E635916D06E394DE7FBE09AC4C249BB41
            C7BF32A87DB258201DFBE1281C2411397C2F786990F21AF792440E49880EFC73
            A38DC2D91B21E224E50E0B0338CB8F7706618BDC53707CA5E8960ABD147D0276
            B87B3F8090A8FC2E6AFCB400F7406811814404087702FFD68A0E661518430D50
            1C7F87448A011280F17704106368FC87F2BF8A80314A1D325A19F26F1902
        """
    }
}

private fun XMPMeta.setCollectionDetails(
    name: String,
) = apply {
    setTitle(name)
}
