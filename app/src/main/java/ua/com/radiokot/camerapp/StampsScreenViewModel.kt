package ua.com.radiokot.camerapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.stateIn

class StampsScreenViewModel(
    private val stampRepository: StampRepository,
) : ViewModel() {

    val stamps: StateFlow<List<StampListItem>> =
        suspend {
            stampRepository
                .getStamps(
                    asc = false,
                )
                .map(::StampListItem)
        }
            .asFlow()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}
