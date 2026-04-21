package ua.com.radiokot.camerapp

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.Optional

@Immutable
class StampScreenViewModel(
    private val parameters: Parameters,
    private val stampRepository: StampRepository,
) : ViewModel() {

    private val stamp: Stamp = runBlocking {
        stampRepository.getStamp(parameters.stampId)
            ?: error("Stamp ${parameters.stampId} not found")
    }

    val stampId: String by stamp::id
    val imageUri: String by stamp::imageUri
    val takenAt: LocalDate
        get() = stamp.takenAtLocal.toLocalDate()
    val isEditable: Boolean
        get() = !stamp.isReadOnly

    private val _caption: MutableStateFlow<String?> = MutableStateFlow(stamp.caption)
    val caption: StateFlow<String?> = _caption
    private val _isCaptionInputEnabled: MutableStateFlow<Boolean> =
        MutableStateFlow(isEditable && stamp.caption != null)
    val isCaptionInputEnabled: StateFlow<Boolean> = _isCaptionInputEnabled

    private val _events: MutableSharedFlow<Event> = eventSharedFlow()
    val events: SharedFlow<Event> = _events

    fun onCaptionInputChanged(
        newInput: String,
    ) {
        _caption.value = newInput
    }

    fun onAddCaptionAction() {
        check(isEditable) {
            "Can't add a caption for a read-only stamp"
        }

        _isCaptionInputEnabled.value = true
    }

    fun onDeleteAction() {
        check(isEditable) {
            "Can't delete a read-only stamp"
        }

        viewModelScope.launch {
            stampRepository.deleteStamp(stamp)
            _events.emit(Event.Done)
        }
    }

    private suspend fun saveUpdates() {
        val newCaption =
            _caption
                .value
                ?.trim()
                ?.takeIf(String::isNotEmpty)

        if (newCaption != stamp.caption) {
            stampRepository.updateStamp(
                stamp = stamp,
                newCaption = Optional.ofNullable(newCaption),
            )
        }
    }

    override fun onCleared() {
        if (isEditable) {
            runBlocking {
                saveUpdates()
            }
        }
        super.onCleared()
    }

    data class Parameters(
        val stampId: String,
    )

    sealed interface Event {
        object Done : Event
    }
}
