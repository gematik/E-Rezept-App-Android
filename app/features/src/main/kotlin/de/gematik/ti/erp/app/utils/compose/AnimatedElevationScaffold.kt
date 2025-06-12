/*
 * Copyright 2025, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
fun AnimatedElevationScaffold(
    topBar: @Composable (Boolean) -> Unit,
    listState: LazyListState = rememberLazyListState(),
    content: @Composable (PaddingValues) -> Unit
) {
    val activity = LocalActivity.current as? BaseActivity
    val padding = remember { activity?.applicationInnerPadding }
    val elevated by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }
    Scaffold(
        topBar = {
            Surface(
                elevation = when {
                    elevated -> SizeDefaults.half
                    else -> SizeDefaults.zero
                }
            ) {
                topBar(elevated)
            }
        },
        content = { innerPadding ->
            content(padding?.combineWithInnerScaffold(innerPadding) ?: innerPadding)
        }
    )
}

@Composable
fun AnimatedElevationScaffold(
    modifier: Modifier = Modifier,
    topBarTitle: @Composable () -> Unit,
    topBarPadding: PaddingValues = PaddingValues.Absolute(
        SizeDefaults.zero,
        SizeDefaults.zero,
        SizeDefaults.zero,
        SizeDefaults.zero
    ),
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    topBarColor: Color = MaterialTheme.colors.surface,
    navigationMode: NavigationBarMode? = NavigationBarMode.Close,
    listState: LazyListState = rememberLazyListState(),
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    bottomBar: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val activity = LocalActivity.current as? BaseActivity
    val padding = remember { activity?.applicationInnerPadding }

    val elevated by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            NavigationTopAppBar(
                modifier = Modifier.padding(topBarPadding),
                navigationMode = navigationMode,
                backgroundColor = topBarColor,
                title = topBarTitle,
                elevation = if (elevated) AppBarDefaults.TopAppBarElevation else SizeDefaults.zero,
                onBack = onBack,
                actions = actions
            )
        },
        snackbarHost = snackbarHost,
        bottomBar = bottomBar,
        content = { innerPadding ->
            content(padding?.combineWithInnerScaffold(innerPadding) ?: innerPadding)
        }
    )
}

@Composable
fun AnimatedElevationScaffold(
    listState: LazyListState = rememberLazyListState(),
    topBar: @Composable () -> Unit,
    floatingActionButton: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val activity = LocalActivity.current as? BaseActivity
    val padding = remember { activity?.applicationInnerPadding }
    val elevated by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }
    Scaffold(
        topBar = {
            Surface(
                elevation = when {
                    elevated -> AppBarDefaults.TopAppBarElevation
                    else -> SizeDefaults.zero
                }
            ) {
                topBar()
            }
        },
        floatingActionButton = {
            floatingActionButton()
        },
        content = { innerPadding ->
            content(padding?.combineWithInnerScaffold(innerPadding) ?: innerPadding)
        }
    )
}

@Composable
fun AnimatedElevationScaffold(
    modifier: Modifier = Modifier,
    topBarTitle: String,
    topBarPadding: PaddingValues = PaddingValues.Absolute(
        SizeDefaults.zero,
        SizeDefaults.zero,
        SizeDefaults.zero,
        SizeDefaults.zero
    ),
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    topBarColor: Color = MaterialTheme.colors.surface,
    navigationMode: NavigationBarMode? = NavigationBarMode.Close,
    listState: LazyListState = rememberLazyListState(),
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    bottomBar: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val activity = LocalActivity.current as? BaseActivity
    val padding = remember { activity?.applicationInnerPadding }
    val elevated by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            NavigationTopAppBar(
                modifier = Modifier.padding(topBarPadding),
                navigationMode = navigationMode,
                backgroundColor = topBarColor,
                title = topBarTitle,
                elevation = when {
                    elevated -> AppBarDefaults.TopAppBarElevation
                    else -> SizeDefaults.zero
                },
                onBack = onBack,
                actions = actions
            )
        },
        snackbarHost = snackbarHost,
        bottomBar = bottomBar,
        content = { innerPadding ->
            content(padding?.combineWithInnerScaffold(innerPadding) ?: innerPadding)
        }
    )
}

// AnimatedElevationScaffold used it titles
@Composable
fun AnimatedElevationScaffold(
    modifier: Modifier = Modifier,
    topBarTitle: String,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    topBarColor: Color = MaterialTheme.colors.surface,
    listState: LazyListState = rememberLazyListState(),
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val activity = LocalActivity.current as? BaseActivity
    val padding = remember { activity?.applicationInnerPadding }
    val elevated by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier
                            .padding(
                                top = PaddingDefaults.Medium
                            ).semanticsHeading(),
                        text = topBarTitle,
                        style = AppTheme.typography.h5
                    )
                },
                actions = actions,
                backgroundColor = topBarColor,
                elevation = when {
                    elevated -> AppBarDefaults.TopAppBarElevation
                    else -> SizeDefaults.zero
                }
            )
        },
        snackbarHost = snackbarHost,
        bottomBar = bottomBar,
        content = { innerPadding ->
            content(padding?.combineWithInnerScaffold(innerPadding) ?: innerPadding)
        }
    )
}

// switch between AnimatedElevationScaffold with back button and different text
// and the one used for titles
@Composable
fun AnimatedElevationScaffold(
    topBarTitle: String,
    listState: LazyListState,
    isModalFlow: Boolean,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    if (isModalFlow) {
        AnimatedElevationScaffold(
            topBarTitle = topBarTitle,
            listState = listState,
            onBack = onBack,
            content = content
        )
    } else {
        AnimatedElevationScaffold(
            topBarTitle = topBarTitle,
            listState = listState,
            content = content
        )
    }
}
