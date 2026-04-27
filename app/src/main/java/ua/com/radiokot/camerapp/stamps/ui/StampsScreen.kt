package ua.com.radiokot.camerapp.stamps.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import kotlinx.collections.immutable.toPersistentList
import ua.com.radiokot.camerapp.ui.podkovaFamily
import kotlin.math.absoluteValue

@Composable
fun StampsScreen(
    modifier: Modifier = Modifier,
    collectionId: String,
    collectionName: String,
    stamps: State<ImmutableList<StampListItem>>,
    onStampClicked: (StampListItem) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) {
    val spacedBy = Arrangement.spacedBy(24.dp)
    val shadowColor = Color(0x7447525E)
    val rotationAngles = remember {
        intArrayOf(4, 3, 2, -2, -3, -4)
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(
            minSize = StampSize.width * 1.05f,
        ),
        verticalArrangement = spacedBy,
        contentPadding = WindowInsets
            .safeContent
            .asPaddingValues(),
        modifier = modifier
            .run {
                if (sharedTransitionScope == null || animatedVisibilityScope == null) {
                    return@run this
                }

                with(sharedTransitionScope) {
                    sharedBounds(
                        sharedContentState = rememberSharedContentState(collectionId),
                        animatedVisibilityScope = animatedVisibilityScope,
                        zIndexInOverlay = 10f,
                    )
                }
            }
    ) {
        item(
            span = {
                GridItemSpan(maxCurrentLineSpan)
            },
            key = "name",
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 24.dp,
                    )
            ) {
                BasicText(
                    text = collectionName,
                    style = TextStyle(
                        fontFamily = podkovaFamily,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .run {
                            if (sharedTransitionScope == null || animatedVisibilityScope == null) {
                                return@run this
                            }

                            with(sharedTransitionScope) {
                                sharedElement(
                                    sharedContentState = rememberSharedContentState("$collectionId-name"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    zIndexInOverlay = 20f,
                                )
                            }
                        }
                )
            }
        }
        items(
            items = stamps.value,
            key = StampListItem::key,
        ) { stamp ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(StampSize.height * 1.05f)
            ) {
                LandscapistImage(
                    imageModel = { stamp.thumbnailUrl.toUri() },
                    modifier = Modifier
                        .size(StampSize)
                        .run {
                            if (stamp.thumbnailUrl.isNotEmpty()) {
                                return@run this
                            }

                            background(Color.Yellow)
                        }
                        .run {
                            if (sharedTransitionScope == null || animatedVisibilityScope == null) {
                                return@run this
                            }

                            with(sharedTransitionScope) {
                                sharedElement(
                                    sharedContentState = rememberSharedContentState(stamp.key),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                )
                            }
                        }
                        .rotate(
                            (rotationAngles[stamp.key.hashCode().absoluteValue % rotationAngles.size])
                                .toFloat()
                        )
                        .dropShadow(
                            shape = RectangleShape,
                            shadow = Shadow(
                                radius = 4.dp,
                                color = shadowColor,
                            )
                        )
                        .clickable(
                            onClick = {
                                onStampClicked(stamp)
                            }
                        )
                )
            }
        }
    }
}

@Preview
@Composable
private fun StampsScreenPreview(

) {
    val stamps = (1..5)
        .map { i ->
            StampListItem(
                thumbnailUrl = "",
                key = i.toString(),
            )
        }
        .toPersistentList()

    StampsScreen(
        modifier = Modifier
            .fillMaxSize(),
        collectionId = "",
        collectionName = "My stamps",
        stamps = stamps.let(::mutableStateOf),
        onStampClicked = { },
        sharedTransitionScope = null,
        animatedVisibilityScope = null
    )
}
