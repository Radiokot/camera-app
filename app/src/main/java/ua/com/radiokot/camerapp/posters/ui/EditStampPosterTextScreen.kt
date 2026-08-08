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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import ua.com.radiokot.camerapp.stamps.ui.CaptionInput
import ua.com.radiokot.camerapp.ui.DarkAppColors
import ua.com.radiokot.camerapp.ui.LocalColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditStampPosterTextScreen(
    inputState: TextFieldState,
    onDone: () -> Unit,
) = Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
        .safeContentPadding()
) {
    val focusRequester = remember(::FocusRequester)
    var doneOnFocusLoss by remember { mutableStateOf(false) }

    CompositionLocalProvider(
        LocalColors provides DarkAppColors,
    ) {
        CaptionInput(
            hint = "A text",
            inputState = inputState,
            focusRequester = focusRequester,
            isSingleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (!it.hasFocus && doneOnFocusLoss) {
                        onDone()
                    }
                }
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        doneOnFocusLoss = true
    }

    BackHandler(
        onBack = onDone,
    )
}
