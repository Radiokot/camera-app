package ua.com.radiokot.camerapp

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@Immutable
class ImageAdjustmentsControllerViewModel : ViewModel() {

    val items = persistentListOf(
        AdjustmentControllerItem(
            title = "Brightness",
            minValue = -100,
            maxValue = 100,
            key = BRIGHTNESS_KEY,
        ),
        AdjustmentControllerItem(
            title = "Contrast",
            minValue = -100,
            maxValue = 100,
            key = CONTRAST_KEY,
        ),
        AdjustmentControllerItem(
            title = "Vibrance",
            minValue = -100,
            maxValue = 100,
            key = VIBRANCE_KEY,
        ),
    )
    private val _currentItem: MutableStateFlow<AdjustmentControllerItem> =
        MutableStateFlow(items.first())
    val currentItem: StateFlow<AdjustmentControllerItem> = _currentItem
    private val _brightnessValue: MutableStateFlow<Int> = MutableStateFlow(0)
    val brightnessValue: StateFlow<Int> = _brightnessValue
    private val _contrastValue: MutableStateFlow<Int> = MutableStateFlow(0)
    val contrastValue: StateFlow<Int> = _contrastValue
    private val _vibranceValue: MutableStateFlow<Int> = MutableStateFlow(0)
    val vibranceValue: StateFlow<Int> = _vibranceValue

    private val _currentValue: MutableStateFlow<Int>
        get() = when (currentItem.value.key) {
            BRIGHTNESS_KEY -> _brightnessValue
            CONTRAST_KEY -> _contrastValue
            VIBRANCE_KEY -> _vibranceValue
            else -> error("Unknown key")
        }
    val currentValue: StateFlow<Int> =
        currentItem
            .flatMapLatest { _currentValue }
            .stateIn(viewModelScope, SharingStarted.Eagerly, _currentValue.value)

    fun onCurrentItemChanged(newItem: AdjustmentControllerItem) {
        _currentItem.value = newItem
    }

    fun onValueChanged(newValue: Int) {
        _currentValue.value = newValue
    }

    private companion object {
        private const val BRIGHTNESS_KEY = "brightness"
        private const val CONTRAST_KEY = "contrast"
        private const val VIBRANCE_KEY = "vibrance"
    }
}
