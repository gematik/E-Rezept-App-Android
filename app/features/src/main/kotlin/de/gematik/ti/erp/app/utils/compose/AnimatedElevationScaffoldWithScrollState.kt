/*
 * Copyright 2024, gematik GmbH
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
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.theme.SizeDefaults

// This AnimatedElevationScaffoldWithScrollState is used when the scroll state is fed from the child composable
@Composable
fun AnimatedElevationScaffoldWithScrollState(
    modifier: Modifier = Modifier,
    topBarTitle: String,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    topBarColor: Color = MaterialTheme.colors.surface,
    navigationMode: NavigationBarMode? = NavigationBarMode.Close,
    elevated: Boolean,
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    bottomBar: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val padding = (LocalActivity.current as? BaseActivity)?.applicationInnerPadding

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            NavigationTopAppBar(
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
