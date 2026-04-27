package ua.com.radiokot.camerapp.stamps.domain

import kotlinx.coroutines.flow.first
import ua.com.radiokot.camerapp.util.lazyLogger

class EnsurePrimaryStampCollectionUseCase(
    private val collectionRepository: StampCollectionRepository,
) {
    private val log by lazyLogger("EnsurePrimaryStampCollectionUC")

    suspend operator fun invoke() {
        val primaryCollectionExists =
            collectionRepository
                .getStampCollectionsFlow()
                .first()
                .any(StampCollection::isPrimary)

        if (!primaryCollectionExists) {
            log.debug {
                "invoke(): creating the primary collection"
            }

            collectionRepository.addStampCollection(
                id = StampCollection.PRIMARY_ID,
                name = "My stamps",
            )

            log.info {
                "Primary collection created"
            }
        } else {
            log.debug {
                "invoke(): primary collection exists"
            }
        }
    }
}
