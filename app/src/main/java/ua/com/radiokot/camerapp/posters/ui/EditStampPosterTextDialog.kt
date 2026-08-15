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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreInterceptKeyBeforeSoftKeyboard
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ua.com.radiokot.camerapp.R
import ua.com.radiokot.camerapp.posters.domain.StampPosterLayer
import ua.com.radiokot.camerapp.posters.domain.drawStampPosterTextBackground
import ua.com.radiokot.camerapp.ui.LeField
import ua.com.radiokot.camerapp.ui.rememberLeFieldTextStyle
import ua.com.radiokot.camerapp.ui.AppTheme
import ua.com.radiokot.camerapp.ui.LocalColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditStampPosterTextDialog(
    inputState: TextFieldState,
    appearance: StampPosterLayer.Text.Appearance,
    onChangeBackgroundAction: () -> Unit,
    onChangeAlignmentAction: () -> Unit,
    onDone: () -> Unit,
) = Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
        .fillMaxSize()
        .safeContentPadding()
        .imePadding()
        .padding(24.dp)
) {
    val focusRequester = remember(::FocusRequester)
    val textStyle = rememberLeFieldTextStyle(
        color = Color.Unspecified,
        textAlign = appearance.alignment.textAlign,
    )
    val textMeasurer = rememberTextMeasurer()
    val textColors = appearance.background?.colors
        ?: LocalColors.current
    val fontScale = LocalDensity.current.fontScale

    CompositionLocalProvider(
        LocalColors provides textColors
    ) {
        LeField(
            hint = "A text",
            inputState = inputState,
            focusRequester = focusRequester,
            isSingleLine = false,
            textAlign = appearance.alignment.textAlign,
            modifier = Modifier
                // Handle Back when the keyboard is shown.
                .onPreInterceptKeyBeforeSoftKeyboard { keyEvent ->
                    if (keyEvent.key == Key.Back && keyEvent.type == KeyEventType.KeyDown) {
                        onDone()
                        true
                    } else {
                        false
                    }
                }
                .run {
                    val currentBackground = appearance.background
                        ?: return@run this

                    drawWithCache {
                        val textString = inputState.text.toString()

                        val textLayout =
                            textMeasurer.measure(
                                text = textString,
                                style = textStyle,
                            )
                        val backgroundPath =
                            StampPosterLayer.Text.createBackgroundPath(
                                textLayout = textLayout,
                                scale = fontScale,
                            )

                        onDrawBehind {
                            // Do not draw tiny background behind a hint.
                            if (textString.isEmpty()) {
                                return@onDrawBehind
                            }

                            val x = when (appearance.alignment) {
                                StampPosterLayer.Text.Alignment.Left ->
                                    0f

                                StampPosterLayer.Text.Alignment.Center ->
                                    (size.width - textLayout.size.width) / 2f

                                StampPosterLayer.Text.Alignment.Right ->
                                    size.width - textLayout.size.width
                            }
                            drawContext.canvas.translate(x, 0f)
                            drawStampPosterTextBackground(
                                path = backgroundPath,
                                color = currentBackground.colors.componentBackground,
                                scale = fontScale,
                            )
                            drawContext.canvas.translate(-x, 0f)
                        }
                    }
                }
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterHorizontally,
        ),
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
    ) {
        AlignmentButton(
            currentAlignment = appearance.alignment,
            onClick = onChangeAlignmentAction,
        )
        BackgroundButton(
            currentBackground = appearance.background,
            onClick = onChangeBackgroundAction,
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun AlignmentButton(
    currentAlignment: StampPosterLayer.Text.Alignment,
    onClick: () -> Unit,
) = StampPosterActionButton(
    onClick = onClick,
) {
    Image(
        painter = painterResource(
            when (currentAlignment) {
                StampPosterLayer.Text.Alignment.Left ->
                    R.drawable.left_alignment_by_gregor_cresnar_from_noun_project

                StampPosterLayer.Text.Alignment.Center ->
                    R.drawable.center_alignment_by_gregor_cresnar_from_noun_project

                StampPosterLayer.Text.Alignment.Right ->
                    R.drawable.right_alignment_by_gregor_cresnar_from_noun_project
            }
        ),
        contentDescription = "Text alignment",
        colorFilter = ColorFilter.tint(LocalColors.current.textPrimary),
        modifier = Modifier
            .size(26.dp)
    )
}

@Composable
private fun BackgroundButton(
    currentBackground: StampPosterLayer.Text.Background?,
    onClick: () -> Unit,
) = StampPosterActionButton(
    onClick = onClick,
) {
    val colors = currentBackground?.colors
        ?: LocalColors.current

    CompositionLocalProvider(
        LocalColors provides colors
    ) {
        if (currentBackground != null) {
            Spacer(
                Modifier
                    .fillMaxSize()
                    .background(colors.componentBackground)
            )
        }

        BasicText(
            text = "A",
            style = stampPosterActionButtonTextStyle(),
        )
    }
}

@Preview
@Composable
private fun EditStampPosterTextDialogPreview() {
    AppTheme {
        EditStampPosterTextDialog(
            inputState = TextFieldState(),
            appearance = StampPosterLayer.Text.Appearance(
                background = null,
                alignment = StampPosterLayer.Text.Alignment.Center,
            ),
            onChangeBackgroundAction = {},
            onChangeAlignmentAction = {},
            onDone = {},
        )
    }
}
