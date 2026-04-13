package ua.com.radiokot.camerapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt

@Composable
fun AdjustmentsController(
    modifier: Modifier = Modifier,
    items: List<AdjustmentControllerItem>,
    currentItemState: State<AdjustmentControllerItem>,
    onCurrentItemChanged: (AdjustmentControllerItem) -> Unit,
    valueState: IntState,
    onValueChanged: (Int) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        BasicText(
            text = currentItemState.value.title,
            style = TextStyle(
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier
                .fillMaxWidth()
        )

        ItemSelector(
            items = items,
            currentItemState = currentItemState,
            onCurrentItemChanged = onCurrentItemChanged,
            modifier = Modifier
                .padding(
                    top = 8.dp
                )
                .fillMaxWidth()
        )

        ValueDial(
            minValue = currentItemState.value.minValue,
            maxValue = currentItemState.value.maxValue,
            valueState = valueState,
            onValueChanged = onValueChanged,
            modifier = Modifier
                .padding(
                    top = 8.dp
                )
                .fillMaxWidth()
        )
    }
}

@Composable
private fun ItemSelector(
    modifier: Modifier = Modifier,
    items: List<AdjustmentControllerItem>,
    currentItemState: State<AdjustmentControllerItem>,
    onCurrentItemChanged: (AdjustmentControllerItem) -> Unit,
) = BoxWithConstraints(
    modifier = modifier
) {
    val itemSize = 42.dp
    val itemSizePx = with(LocalDensity.current) {
        itemSize.toPx()
    }
    val spacingDp = 16.dp
    val spacingPx = with(LocalDensity.current) {
        spacingDp.toPx()
    }

    val initialFirstVisibleItemIndex = remember(currentItemState) {
        items.indexOf(currentItemState.value)
    }
    val rowState = rememberLazyListState(initialFirstVisibleItemIndex)
    val itemFlow = remember(rowState) {
        val offsetThreshold = (itemSizePx + spacingPx) / 2f
        snapshotFlow {
            var index = rowState.firstVisibleItemIndex
            if (rowState.firstVisibleItemScrollOffset > offsetThreshold) {
                index++
            }
            items[index]
        }
    }
    LaunchedEffect(itemFlow) {
        itemFlow.collect { newItem ->
            if (newItem != currentItemState.value) {
                onCurrentItemChanged(newItem)
            }
        }
    }

    LazyRow(
        state = rowState,
        flingBehavior = rememberSnapFlingBehavior(rowState),
        contentPadding =
            PaddingValues(
                start = (maxWidth - itemSize) / 2,
                end = (maxWidth - itemSize) / 2,
            ),
        horizontalArrangement = Arrangement.spacedBy(spacingDp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(
            items = items,
            key = AdjustmentControllerItem::key,
        ) { item ->
            Spacer(
                modifier = Modifier
                    .size(itemSize)
                    .align(Alignment.Center)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFB9AC8C),
                        shape = CircleShape,
                    )
            )
        }
    }

    Spacer(
        modifier = Modifier
            .size(itemSize)
            .align(Alignment.Center)
            .border(
                width = 1.dp,
                color = Color(0xFF6B624B),
                shape = CircleShape,
            )
    )
}

@Composable
private fun ValueDial(
    modifier: Modifier = Modifier,
    minValue: Int,
    maxValue: Int,
    valueState: IntState,
    onValueChanged: (Int) -> Unit,
) = BoxWithConstraints(
    contentAlignment = Alignment.BottomCenter,
    modifier = modifier
) {
    val step = 5
    val spacingDp = 6.dp
    val spacingPx = with(LocalDensity.current) {
        spacingDp.toPx()
    }
    val (
        initialFirstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset,
    ) = remember(minValue, step, spacingPx, valueState) {
        toIndexAndOffset(
            minValue = minValue,
            step = step,
            itemSpacingPx = spacingPx,
            value = valueState.value,
        )
    }
    val rowState = rememberLazyListState(
        initialFirstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset,
    )
    val valueFlow = remember(rowState) {
        snapshotFlow {
            toValue(
                minValue = minValue,
                step = step,
                itemSpacingPx = spacingPx,
                firstVisibleItemIndex = rowState.firstVisibleItemIndex,
                firstVisibleItemScrollOffset = rowState.firstVisibleItemScrollOffset,
            )
        }
    }

    LaunchedEffect(valueFlow) {
        valueFlow.collect { newValue ->
            if (newValue != valueState.value) {
                onValueChanged(newValue)
            }
        }
    }

    LazyRow(
        state = rowState,
        contentPadding =
            PaddingValues(
                start = maxWidth / 2 - 1.dp,
                end = maxWidth / 2 - 1.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(spacingDp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(
            count = (maxValue - minValue) / step + 1,
        ) { i ->
            Spacer(
                modifier = Modifier
                    .size(
                        height = 12.dp,
                        width = 1.dp,
                    )
                    .background(
                        color =
                            if (i % step == 0)
                                Color(0xFFB9AC8C)
                            else
                                Color(0x99B9AC8C),
                    )
            )
        }
    }

    Spacer(
        modifier = Modifier
            .size(
                height = 16.dp,
                width = 2.dp,
            )
            .border(
                width = 2.dp,
                color = Color(0xFF6B624B),
                shape = CircleShape,
            )
    )
}

private fun toValue(
    minValue: Int,
    step: Int,
    itemSpacingPx: Float,
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
): Int {
    val stepIndex = firstVisibleItemIndex
    val stepOffsetPx = firstVisibleItemScrollOffset
    return minValue + ((stepIndex + stepOffsetPx / itemSpacingPx) * step).fastRoundToInt()
}

private fun toIndexAndOffset(
    minValue: Int,
    step: Int,
    itemSpacingPx: Float,
    value: Int,
): Pair<Int, Int> {
    val index = (value - minValue) / step
    val offsetPx = (itemSpacingPx * ((value - minValue) % step) / step).fastRoundToInt()
    return index to offsetPx
}

@Preview
@Composable
private fun AdjustmentsControllerPreview(

) = Box(
    modifier = Modifier
        .fillMaxSize()
        .paperBackground()
) {
    val items = listOf(
        AdjustmentControllerItem(
            title = "Brightness",
            minValue = -100,
            maxValue = 100,
            key = "B",
        ),
        AdjustmentControllerItem(
            title = "Contrast",
            minValue = -100,
            maxValue = 100,
            key = "C",
        ),
        AdjustmentControllerItem(
            title = "Vibrance",
            minValue = -100,
            maxValue = 100,
            key = "V",
        )
    )
    val currentItem = remember {
        mutableStateOf(items[1])
    }
    val currentValue = remember {
        mutableIntStateOf(0)
    }

    AdjustmentsController(
        items = items,
        currentItemState = currentItem,
        onCurrentItemChanged = { newItem ->
            println("OOLEG new item $newItem")
            currentItem.value = newItem
        },
        valueState = currentValue,
        onValueChanged = { newValue ->
            println("OOLEG new value $newValue")
            currentValue.intValue = newValue
        },
        modifier = Modifier
            .fillMaxWidth()
    )
}
