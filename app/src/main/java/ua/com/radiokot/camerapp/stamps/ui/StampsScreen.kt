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

package ua.com.radiokot.camerapp.stamps.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import ua.com.radiokot.camerapp.R
import ua.com.radiokot.camerapp.posters.domain.StampPosterMaxStamps
import ua.com.radiokot.camerapp.ui.AppTheme
import ua.com.radiokot.camerapp.ui.LeField
import ua.com.radiokot.camerapp.ui.LeTextButton
import ua.com.radiokot.camerapp.ui.LocalColors
import ua.com.radiokot.camerapp.ui.PodkovaFamily
import ua.com.radiokot.camerapp.ui.Vignette
import ua.com.radiokot.camerapp.ui.paperBackground
import ua.com.radiokot.camerapp.util.barsAndCutout
import ua.com.radiokot.camerapp.util.doNothing
import ua.com.radiokot.camerapp.util.optionalSharedBounds
import ua.com.radiokot.camerapp.util.optionalSharedElement
import ua.com.radiokot.camerapp.util.plus

@Composable
fun StampsScreen(
    modifier: Modifier = Modifier,
    collectionId: String,
    collectionNameInputState: TextFieldState,
    focusCollectionNameInput: Boolean,
    showGiftMessage: Boolean,
    stamps: State<ImmutableList<StampsGridItem>>,
    selectedStampKeys: State<ImmutableSet<String>>,
    onStampClicked: (StampsGridItem) -> Unit,
    onStampLongClicked: (StampsGridItem) -> Unit,
    onMoveSelectedAction: () -> Unit,
    onSendSelectedAsEnvelopeAction: () -> Unit,
    onSendSelectedAsPosterAction: () -> Unit,
    onDeleteSelectedAction: () -> Unit,
    onNewStampAction: () -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) = Box(
    modifier
        .optionalSharedBounds(
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            contentKey = "$collectionId-box-front",
            zIndexInOverlay = 10f,
        )
) {
    val barsAndCutoutPadding =
        WindowInsets.barsAndCutout.asPaddingValues()
    val contentPadding =
        barsAndCutoutPadding + PaddingValues(
            bottom = 120.dp,
        )

    val nameInputFocusRequester = remember(::FocusRequester)
    if (focusCollectionNameInput) {
        LaunchedEffect(Unit) {
            nameInputFocusRequester.requestFocus()
        }
    }

    LazyVerticalGrid(
        columns = GridCells.FixedSize(StampContainerBaseSize.width * 1.15f),
        horizontalArrangement = Arrangement.SpaceAround,
        contentPadding = contentPadding,
        overscrollEffect = null,
        modifier = Modifier
            .fillMaxSize()
    ) {
        item(
            span = {
                GridItemSpan(maxCurrentLineSpan)
            },
            key = "name",
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 24.dp,
                    )
            ) {
                LeField(
                    hint = "A name",
                    focusRequester = nameInputFocusRequester,
                    inputState = collectionNameInputState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .optionalSharedElement(
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            contentKey = "$collectionId-name",
                            zIndexInOverlay = 20f,
                        )
                )

                Vignette(
                    modifier = Modifier
                        .padding(
                            top = 32.dp,
                        )
                )

                if (showGiftMessage) {
                    BasicText(
                        text = "Please take these stamps as a gift. " +
                                "I hope you'll enjoy collecting your own!",
                        style = TextStyle(
                            fontFamily = PodkovaFamily,
                            fontSize = 16.sp,
                            color = LocalColors.current.textSecondary,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 32.dp,
                                start = 24.dp,
                                end = 24.dp,
                            )
                    )
                }
            }
        }

        stampItems(
            items = stamps.value,
            selectedItemKeys = selectedStampKeys.value,
            onClicked = onStampClicked,
            onLongClicked = onStampLongClicked,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    var visibleSelectedCount by rememberSaveable {
        mutableIntStateOf(0)
    }
    var areSelectionActionsVisible by rememberSaveable {
        mutableStateOf(false)
    }
    val isSelectionVisible by remember {
        derivedStateOf {
            if (selectedStampKeys.value.isNotEmpty()) {
                visibleSelectedCount = selectedStampKeys.value.size
                true
            } else {
                areSelectionActionsVisible = false
                false
            }
        }
    }

    AnimatedVisibility(
        visible = areSelectionActionsVisible,
        enter =
            fadeIn() + slideInVertically(
                initialOffsetY = { height ->
                    height / 2
                },
            ),
        exit = fadeOut(),
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(contentPadding)
            .padding(
                horizontal = 24.dp,
            )
            .width(StampContainerBaseSize.width * 2.5f)
    ) {
        SelectionActions(
            selectedCount = visibleSelectedCount,
            onMove = {
                areSelectionActionsVisible = false
                onMoveSelectedAction()
            },
            onSendAsEnvelope = {
                areSelectionActionsVisible = false
                onSendSelectedAsEnvelopeAction()
            },
            onSendAsPoster = {
                areSelectionActionsVisible = false
                onSendSelectedAsPosterAction()
            },
            onDelete = {
                areSelectionActionsVisible = false
                onDeleteSelectedAction()
            },
            modifier = Modifier
                .fillMaxWidth()
        )

        BackHandler {
            areSelectionActionsVisible = false
        }
    }

    AnimatedContent(
        targetState = isSelectionVisible,
        modifier = Modifier
            .padding(barsAndCutoutPadding)
            .padding(24.dp)
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
    ) { isSelectionVisible ->
        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (isSelectionVisible) {
                SelectionController(
                    selectedCount = visibleSelectedCount,
                    onActionsClicked = {
                        areSelectionActionsVisible = !areSelectionActionsVisible
                    },
                    modifier = Modifier
                        .padding(
                            bottom = 10.dp,
                        )
                )
            } else {
                LeTextButton(
                    text = "New Stamp",
                    onClick = onNewStampAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .optionalSharedElement(
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            contentKey = "new-stamp-button",
                            zIndexInOverlay = 30f,
                        )
                )
            }
        }
    }
}

@Composable
private fun SelectionController(
    modifier: Modifier = Modifier,
    selectedCount: Int,
    onActionsClicked: () -> Unit,
) {
    val colors = LocalColors.current
    val cornerRadius = 10.dp
    val textStyle = remember(colors) {
        TextStyle(
            fontFamily = PodkovaFamily,
            fontSize = 20.sp,
            color = colors.textPrimary,
        )
    }
    val density = LocalDensity.current
    val spDp = remember(density) {
        with(density) {
            1.sp.toDp()
        }
    }

    Row(
        modifier = modifier
            .background(
                color = colors.componentBackground,
                shape = RoundedCornerShape(cornerRadius),
            )
            .border(
                width = 2.dp,
                color = colors.componentStroke,
                shape = RoundedCornerShape(cornerRadius),
            )
            .height(IntrinsicSize.Max)
    ) {
        Spacer(
            modifier = Modifier
                .width(24.dp)
        )

        BasicText(
            text = "Picked ",
            style = textStyle,
            modifier = Modifier
                .padding(
                    vertical = 16.dp,
                )
        )

        // All the width shenanigans are to prevent slight width changes
        // due to variable digit width.
        val selectedCountString = selectedCount.toString()
        BasicText(
            text = selectedCountString,
            style = textStyle,
            modifier = Modifier
                .padding(
                    vertical = 16.dp,
                )
                .width(24.dp + (spDp * 10f * selectedCountString.length))
        )

        Spacer(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(colors.componentDivider)
        )

        Image(
            painter = painterResource(R.drawable.ic_more_vert),
            contentDescription = "Actions",
            colorFilter = ColorFilter.tint(colors.textPrimary),
            modifier = Modifier
                .fillMaxHeight()
                .clickable(
                    onClick = onActionsClicked,
                )
                .padding(
                    start = 14.dp,
                    end = 16.dp,
                )
        )
    }
}

@Composable
@PreviewLightDark
private fun SelectionControllerPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .paperBackground(
                    drawBackgroundColor = true,
                )
                .padding(24.dp)
        ) {
            SelectionController(
                selectedCount = 24,
                onActionsClicked = ::doNothing,
            )
        }
    }
}

@Composable
private fun SelectionActions(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    selectedCount: Int,
    onMove: () -> Unit,
    onSendAsEnvelope: () -> Unit,
    onSendAsPoster: () -> Unit,
    onDelete: () -> Unit,
) = Column(
    modifier = modifier
        .background(
            color = LocalColors.current.componentBackground,
            shape = RoundedCornerShape(cornerRadius),
        )
        .border(
            width = 2.dp,
            color = LocalColors.current.componentStroke,
            shape = RoundedCornerShape(cornerRadius),
        )
        .clip(RoundedCornerShape(cornerRadius))
) {
    val colors = LocalColors.current
    val textStyle = remember {
        TextStyle(
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            fontFamily = PodkovaFamily,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
        )
    }

    var submenu by remember {
        mutableIntStateOf(0)
    }

    AnimatedContent(
        targetState = submenu,
        modifier = Modifier
            .fillMaxWidth()
    ) { currentSubmenu ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (currentSubmenu == 1) {
                BasicText(
                    text = "Send stamps as",
                    style = textStyle.copy(
                        fontWeight = FontWeight.Normal,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = 20.dp,
                        )
                )

                Spacer(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                        .background(colors.componentDivider)
                )

                BasicText(
                    text = "An envelope",
                    style = textStyle,
                    modifier = Modifier
                        .clickable(
                            onClick = onSendAsEnvelope,
                        )
                        .padding(
                            vertical = 20.dp,
                        )
                        .fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                        .background(colors.componentDivider)
                )

                Column(
                    modifier = Modifier
                        .clickable(
                            onClick = onSendAsPoster,
                        )
                ) {
                    BasicText(
                        text = "A poster",
                        style = textStyle,
                        modifier = Modifier
                            .padding(
                                top = 20.dp,
                            )
                            .fillMaxWidth()
                    )
                    if (selectedCount > StampPosterMaxStamps) {
                        BasicText(
                            text = "But only $StampPosterMaxStamps of them",
                            style = TextStyle(
                                fontFamily = PodkovaFamily,
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                                color = LocalColors.current.textSecondary,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 2.dp,
                                    bottom = 20.dp,
                                )
                        )
                    } else {
                        Spacer(Modifier.height(20.dp))
                    }
                }

                BackHandler { submenu = 0 }

                return@Column
            }
            BasicText(
                text = "Move",
                style = textStyle,
                modifier = Modifier
                    .clickable(
                        onClick = onMove,
                    )
                    .padding(
                        vertical = 20.dp,
                    )
                    .fillMaxWidth()
            )

            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(colors.componentDivider)
            )

            BasicText(
                text = "Send",
                style = textStyle,
                modifier = Modifier
                    .clickable(
                        onClick = {
                            submenu = 1
                        },
                    )
                    .padding(
                        vertical = 20.dp,
                    )
                    .fillMaxWidth()
            )

            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(colors.componentDivider)
            )

            BasicText(
                text = "Hold to delete",
                style = textStyle.copy(
                    color = colors.textDanger,
                ),
                modifier = Modifier
                    .holdToDeleteAction(
                        roundedCornerRadius = cornerRadius,
                        areTopCornersRounded = false,
                        onDelete = onDelete,
                    )
                    .padding(
                        vertical = 20.dp,
                    )
                    .fillMaxWidth()
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SelectionActionsPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .paperBackground(
                    drawBackgroundColor = true,
                )
                .padding(24.dp)
        ) {
            SelectionActions(
                onMove = ::doNothing,
                onSendAsEnvelope = ::doNothing,
                onSendAsPoster = ::doNothing,
                selectedCount = 24,
                onDelete = ::doNothing,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun StampsScreenPreview() {
    AppTheme {
        val stamps = remember {
            (1..6)
                .map { i ->
                    StampsGridItem(
                        imageUri = "",
                        shape = UiStampShapeA,
                        key = i.toString(),
                    )
                }
                .toPersistentList()
        }

        StampsScreen(
            collectionId = "",
            collectionNameInputState = TextFieldState("My stamps"),
            focusCollectionNameInput = false,
            showGiftMessage = true,
            stamps = stamps.let(::mutableStateOf),
            selectedStampKeys = remember { mutableStateOf(persistentSetOf()) },
            onStampClicked = ::doNothing,
            onStampLongClicked = ::doNothing,
            onMoveSelectedAction = ::doNothing,
            onSendSelectedAsEnvelopeAction = ::doNothing,
            onSendSelectedAsPosterAction = ::doNothing,
            onDeleteSelectedAction = ::doNothing,
            onNewStampAction = ::doNothing,
            sharedTransitionScope = null,
            animatedVisibilityScope = null,
            modifier = Modifier
                .fillMaxSize()
                .paperBackground(
                    drawBackgroundColor = true,
                )
        )
    }
}
