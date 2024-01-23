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

package de.gematik.ti.erp.app.debugsettings.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.components.ErezeptTopAppBar
import de.gematik.ti.erp.app.debugsettings.navigation.SampleScreenRoutes
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium

class SampleOverviewScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        BackHandler {
            navController.popBackStack()
        }
        Scaffold(
            topBar = {
                ErezeptTopAppBar
                    .Close(title = "UI Components") {
                        navController.popBackStack()
                    }
            }
        ) {
            SampleOverviewScreenContent(
                paddingValues = it,
                onAdaptableScreenClick = {
                    navController.navigate(SampleScreenRoutes.BottomSheetSampleScreen.path())
                },
                onLargeScreenClick = {
                    navController.navigate(SampleScreenRoutes.BottomSheetSampleLargeScreen.path())
                },
                onSmallScreenClick = {
                    navController.navigate(SampleScreenRoutes.BottomSheetSampleSmallScreen.path())
                }
            )
        }
    }
}

@Composable
private fun SampleOverviewScreenContent(
    paddingValues: PaddingValues,
    onAdaptableScreenClick: () -> Unit,
    onLargeScreenClick: () -> Unit,
    onSmallScreenClick: () -> Unit
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        item {
            SpacerLarge()
        }
        item {
            TitleInfoCard(
                onClick = onAdaptableScreenClick,
                title = "Adaptable bottom sheet",
                body = "The height of the content in this bottom sheet will adjust itself based on the content"
            )
        }
        item {
            SpacerMedium()
        }
        item {
            TitleInfoCard(
                onClick = onSmallScreenClick,
                title = "Small bottom sheet",
                body = "The height of this bottomsheet will not grow no matter how much content we add"
            )
        }
        item {
            SpacerMedium()
        }
        item {
            TitleInfoCard(
                onClick = onLargeScreenClick,
                title = "Full screen bottom sheet",
                body = "The height of this bottomsheet will take the full screen even if the content is not that much"
            )
        }
        item {
            SpacerMedium()
        }
    }
}

@LightDarkPreview
@Composable
fun SampleOverviewScreenPreview() {
    PreviewAppTheme {
        Scaffold {
            SampleOverviewScreenContent(
                onAdaptableScreenClick = {},
                onLargeScreenClick = {},
                onSmallScreenClick = {},
                paddingValues = it
            )
        }
    }
}
