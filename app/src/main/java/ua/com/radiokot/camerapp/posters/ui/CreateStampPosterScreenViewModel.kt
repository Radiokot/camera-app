/* Copyright 2026 Oleg Koretsky

   This file is part of the Press-Cut,
   a digital postage stamp cutter Android app.

   Press-Cut is free software: you can redistribute it
   and/or modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation, either version 3 of the License,
   or (at your option) any later version.

   Press-Cut is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   See the GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Press-Cut. If not, see <http://www.gnu.org/licenses/>.
*/

package ua.com.radiokot.camerapp.posters.ui

import android.graphics.Bitmap
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skydoves.landscapist.core.ImageRequest
import com.skydoves.landscapist.core.Landscapist
import com.skydoves.landscapist.core.model.ImageResult
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import ua.com.radiokot.camerapp.stamps.domain.Stamp
import ua.com.radiokot.camerapp.stamps.domain.StampRepository
import ua.com.radiokot.camerapp.stamps.ui.UiStampShape

@Stable
class CreateStampPosterScreenViewModel(
    private val stampRepository: StampRepository,
    private val landscapist: Landscapist,
    parameters: Parameters,
) : ViewModel() {

    val layers: StateFlow<PersistentList<UiStampPosterLayer>>
        field = MutableStateFlow(persistentListOf<UiStampPosterLayer>())

    init {
        viewModelScope.launch {
            val firstStamp = stampRepository.getStamp(parameters.firstStampId)
                ?: error("Stamp with id ${parameters.firstStampId} not found")

            layers.value = persistentListOf(
                UiStampPosterLayer.Stamp(
                    imageBitmap = firstStamp.getImageBitmap(),
                    shape = UiStampShape.fromShape(firstStamp.shape),
                )
            )
        }
    }

    private suspend fun Stamp.getImageBitmap() =
        landscapist
            .load(
                ImageRequest
                    .builder()
                    .model(imageUri.toUri())
                    .progressiveEnabled(false)
                    .build()
            )
            .filterIsInstance<ImageResult.Success>()
            .firstOrNull()
            ?.data
            ?.let { it as? Bitmap }
//            ?.let {
//                if (it.config == Bitmap.Config.HARDWARE)
//                    it.copy(Bitmap.Config.ARGB_8888, false)
//                else
//                    it
//            }
            ?.asImageBitmap()

    data class Parameters(
        val firstStampId: String,
    )
}
