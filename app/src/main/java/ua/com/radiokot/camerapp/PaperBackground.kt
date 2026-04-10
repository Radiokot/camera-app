package ua.com.radiokot.camerapp

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.ranges.step

fun Modifier.paperBackground(

) = drawWithCache {

    val backgroundColor = Color(0xfffef6eb)
    val lineColor = Color(0xFFEEEDE6)
    val gridSize = 20.dp.roundToPx()
    val gridThickness = 1.dp.toPx()

    onDrawWithContent {
        drawRect(backgroundColor)

        for (y in ((0 - gridSize / 2)..size.height.toInt() step gridSize)) {
            drawLine(
                color = lineColor,
                start = Offset(
                    x = 0f,
                    y = y.toFloat(),
                ),
                end = Offset(
                    x = size.width,
                    y = y.toFloat(),
                ),
                strokeWidth = gridThickness,
            )
        }
        for (x in (0..size.width.toInt() step gridSize)) {
            drawLine(
                color = lineColor,
                start = Offset(
                    x = x.toFloat(),
                    y = 0f,
                ),
                end = Offset(
                    x = x.toFloat(),
                    y = size.height,
                ),
                strokeWidth = gridThickness,
            )
        }

        drawContent()
    }
}
