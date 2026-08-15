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

package ua.com.radiokot.camerapp.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreInterceptKeyBeforeSoftKeyboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LeField(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember(::FocusRequester),
    isEnabled: Boolean = true,
    isSingleLine: Boolean = true,
    textAlign: TextAlign = TextAlign.Center,
    scrollState: ScrollState = rememberScrollState(),
    inputState: TextFieldState,
    hint: String,
) = Box(
    contentAlignment = when (textAlign) {
        TextAlign.Left -> AbsoluteAlignment.CenterLeft
        TextAlign.Right -> AbsoluteAlignment.CenterRight
        else -> Alignment.Center
    },
    modifier = modifier
) {
    val colors = LocalColors.current
    val hintStyle = rememberLeFieldTextStyle(
        color = colors.textInputHint,
        textAlign = textAlign,
    )
    val inputStyle = remember(hintStyle) {
        hintStyle.copy(
            color = colors.textPrimary,
        )
    }
    val focusManager = LocalFocusManager.current

    val isHintVisible by remember(isEnabled) {
        derivedStateOf {
            isEnabled && inputState.text.isEmpty()
        }
    }

    if (isHintVisible) {
        BasicText(
            text = hint,
            style = hintStyle,
            modifier = Modifier
                .clickable(
                    enabled = isEnabled,
                    onClick = {
                        focusRequester.requestFocus()
                    }
                )
        )
    }

    BasicTextField(
        state = inputState,
        textStyle = inputStyle,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            keyboardType = KeyboardType.Text,
            showKeyboardOnFocus = true,
            imeAction =
                if (isSingleLine)
                    ImeAction.Done
                else
                    ImeAction.Default,
        ),
        lineLimits =
            if (isSingleLine)
                TextFieldLineLimits.SingleLine
            else
                TextFieldLineLimits.Default,
        onKeyboardAction = {
            // Done.
            focusManager.clearFocus()
        },
        cursorBrush = SolidColor(colors.textInputCursor),
        enabled = isEnabled,
        readOnly = !isEnabled,
        scrollState = scrollState,
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .widthIn(
                min = 10.dp,
            )
            .onPreInterceptKeyBeforeSoftKeyboard { keyEvent ->
                if (keyEvent.key == Key.Back) {
                    // Done.
                    focusManager.clearFocus()
                    return@onPreInterceptKeyBeforeSoftKeyboard true
                }
                false
            }
            .focusRequester(focusRequester)
    )
}

@Composable
fun rememberLeFieldTextStyle(
    color: Color,
    textAlign: TextAlign,
) = remember(color, textAlign) {
    TextStyle(
        fontFamily = PodkovaFamily,
        fontSize = 24.sp,
        color = color,
        textAlign = textAlign,
    )
}

@PreviewLightDark
@Composable
private fun LeFieldPreview() {
    AppTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .paperBackground(
                    drawBackgroundColor = true,
                )
                .padding(24.dp)
        ) {
            LeField(
                inputState = TextFieldState(""),
                hint = "A hint",
            )

            LeField(
                inputState = TextFieldState("My stamp"),
                hint = "A hint",
            )
        }
    }
}
