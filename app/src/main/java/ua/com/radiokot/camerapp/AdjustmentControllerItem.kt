package ua.com.radiokot.camerapp

import androidx.compose.runtime.Immutable

@Immutable
data class AdjustmentControllerItem(
    val title: String,
    val minValue: Int,
    val maxValue: Int,
    val key: Any,
) {
}
