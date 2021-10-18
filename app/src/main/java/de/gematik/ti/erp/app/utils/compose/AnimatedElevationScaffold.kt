/*
 * Copyright (c) 2021 gematik GmbH
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
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedElevationScaffold(
    modifier: Modifier = Modifier,
    topBarColor: Color = MaterialTheme.colors.surface,
    navigationMode: NavigationBarMode = NavigationBarMode.Close,
    bottomBar: @Composable () -> Unit = {},
    topBarTitle: String,
    scrollPosition: Int,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    // this threshold defines the minimum scroll distance after which the top app bar gets elevated
    val threshold = with(LocalDensity.current) {
        10.dp.roundToPx()
    }

    var topBarElevation = 0.dp

    topBarElevation = when {
        scrollPosition > threshold -> AppBarDefaults.TopAppBarElevation
        scrollPosition < threshold / 2 -> 0.dp // divided by 2 leaves enough space to avoid flickering
        else -> topBarElevation
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            NavigationTopAppBar(
                navigationMode = navigationMode,
                backgroundColor = topBarColor,
                headline = topBarTitle,
                elevation = topBarElevation,
                onClick = onBack
            )
        },
        bottomBar = bottomBar,
        content = content
    )
}
