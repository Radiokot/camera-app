package ua.com.radiokot.camerapp.stamps.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.skydoves.landscapist.image.LandscapistImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import ua.com.radiokot.camerapp.ui.paperBackground
import ua.com.radiokot.camerapp.ui.podkovaFamily
import kotlin.math.absoluteValue

@Composable
fun CollectionsScreen(
    modifier: Modifier = Modifier,
    itemsState: State<ImmutableList<CollectionListItem>>,
    onItemClicked: (CollectionListItem) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
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
    val centerSampleRotationAngles = remember {
        intArrayOf(3, 2, -2, -3)
    }
    val leftSampleRotationAngles = remember {
        intArrayOf(-4, -5, -6)
    }
    val rightSampleRotationAngles = remember {
        intArrayOf(6, 5, 4)
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(
            minSize = StampSize.width * 1.5f,
        ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(
            space = 24.dp,
            alignment = Alignment.CenterVertically,
        ),
        contentPadding = WindowInsets
            .safeContent
            .asPaddingValues(),
        state = rememberLazyGridState(),
        modifier = modifier
            .padding(
                horizontal = 16.dp,
            )
    ) {
        items(
            items = itemsState.value,
            key = CollectionListItem::key,
        ) { item ->

            Box(
                modifier = Modifier
                    .height(StampSize.height)
                    .clickable(
                        onClick = {
                            onItemClicked(item)
                        },
                    )
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(StampSize.height * 0.7f)
                        .background(
                            color = Color(0xFFCBC4BB),
                            shape = collectionShape,
                        )
                        .border(
                            width = 2.dp,
                            color = Color(0xFF6B624B),
                            shape = collectionShape,
                        )
                        .align(Alignment.BottomCenter)
                )

                when (item.someStamps.size) {
                    1 -> {
                        StampSampleView(
                            sample = item.someStamps[0],
                            order = 0,
                            possibleRotationAngles = centerSampleRotationAngles,
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
                            possibleRotationAngles = rightSampleRotationAngles,
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
                            possibleRotationAngles = leftSampleRotationAngles,
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
                            possibleRotationAngles = rightSampleRotationAngles,
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
                            possibleRotationAngles = centerSampleRotationAngles,
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
                            possibleRotationAngles = leftSampleRotationAngles,
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
                            shape = collectionShape,
                        )
                        .border(
                            width = 2.dp,
                            color = Color(0xFF6B624B),
                            shape = collectionShape,
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
        }
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
) = LandscapistImage(
    imageModel = { sample.imageUri.toUri() },
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
        sharedTransitionScope = null,
        animatedVisibilityScope = null,
        modifier = Modifier
            .fillMaxSize()
            .paperBackground()
    )
}
