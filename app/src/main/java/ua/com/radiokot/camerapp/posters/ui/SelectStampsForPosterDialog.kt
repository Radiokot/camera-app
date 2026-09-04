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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import ua.com.radiokot.camerapp.stamps.ui.StampContainerBaseSize
import ua.com.radiokot.camerapp.stamps.ui.StampsGridItem
import ua.com.radiokot.camerapp.stamps.ui.stampItems
import ua.com.radiokot.camerapp.ui.LeTextButton
import ua.com.radiokot.camerapp.util.barsAndCutout
import ua.com.radiokot.camerapp.util.barsAndCutoutPadding
import ua.com.radiokot.camerapp.util.doNothing
import ua.com.radiokot.camerapp.util.plus

@Composable
fun SelectStampsForPosterDialog(
    stamps: State<ImmutableList<StampsGridItem>>,
    selectedStampKeys: State<ImmutableSet<String>>,
    onStampClicked: (StampsGridItem) -> Unit,
    onAddSelectedAction: () -> Unit,
) = Box(
    modifier = Modifier
        .fillMaxSize()
) {
    val barsAndCutoutPadding =
        WindowInsets.barsAndCutout.asPaddingValues()
    val contentPadding =
        barsAndCutoutPadding + PaddingValues(
            bottom = 120.dp,
        )

    LazyVerticalGrid(
        columns = GridCells.FixedSize(StampContainerBaseSize.width * 1.15f),
        horizontalArrangement = Arrangement.SpaceAround,
        contentPadding = contentPadding,
        overscrollEffect = null,
        modifier = Modifier
            .fillMaxSize()
    ) {
        stampItems(
            items = stamps.value,
            selectedItemKeys = selectedStampKeys.value,
            onClicked = onStampClicked,
            onLongClicked = ::doNothing,
        )
    }

    LeTextButton(
        text = "Add selected",
        onClick = onAddSelectedAction,
        modifier = Modifier
            .barsAndCutoutPadding()
            .padding(24.dp)
            .align(Alignment.BottomCenter)
    )
}
