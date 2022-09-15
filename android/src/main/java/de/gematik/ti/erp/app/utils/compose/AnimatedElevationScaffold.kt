/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedElevationScaffold(
    modifier: Modifier = Modifier,
    topBarColor: Color = MaterialTheme.colors.surface,
    navigationMode: NavigationBarMode = NavigationBarMode.Close,
    bottomBar: @Composable () -> Unit = {},
    topBarTitle: String,
    listState: LazyListState,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            NavigationTopAppBar(
                navigationMode = navigationMode,
                backgroundColor = topBarColor,
                title = topBarTitle,
                elevation = if (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0) {
                    AppBarDefaults.TopAppBarElevation
                } else {
                    0.dp
                },
                onBack = onBack
            )
        },
        bottomBar = bottomBar,
        content = content
    )
}

@Composable
fun AnimatedElevationScaffold(
    modifier: Modifier = Modifier,
    topBarColor: Color = MaterialTheme.colors.surface,
    navigationMode: NavigationBarMode = NavigationBarMode.Close,
    bottomBar: @Composable () -> Unit = {},
    topBarTitle: String,
    elevated: Boolean,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val elevation = remember(elevated) { if (elevated) AppBarDefaults.TopAppBarElevation else 0.dp }

    Scaffold(
        modifier = modifier,
        topBar = {
            NavigationTopAppBar(
                navigationMode = navigationMode,
                backgroundColor = topBarColor,
                title = topBarTitle,
                elevation = elevation,
                onBack = onBack
            )
        },
        bottomBar = bottomBar,
        content = content
    )
}
