package ua.com.radiokot.camerapp.cut.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.IntState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.createBitmap
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform
import ua.com.radiokot.camerapp.stamps.ui.CaptionInput
import ua.com.radiokot.camerapp.stamps.ui.StampSize
import ua.com.radiokot.camerapp.ui.LeTextButton

@Composable
fun StampSaveScreen(
    modifier: Modifier = Modifier,
    captionInputState: TextFieldState,
    imageState: State<ImageBitmap>,
    onSaveAction: () -> Unit,
    adjustmentsControllerItems: ImmutableList<AdjustmentControllerItem>,
    currentAdjustmentsControllerItemState: State<AdjustmentControllerItem>,
    onCurrentAdjustmentsControllerItemChanged: (AdjustmentControllerItem) -> Unit,
    adjustmentsControllerValueState: IntState,
    onAdjustmentsControllerValueChanged: (Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) = Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
        .safeContentPadding()
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(10f)
    ) {
        CaptionInput(
            inputState = captionInputState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )

        Image(
            bitmap = imageState.value,
            contentDescription = null,
            modifier = Modifier
                .size(StampSize * 2f)
                .run {
                    if (sharedTransitionScope == null || animatedVisibilityScope == null) {
                        return@run this
                    }

                    with(sharedTransitionScope) {
                        sharedElement(
                            sharedContentState = rememberSharedContentState("image"),
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                    }
                }
                .dropShadow(
                    shape = RectangleShape,
                    shadow = Shadow(
                        radius = 16.dp,
                        color = Color(0x7447525E),
                    )
                )
        )

        Spacer(
            modifier = Modifier
                .fillMaxHeight(0.5f)
        )
    }

    val bottomControlsAlpha = remember {
        Animatable(1f)
    }
    val imePadding = WindowInsets.ime.asPaddingValues()
    val keyboardTrendFlow = remember {
        var previousPadding = 0f
        snapshotFlow { imePadding.calculateBottomPadding().value }
            .transform { currentPadding ->
                val trend = currentPadding.compareTo(previousPadding)
                previousPadding = currentPadding
                if (trend != 0) {
                    emit(trend)
                }
            }
            .distinctUntilChanged()
    }
    LaunchedEffect(keyboardTrendFlow) {
        keyboardTrendFlow.collect { trend ->
            bottomControlsAlpha.animateTo(
                targetValue = if (trend > 0) 0f else 1f
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .graphicsLayer {
                alpha = bottomControlsAlpha.value
            }
    ) {
        AdjustmentsController(
            items = adjustmentsControllerItems,
            currentItemState = currentAdjustmentsControllerItemState,
            onCurrentItemChanged = onCurrentAdjustmentsControllerItemChanged,
            valueState = adjustmentsControllerValueState,
            onValueChanged = onAdjustmentsControllerValueChanged,
            modifier = Modifier
                .fillMaxWidth()
        )

        LeTextButton(
            text = "Save",
            onClick = onSaveAction,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun StampSaveScreenPreview(

) {
    val adjustmentsControllerItems =
        persistentListOf(
            AdjustmentControllerItem(
                title = "Brightness",
                minValue = -100,
                maxValue = 100,
                key = "brightness",
            ),
            AdjustmentControllerItem(
                title = "Contrast",
                minValue = -100,
                maxValue = 100,
                key = "contrast",
            ),
            AdjustmentControllerItem(
                title = "Vibrance",
                minValue = -100,
                maxValue = 100,
                key = "vibrance",
            ),
        )

    val captionState = remember {
        TextFieldState("")
    }

    val frameImage = remember {
        createBitmap(
            width = StampSize.width.value.toInt(),
            height = StampSize.height.value.toInt(),
        ).asImageBitmap()
    }

    StampSaveScreen(
        captionInputState = captionState,
        imageState = frameImage.let(::mutableStateOf),
        onSaveAction = { },
        adjustmentsControllerItems = adjustmentsControllerItems,
        currentAdjustmentsControllerItemState =
            adjustmentsControllerItems
                .first()
                .let(::mutableStateOf),
        onCurrentAdjustmentsControllerItemChanged = {},
        onAdjustmentsControllerValueChanged = {},
        adjustmentsControllerValueState = 0.let(::mutableIntStateOf),
        sharedTransitionScope = null,
        animatedVisibilityScope = null,
        modifier = Modifier
            .fillMaxSize()
    )
}
