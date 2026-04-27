package ua.com.radiokot.camerapp.stamps.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.com.radiokot.camerapp.ui.podkovaFamily

@Composable
fun StampCaptionInput(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember(::FocusRequester),
    isEnabled: Boolean = true,
    inputState: State<String?>,
    onInputChanged: (String) -> Unit,
) = Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
) {
    val hintStyle = remember {
        TextStyle(
            fontFamily = podkovaFamily,
            fontSize = 24.sp,
            color = Color(0xFFB9AC8C),
            textAlign = TextAlign.Center,
        )
    }
    val inputStyle = remember {
        hintStyle.copy(
            color = Color.Unspecified,
        )
    }
    val focusManager = LocalFocusManager.current

    val isCaptionHintVisible by remember(isEnabled) {
        derivedStateOf {
            isEnabled && inputState.value.isNullOrBlank()
        }
    }

    if (isCaptionHintVisible) {
        BasicText(
            text = "A caption",
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
        value = inputState.value ?: "",
        onValueChange = onInputChanged,
        textStyle = inputStyle,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            keyboardType = KeyboardType.Text,
            showKeyboardOnFocus = true,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
            },
        ),
        enabled = isEnabled,
        readOnly = !isEnabled,
        modifier = Modifier
            .focusRequester(focusRequester)
    )
}

@Preview
@Composable
private fun StampCaptionInputPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        StampCaptionInput(
            inputState = "".let(::mutableStateOf),
            onInputChanged = {}
        )

        StampCaptionInput(
            inputState = "My stamp".let(::mutableStateOf),
            onInputChanged = {}
        )
    }
}
