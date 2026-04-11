package ua.com.radiokot.camerapp

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.runBlocking

@Immutable
class StampScreenViewModel(
    private val parameters: Parameters,
    private val stampRepository: StampRepository,
) : ViewModel() {

    private val stamp: Stamp = runBlocking {
        stampRepository.getStamp(parameters.stampId)
            ?: error("Stamp ${parameters.stampId} not found")
    }

    val stampId: String
        get() = stamp.id

    val thumbnailUrl: String
        get() = stamp.thumbnailUrl

    data class Parameters(
        val stampId: String,
    )
}
