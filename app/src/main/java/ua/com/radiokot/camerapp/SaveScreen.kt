package ua.com.radiokot.camerapp

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.IntState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun SaveScreen(
    modifier: Modifier = Modifier,
    frameImageState: State<ImageBitmap?>,
    onSaveClicked: () -> Unit,
    adjustmentsControllerItems: ImmutableList<AdjustmentControllerItem>,
    currentAdjustmentsControllerItemState: State<AdjustmentControllerItem>,
    onCurrentAdjustmentsControllerItemChanged: (AdjustmentControllerItem) -> Unit,
    adjustmentsControllerValueState: IntState,
    onAdjustmentsControllerValueChanged: (Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) = Box(
    modifier = modifier
        .paperBackground()
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
    ) {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f, true)
        ) {
            Box(
                modifier = Modifier
                    .offset(
                        y = maxHeight / 9,
                    )
                    .size(StampSize * 2f)
                    .dropShadow(
                        shape = RectangleShape,
                        shadow = Shadow(
                            radius = 16.dp,
                            color = Color(0x7447525E),
                        )
                    )
            ) {
                if (frameImageState.value != null) {
                    Image(
                        bitmap = frameImageState.value!!,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
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
                    )
                }
            }
        }

        AdjustmentsController(
            items = adjustmentsControllerItems,
            currentItemState = currentAdjustmentsControllerItemState,
            onCurrentItemChanged = onCurrentAdjustmentsControllerItemChanged,
            valueState = adjustmentsControllerValueState,
            onValueChanged = onAdjustmentsControllerValueChanged,
            modifier = Modifier
                .fillMaxWidth()
        )

        val buttonShape = RoundedCornerShape(
            percent = 50,
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(24.dp)
                .height(56.dp)
                .fillMaxWidth()
                .dropShadow(
                    shape = buttonShape,
                    shadow = Shadow(
                        radius = 8.dp,
                        spread = 2.dp,
                        color = Color(0x414A6585),
                    )
                )
                .clip(buttonShape)
                .background(
                    color = Color(0xffe5920f),
                )
                .clickable(
                    onClick = onSaveClicked,
                )
        ) {
            BasicText(
                text = "Save to Gallery",
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF212121),
                ),
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
private fun SaveScreenPreview(

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

    SaveScreen(
        frameImageState = null.let(::mutableStateOf),
        onSaveClicked = { },
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
