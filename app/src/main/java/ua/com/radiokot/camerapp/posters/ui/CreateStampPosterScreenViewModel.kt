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

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skydoves.landscapist.core.ImageRequest
import com.skydoves.landscapist.core.Landscapist
import com.skydoves.landscapist.core.model.ImageResult
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import ua.com.radiokot.camerapp.posters.domain.CreateSendStampPosterIntent
import ua.com.radiokot.camerapp.posters.domain.SendStampPosterOptions
import ua.com.radiokot.camerapp.posters.domain.StampPosterHeight
import ua.com.radiokot.camerapp.posters.domain.StampPosterLayer
import ua.com.radiokot.camerapp.stamps.domain.Stamp
import ua.com.radiokot.camerapp.stamps.domain.StampRepository
import ua.com.radiokot.camerapp.stamps.ui.UiStampShape
import ua.com.radiokot.camerapp.util.eventSharedFlow
import ua.com.radiokot.camerapp.util.lazyLogger

@Stable
class CreateStampPosterScreenViewModel(
    private val stampRepository: StampRepository,
    private val landscapist: Landscapist,
    private val fontFamilyResolver: FontFamily.Resolver,
    private val createSendStampPosterIntent: CreateSendStampPosterIntent,
    parameters: Parameters,
) : ViewModel() {

    private val log by lazyLogger("CreateStampPosterScreenVM")

    private val posterId = System.currentTimeMillis().toString()
    private var textLayerToEdit: StampPosterLayer.Text? = null
    val layers: StateFlow<PersistentList<StampPosterLayer>>
        field = MutableStateFlow(persistentListOf<StampPosterLayer>())
    val isDark: StateFlow<Boolean>
        field = MutableStateFlow(false)
    val events: SharedFlow<Event>
        field = eventSharedFlow()

    init {
        viewModelScope.launch {
            val firstStamp = stampRepository.getStamp(parameters.firstStampId)
                ?: error("Stamp with id ${parameters.firstStampId} not found")

            initLayersWithStamp(firstStamp)
        }
    }

    private suspend fun initLayersWithStamp(
        stamp: Stamp,
    ) {
        val stampLayers = mutableListOf<StampPosterLayer>()

        stampLayers +=
            StampPosterLayer.Stamp(
                imageBitmap = stamp.getImageBitmap(),
                shape = UiStampShape.fromShape(stamp.shape),
            )

        if (stamp.caption != null) {
            stampLayers +=
                StampPosterLayer.Text(
                    text = stamp.caption,
                    fontFamilyResolver = fontFamilyResolver,
                ).apply {
                    center = center.copy(
                        y = StampPosterHeight / 4f,
                    )
                }
        }

        layers.value = stampLayers.toPersistentList()
    }

    fun onToggleIsDarkAction() {
        isDark.value = !isDark.value
    }

    fun onAddTextAction() {
        events.tryEmit(
            Event.ProceedToEditText(
                currentText = null,
            )
        )
    }

    fun onBeginInteractionWithLayer(
        layer: StampPosterLayer,
    ) {
        val layers = this.layers.value
        val layerIndex = layers.indexOf(layer)

        if (layerIndex == layers.size - 1) {
            return
        }

        log.debug {
            "onBeginInteractionWithLayer(): moving layer to the top:" +
                    "\nlayer=$layer"
        }

        this.layers.value =
            layers
                .removingAt(layerIndex)
                .adding(layer)
    }

    fun onSendAction() {
        val layers = layers.value
        val options = SendStampPosterOptions(
            id = posterId,
            layers = layers,
            isDark = isDark.value,
        )
        val intent = createSendStampPosterIntent(options)

        log.debug {
            "onSendAction(): proceeding to send:" +
                    "\noptions=$options," +
                    "\nintent=$intent"
        }

        events.tryEmit(Event.ProceedToSendIntent(intent))
    }

    fun onDoneEditingText(
        text: String?,
    ) {
        val textLayerToEdit = this.textLayerToEdit

        when {
            // When adding text.
            textLayerToEdit == null && text != null -> {
                layers.value = layers.value.adding(
                    StampPosterLayer.Text(
                        text = text,
                        fontFamilyResolver = fontFamilyResolver,
                    )
                )
            }

            // When editing text.
            textLayerToEdit != null && text != null -> {
                textLayerToEdit.text = text
            }

            // When erasing text through editing.
            textLayerToEdit != null && text == null -> {
                layers.value = layers.value.removing(textLayerToEdit)
            }
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
            ?.asImageBitmap()

    data class Parameters(
        val firstStampId: String,
    )

    sealed interface Event {
        class ProceedToSendIntent(
            val intent: Intent,
        ) : Event

        class ProceedToEditText(
            val currentText: String?,
        ) : Event
    }
}
