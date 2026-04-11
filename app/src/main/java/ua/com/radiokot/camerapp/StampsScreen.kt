package ua.com.radiokot.camerapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.skydoves.landscapist.core.ImageRequest
import com.skydoves.landscapist.core.model.CachePolicy
import com.skydoves.landscapist.image.LandscapistImage
import kotlin.math.absoluteValue

@Composable
fun StampsScreen(
    modifier: Modifier = Modifier,
    stamps: State<List<StampListItem>>,
) = Box(
    modifier = modifier
        .paperBackground(),
) {
    val gridState = rememberLazyGridState()
    val spacedBy = Arrangement.spacedBy(24.dp)
    val imageRequestBuilder = retain {
        fun ImageRequest.Builder.() {
            diskCachePolicy(CachePolicy.DISABLED)
        }
    }
    val shadowColor = Color(0x7447525E)
    val rotationAngles = arrayOf(4, 3, 2, -2, -3, -4)

    LazyVerticalGrid(
        columns = GridCells.Adaptive(
            minSize = StampSize.width * 1.05f,
        ),
        verticalArrangement = spacedBy,
        contentPadding = WindowInsets
            .safeContent
            .asPaddingValues(),
        state = gridState,
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(
            items = stamps.value,
            key = StampListItem::key,
        ) { stamp ->
            Box(
                contentAlignment = Alignment.Center
            ) {
                LandscapistImage(
                    imageModel = { stamp.thumbnailUrl.toUri() },
                    requestBuilder = imageRequestBuilder,
                    modifier = Modifier
                        .size(StampSize)
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
                        .run {
                            if (stamp.thumbnailUrl.isNotEmpty()) {
                                return@run this
                            }

                            background(Color.Yellow)
                        }
                )
            }
        }
    }
}

@Preview
@Composable
private fun StampsScreenPreview(

) {
    val stamps = (1..5).map { i ->
        StampListItem(
            thumbnailUrl = "",
            key = i.toString(),
        )
    }

    StampsScreen(
        stamps = stamps.let(::mutableStateOf),
        modifier = Modifier
            .fillMaxSize()
    )
}
