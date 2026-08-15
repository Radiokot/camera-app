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
import androidx.compose.ui.geometry.Offset
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.com.radiokot.camerapp.posters.domain.CreateSendStampPosterIntent
import ua.com.radiokot.camerapp.posters.domain.SendStampPosterOptions
import ua.com.radiokot.camerapp.posters.domain.StampPosterHeight
import ua.com.radiokot.camerapp.posters.domain.StampPosterLayer
import ua.com.radiokot.camerapp.posters.domain.StampPosterMaxStamps
import ua.com.radiokot.camerapp.posters.domain.StampPosterRect
import ua.com.radiokot.camerapp.posters.domain.StampPosterWidth
import ua.com.radiokot.camerapp.stamps.domain.Stamp
import ua.com.radiokot.camerapp.stamps.domain.StampRepository
import ua.com.radiokot.camerapp.stamps.domain.StampSelections
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
    private var anyChanges: Boolean = false
        set(value) {
            field = value
            isDiscardConfirmationRequired.value = true
        }
    val layers: StateFlow<PersistentList<StampPosterLayer>>
        field = MutableStateFlow(persistentListOf<StampPosterLayer>())
    val isDark: StateFlow<Boolean>
        field = MutableStateFlow(false)
    val events: SharedFlow<Event>
        field = eventSharedFlow()
    val isDiscardConfirmationRequired: StateFlow<Boolean>
        field = MutableStateFlow(false)
    private val stampLayerCount: StateFlow<Int> =
        layers
            .map { layers ->
                layers.count { it is StampPosterLayer.Stamp }
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val canAddStamps: StateFlow<Boolean> =
        stampLayerCount
            .map { it < StampPosterMaxStamps }
            .stateIn(viewModelScope, SharingStarted.Lazily, true)

    init {
        viewModelScope.launch {
            when {
                parameters.firstStampId != null -> {
                    initLayersWithStamp(
                        stamp =
                            stampRepository.getStamp(parameters.firstStampId)
                                ?: error("Stamp with id ${parameters.firstStampId} not found"),
                    )
                }

                parameters.stampSelectionIndex != null -> {
                    val stampIds = StampSelections[parameters.stampSelectionIndex]
                    val stamps =
                        stampRepository
                            .getStamps()
                            .filter { it.id in stampIds }

                    if (stamps.size == 1) {
                        initLayersWithStamp(stamps.first())
                    } else {
                        initLayersWithStamps(stamps)
                    }
                }
            }
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
                    appearance = StampPosterLayer.Text.Appearance(
                        background = null,
                        alignment = StampPosterLayer.Text.Alignment.Center,
                    ),
                    fontFamilyResolver = fontFamilyResolver,
                ).apply {
                    center = StampPosterRect.center.copy(
                        y = StampPosterHeight / 4f,
                    )
                }
        }

        layers.value = stampLayers.toPersistentList()
    }

    private suspend fun initLayersWithStamps(
        stamps: List<Stamp>,
    ) {
        require(stamps.size <= StampPosterMaxStamps) {
            "That should have been handled before this screen"
        }

        val stampLayers = mutableListOf<StampPosterLayer>()

        when (stamps.size) {
            2 -> {
                stampLayers += StampPosterLayer.Stamp(
                    imageBitmap = stamps[0].getImageBitmap(),
                    shape = UiStampShape.fromShape(stamps[0].shape),
                ).apply {
                    center = Offset(
                        x = StampPosterWidth * 0.5f,
                        y = StampPosterHeight * 0.3f,
                    )
                }

                stampLayers += StampPosterLayer.Stamp(
                    imageBitmap = stamps[1].getImageBitmap(),
                    shape = UiStampShape.fromShape(stamps[1].shape),
                ).apply {
                    center = Offset(
                        x = StampPosterWidth * 0.5f,
                        y = StampPosterHeight * 0.7f,
                    )
                }
            }

            3 -> {
                stampLayers += StampPosterLayer.Stamp(
                    imageBitmap = stamps[0].getImageBitmap(),
                    shape = UiStampShape.fromShape(stamps[0].shape),
                ).apply {
                    center = Offset(
                        x = StampPosterWidth * 0.33f,
                        y = StampPosterHeight * 0.25f,
                    )
                }

                stampLayers += StampPosterLayer.Stamp(
                    imageBitmap = stamps[1].getImageBitmap(),
                    shape = UiStampShape.fromShape(stamps[1].shape),
                ).apply {
                    center = Offset(
                        x = StampPosterWidth * 0.33f,
                        y = StampPosterHeight * 0.75f,
                    )
                }

                stampLayers += StampPosterLayer.Stamp(
                    imageBitmap = stamps[2].getImageBitmap(),
                    shape = UiStampShape.fromShape(stamps[2].shape),
                ).apply {
                    center = Offset(
                        x = StampPosterWidth * 0.66f,
                        y = StampPosterHeight * 0.5f,
                    )
                }
            }

            4 -> {
                stampLayers += StampPosterLayer.Stamp(
                    imageBitmap = stamps[0].getImageBitmap(),
                    shape = UiStampShape.fromShape(stamps[0].shape),
                ).apply {
                    center = Offset(
                        x = StampPosterWidth * 0.33f,
                        y = StampPosterHeight * 0.23f,
                    )
                }

                stampLayers += StampPosterLayer.Stamp(
                    imageBitmap = stamps[1].getImageBitmap(),
                    shape = UiStampShape.fromShape(stamps[1].shape),
                ).apply {
                    center = Offset(
                        x = StampPosterWidth * 0.66f,
                        y = StampPosterHeight * 0.33f,
                    )
                }

                stampLayers += StampPosterLayer.Stamp(
                    imageBitmap = stamps[2].getImageBitmap(),
                    shape = UiStampShape.fromShape(stamps[2].shape),
                ).apply {
                    center = Offset(
                        x = StampPosterWidth * 0.33f,
                        y = StampPosterHeight * 0.7f,
                    )
                }

                stampLayers += StampPosterLayer.Stamp(
                    imageBitmap = stamps[3].getImageBitmap(),
                    shape = UiStampShape.fromShape(stamps[3].shape),
                ).apply {
                    center = Offset(
                        x = StampPosterWidth * 0.66f,
                        y = StampPosterHeight * 0.8f,
                    )
                }
            }

            else -> {
                stampLayers += getStampLayerStack(stamps)
            }
        }

        layers.value = stampLayers.toPersistentList()
    }

    private suspend fun getStampLayerStack(
        stamps: List<Stamp>,
    ): List<StampPosterLayer.Stamp> = buildList {

        // Stack stamps on top of each other with slight offset.
        val centerStep = Offset(
            x = StampPosterWidth * 0.05f * StampPosterMaxStamps / stamps.size,
            y = StampPosterWidth * 0.08f * StampPosterMaxStamps / stamps.size,
        )
        var nextStampCenter = Offset(
            x = StampPosterWidth * 0.28f,
            y = StampPosterHeight * 0.2f,
        )

        for (stamp in stamps) {
            this += StampPosterLayer.Stamp(
                imageBitmap = stamp.getImageBitmap(),
                shape = UiStampShape.fromShape(stamp.shape),
            ).apply {
                center = nextStampCenter
            }

            nextStampCenter += centerStep
        }
    }

    fun onToggleIsDarkAction() {
        isDark.value = !isDark.value
    }

    fun onAddTextAction() {
        textLayerToEdit = null

        log.debug {
            "onAddTextAction(): proceeding to edit new layer text"
        }

        events.tryEmit(
            Event.ProceedToEditText(
                currentText = null,
                currentAppearance = StampPosterLayer.Text.Appearance(
                    background =
                        if (isDark.value)
                            StampPosterLayer.Text.Background.Dark
                        else
                            StampPosterLayer.Text.Background.Light,
                    alignment = StampPosterLayer.Text.Alignment.Center,
                ),
            )
        )
    }

    fun onAddStampsAction() {
        val currentStampCount = stampLayerCount.value

        check(currentStampCount < StampPosterMaxStamps) {
            "Can't add stamps when the count is maxed out"
        }

        val maxCount = StampPosterMaxStamps - currentStampCount

        log.debug {
            "onAddStampsAction(): proceeding to select stamps to add:" +
                    "\nmaxCount=$maxCount"
        }

        events.tryEmit(
            Event.ProceedToSelectStampsToAdd(
                maxCount = maxCount,
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

    fun onEndInteractionWithLayer(
        layer: StampPosterLayer,
    ) {
        if (layer.isOutOfBounds) {
            log.debug {
                "onEndInteractionWithLayer(): removing out-of-bounds layer:" +
                        "\nlayer=$layer"
            }

            layers.value = layers.value.removing(layer)

            events.tryEmit(
                Event.ShowLayerDeletedMessage(
                    layerName = when (layer) {
                        is StampPosterLayer.Stamp -> "Stamp"
                        is StampPosterLayer.Text -> "Text"
                    }
                )
            )
        }

        anyChanges = true
    }

    fun onLayerTap(layer: StampPosterLayer) {
        if (layer is StampPosterLayer.Text) {
            textLayerToEdit = layer

            log.debug {
                "onLayerTap(): proceeding to edit text layer:" +
                        "\nlayer=$layer"
            }

            events.tryEmit(
                Event.ProceedToEditText(
                    currentText = layer.text,
                    currentAppearance = layer.appearance,
                )
            )
        }
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
        appearance: StampPosterLayer.Text.Appearance,
    ) {
        val textLayerToEdit = this.textLayerToEdit

        when {
            // When adding text.
            textLayerToEdit == null && text != null -> {
                log.debug {
                    "onDoneEditingText(): adding new text layer:" +
                            "\ntext=$text"
                }

                layers.value = layers.value.adding(
                    StampPosterLayer.Text(
                        text = text,
                        fontFamilyResolver = fontFamilyResolver,
                        appearance = appearance,
                    )
                )

                anyChanges = true
            }

            // When editing text.
            textLayerToEdit != null && text != null -> {
                log.debug {
                    "onDoneEditingText(): editing text layer:" +
                            "\ntextLayerToEdit=$textLayerToEdit," +
                            "\nnewText=$text"
                }

                textLayerToEdit.text = text
                textLayerToEdit.appearance = appearance

                anyChanges = true
            }

            // When erasing text through editing.
            textLayerToEdit != null && text == null -> {
                log.debug {
                    "onDoneEditingText(): removing text layer:" +
                            "\ntextLayerToEdit=$textLayerToEdit"
                }

                layers.value = layers.value.removing(textLayerToEdit)

                anyChanges = true
            }
        }
    }

    fun onSelectedStampsToAdd(
        selectionIndex: Int,
    ) = viewModelScope.launch {

        val selectedStampIds = StampSelections[selectionIndex]

        if (selectedStampIds.isEmpty()) {
            return@launch
        }

        val stamps = stampRepository
            .getStamps()
            .filter { it.id in selectedStampIds }

        log.debug {
            "onSelectedStampsToAdd(): adding selected stamps:" +
                    "\nstamps=${stamps.size}"
        }

        layers.value = layers.value.addingAll(
            getStampLayerStack(
                stamps = stamps,
            )
        )

        anyChanges = true
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
        val firstStampId: String?,
        val stampSelectionIndex: Int?,
    )

    sealed interface Event {
        class ProceedToSendIntent(
            val intent: Intent,
        ) : Event

        class ProceedToEditText(
            val currentText: String?,
            val currentAppearance: StampPosterLayer.Text.Appearance,
        ) : Event

        class ProceedToSelectStampsToAdd(
            val maxCount: Int,
        ) : Event

        class ShowLayerDeletedMessage(
            val layerName: String,
        ) : Event
    }
}
