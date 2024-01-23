/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.analytics.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.analytics.presentation.DemoTrackerScreenViewModel
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import org.kodein.di.compose.rememberInstance

class DemoTrackerScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        val viewModel by rememberInstance<DemoTrackerScreenViewModel>()
        val screens by viewModel.session.collectAsStateWithLifecycle()
        DemoTrackerScreenContent(screens)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoTrackerScreenContent(screens: List<String>) {
    Scaffold(
        containerColor = AppTheme.colors.neutral025,
        contentColor = AppTheme.colors.neutral999,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults
                    .topAppBarColors(
                        containerColor = AppTheme.colors.neutral025
                    ),
                title = {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PaddingDefaults.Medium),
                        color = AppTheme.colors.neutral999,
                        text = "Tracked Screens"
                    )
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.padding(it)
        ) {
            items(screens) { screen ->
                SpacerMedium()
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PaddingDefaults.Medium),
                    // • = \u2022,
                    // ● = \u25CF,
                    // ○ = \u25CB,
                    // ▪ = \u25AA,
                    // ■ = \u25A0,
                    // □ = \u25A1,
                    // ► = \u25BA
                    text = "\u25BA $screen"
                )
            }
            item { SpacerMedium() }
            item { Divider() }
        }
    }
}

@LightDarkPreview
@Composable
fun DemoTrackerScreenContentPreview() {
    PreviewAppTheme {
        DemoTrackerScreenContent(listOf("1", "2", "3"))
    }
}
