package ua.com.radiokot.camerapp.stamps.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.image.LandscapistImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import ua.com.radiokot.camerapp.ui.LeTextButton
import ua.com.radiokot.camerapp.ui.paperBackground
import ua.com.radiokot.camerapp.ui.podkovaFamily
import ua.com.radiokot.camerapp.util.plus
import kotlin.math.absoluteValue

@Composable
fun CollectionsScreen(
    modifier: Modifier = Modifier,
    itemsState: State<ImmutableList<CollectionListItem>>,
    onItemClicked: (CollectionListItem) -> Unit,
    onItemLongClicked: (CollectionListItem) -> Unit,
    onNewStampAction: () -> Unit,
    onNewCollectionAction: () -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) = Box(
    modifier = modifier
) {
    val collectionShape = remember {
        RoundedCornerShape(10.dp)
    }
    val nameStyle = remember {
        TextStyle(
            fontFamily = podkovaFamily,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
        )
    }
    val sampleRotationAngles = retain {
        SampleRotationAngles(
            center = intArrayOf(3, 2, -2, -3),
            left = intArrayOf(-4, -5, -6),
            right = intArrayOf(6, 5, 4),
        )
    }
    val safeContentPadding =
        WindowInsets.safeContent.asPaddingValues()
    val contentPadding =
        safeContentPadding +
                PaddingValues(
                    // Button height and spacing.
                    bottom = 120.dp,
                )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(
            minSize = StampSize.width * 1.5f,
        ),
        horizontalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterHorizontally,
        ),
        verticalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterVertically,
        ),
        contentPadding = contentPadding,
        state = rememberLazyGridState(),
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 16.dp,
            )
    ) {
        items(
            items = itemsState.value,
            key = CollectionListItem::key,
        ) { item ->
            CollectionView(
                item = item,
                onClicked = onItemClicked,
                onLongClicked = onItemLongClicked,
                nameStyle = nameStyle,
                shape = collectionShape,
                rotationAngles = sampleRotationAngles,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }

        item(
            key = "new-collection-button"
        ) {
            NewCollectionView(
                onClicked = onNewCollectionAction,
                textStyle = nameStyle,
                shape = collectionShape,
            )
        }
    }

    LeTextButton(
        text = "New Stamp",
        onClick = onNewStampAction,
        modifier = Modifier
            .padding(safeContentPadding)
            .padding(24.dp)
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .run {
                if (sharedTransitionScope == null || animatedVisibilityScope == null) {
                    return@run this
                }

                with(sharedTransitionScope) {
                    sharedElement(
                        sharedContentState = rememberSharedContentState("new-stamp-button"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        zIndexInOverlay = 30f,
                    )
                }
            }
    )
}

@Immutable
private class SampleRotationAngles(
    val center: IntArray,
    val left: IntArray,
    val right: IntArray,
)

@Composable
private fun CollectionView(
    modifier: Modifier = Modifier,
    item: CollectionListItem,
    onClicked: (CollectionListItem) -> Unit,
    onLongClicked: (CollectionListItem) -> Unit,
    nameStyle: TextStyle,
    shape: Shape,
    rotationAngles: SampleRotationAngles,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) = Box(
    modifier = modifier
        .height(StampSize.height)
        .combinedClickable(
            indication = null,
            interactionSource = null,
            onClick = {
                onClicked(item)
            },
            onLongClick = {
                onLongClicked(item)
            },
        )
) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(StampSize.height * 0.7f)
            .background(
                color = Color(0xFFCBC4BB),
                shape = shape,
            )
            .border(
                width = 2.dp,
                color = Color(0xFF6B624B),
                shape = shape,
            )
            .align(Alignment.BottomCenter)
    )

    when (item.someStamps.size) {
        1 -> {
            StampSampleView(
                sample = item.someStamps[0],
                order = 0,
                possibleRotationAngles = rotationAngles.center,
                fallbackColor = Color.Yellow,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        y = -(8.dp),
                    )
            )
        }

        2 -> {
            StampSampleView(
                sample = item.someStamps[1],
                order = 0,
                possibleRotationAngles = rotationAngles.right,
                fallbackColor = Color.Red,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        x = StampSize.width * 0.2f,
                        y = -(8.dp),
                    )
            )
            StampSampleView(
                sample = item.someStamps[0],
                order = 1,
                possibleRotationAngles = rotationAngles.left,
                fallbackColor = Color.Yellow,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        x = -StampSize.width * 0.2f,
                        y = -(4.dp),
                    )
            )
        }

        3 -> {
            StampSampleView(
                sample = item.someStamps[2],
                order = 0,
                possibleRotationAngles = rotationAngles.right,
                fallbackColor = Color.Yellow,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        x = StampSize.width * 0.25f,
                        y = -(2.dp),
                    )
            )
            StampSampleView(
                sample = item.someStamps[1],
                order = 1,
                possibleRotationAngles = rotationAngles.center,
                fallbackColor = Color.Red,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        y = -(4.dp),
                    )
            )
            StampSampleView(
                sample = item.someStamps[0],
                order = 2,
                possibleRotationAngles = rotationAngles.left,
                fallbackColor = Color.Magenta,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        x = -StampSize.width * 0.25f,
                        y = -(8.dp),
                    )
            )
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(StampSize.height * 0.5f)
            .align(Alignment.BottomCenter)
            .run {
                if (sharedTransitionScope == null || animatedVisibilityScope == null) {
                    return@run this
                }

                with(sharedTransitionScope) {
                    sharedBounds(
                        sharedContentState = rememberSharedContentState(item.key),
                        animatedVisibilityScope = animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(
                            alignment = Alignment.TopCenter,
                        ),
                        exit = fadeOut(
                            animationSpec = snap(),
                        ),
                        zIndexInOverlay = 10f,
                    )
                }
            }
            .background(
                color = Color(0xFFFFF9EB),
                shape = shape,
            )
            .border(
                width = 2.dp,
                color = Color(0xFF6B624B),
                shape = shape,
            )
            .padding(8.dp)
    ) {
        BasicText(
            text = item.name,
            style = nameStyle,
            modifier = Modifier
                .fillMaxWidth()
                .run {
                    if (sharedTransitionScope == null || animatedVisibilityScope == null) {
                        return@run this
                    }

                    with(sharedTransitionScope) {
                        sharedElement(
                            sharedContentState = rememberSharedContentState("${item.key}-name"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            zIndexInOverlay = 20f,
                        )
                    }
                }
        )
    }
}

@Composable
private fun NewCollectionView(
    modifier: Modifier = Modifier,
    onClicked: () -> Unit,
    textStyle: TextStyle,
    shape: Shape,
) = Box(
    modifier = modifier
        .height(StampSize.height)
        .clickable(
            onClick = {
                onClicked()
            },
        ),
    contentAlignment = Alignment.Center,
) {
    val layoutDirection = LocalLayoutDirection.current

    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(StampSize.height * 0.7f)
            .drawWithCache {
                val backOutline = shape.createOutline(
                    size = this.size,
                    layoutDirection = layoutDirection,
                    density = this,
                )
                val frontBackgroundColor = Color(0xFFFFF9EB)
                val strokeColor = Color(0xFF6B624B)
                val strokeInterval = StampSize.width.toPx() * 0.08f
                val dashStrokeStyle = Stroke(
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(strokeInterval, strokeInterval)
                    )
                )
                val frontOutline = shape.createOutline(
                    size = Size(
                        width = this.size.width + 1f,
                        height = StampSize.height.toPx() * 0.5f,
                    ),
                    layoutDirection = layoutDirection,
                    density = this,
                )
                val plainStrokeStyle = Stroke(
                    width = dashStrokeStyle.width,
                )

                onDrawBehind {
                    drawOutline(
                        outline = backOutline,
                        color = strokeColor,
                        style = dashStrokeStyle,
                    )
                    translate(
                        left = -1f,
                        top = backOutline.bounds.height - frontOutline.bounds.height + 1f,
                    ) {
                        drawOutline(
                            outline = frontOutline,
                            color = frontBackgroundColor,
                            style = Fill,
                        )
                        drawOutline(
                            outline = frontOutline,
                            color = frontBackgroundColor,
                            style = plainStrokeStyle,
                        )
                        drawOutline(
                            outline = frontOutline,
                            color = strokeColor,
                            style = dashStrokeStyle,
                        )
                    }
                }
            }
            .align(Alignment.BottomCenter)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(StampSize.height * 0.5f)
            .align(Alignment.BottomCenter)
            .padding(8.dp)
    ) {
        BasicText(
            text = "New Collection",
            style = textStyle,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
private fun StampSampleView(
    modifier: Modifier = Modifier,
    fallbackColor: Color,
    sample: CollectionListItem.StampSampleItem,
    possibleRotationAngles: IntArray,
    order: Int,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) {
    // To avoid flicker when opening the stamps screen,
    // make the library load the image in a size
    // that matches the stamp size on the stamps screen.
    val density = LocalDensity.current
    val imageOptions = retain(density) {
        with(density) {
            ImageOptions(
                requestSize = IntSize(
                    width = StampSize.width.roundToPx(),
                    height = StampSize.height.roundToPx(),
                )
            )
        }
    }
    LandscapistImage(
        imageModel = { sample.imageUri.toUri() },
        imageOptions = imageOptions,
        modifier = modifier
            .size(StampSize * 0.85f)
            .run {
                if (sharedTransitionScope == null || animatedVisibilityScope == null) {
                    return@run this
                }

                with(sharedTransitionScope) {
                    sharedElement(
                        sharedContentState = rememberSharedContentState(sample.key),
                        animatedVisibilityScope = animatedVisibilityScope,
                        zIndexInOverlay = order.toFloat(),
                    )
                }
            }
            .rotate(
                (possibleRotationAngles[sample.key.hashCode().absoluteValue % possibleRotationAngles.size])
                    .toFloat()
            )
            .run {
                if (sample.imageUri.isNotEmpty()) {
                    return@run this
                }

                background(fallbackColor)
            }
            .dropShadow(
                shape = RectangleShape,
                shadow = Shadow(
                    radius = 4.dp,
                    color = Color(0x7447525E),
                )
            )
    )
}

@Preview
@Composable
private fun CollectionsScreenPreview() {
    val items = listOf(
        CollectionListItem(
            key = "1",
            name = "My stamps",
            someStamps = persistentListOf(
                CollectionListItem.StampSampleItem(
                    imageUri = "",
                    key = "",
                ),
            ),
        ),
        CollectionListItem(
            key = "2",
            name = "RED",
            someStamps = persistentListOf(
                CollectionListItem.StampSampleItem(
                    imageUri = "",
                    key = "1",
                ),
                CollectionListItem.StampSampleItem(
                    imageUri = "",
                    key = "2",
                ),
            ),
        ),
        CollectionListItem(
            key = "3",
            name = "Food",
            someStamps = persistentListOf(
                CollectionListItem.StampSampleItem(
                    imageUri = "",
                    key = "1",
                ),
                CollectionListItem.StampSampleItem(
                    imageUri = "",
                    key = "2",
                ),
                CollectionListItem.StampSampleItem(
                    imageUri = "",
                    key = "3",
                ),
            ),
        ),
    ).toImmutableList()

    CollectionsScreen(
        itemsState = items.let(::mutableStateOf),
        onItemClicked = {},
        onNewStampAction = {},
        onItemLongClicked = {},
        onNewCollectionAction = {},
        sharedTransitionScope = null,
        animatedVisibilityScope = null,
        modifier = Modifier
            .fillMaxSize()
            .paperBackground()
    )
}
