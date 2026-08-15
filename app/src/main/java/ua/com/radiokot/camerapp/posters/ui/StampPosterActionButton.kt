package ua.com.radiokot.camerapp.posters.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.com.radiokot.camerapp.ui.LocalColors
import ua.com.radiokot.camerapp.ui.PodkovaFamily

@Composable
fun StampPosterActionButton(
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) = Box(
    contentAlignment = Alignment.Center,
    content = content,
    modifier = Modifier
        .size(48.dp)
        .border(
            width = 2.dp,
            color = LocalColors.current.componentStroke,
            shape = RoundedCornerShape(8.dp),
        )
        .padding(1.dp)
        .clip(
            shape = RoundedCornerShape(8.dp),
        )
        .clickable(
            onClick = onClick,
        )
)

@Composable
fun stampPosterActionButtonTextStyle() =
    TextStyle(
        fontFamily = PodkovaFamily,
        color = LocalColors.current.textPrimary,
        fontSize = 22.sp,
    )
