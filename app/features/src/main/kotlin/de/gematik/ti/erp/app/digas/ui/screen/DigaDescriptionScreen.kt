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

package de.gematik.ti.erp.app.digas.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.digas.navigation.DigasRoutes
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode

class DigaDescriptionScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val title = remember {
            navBackStackEntry.arguments?.getString(DigasRoutes.DIGAS_NAV_TITLE)
        }
        val description = remember {
            navBackStackEntry.arguments?.getString(DigasRoutes.DIGAS_NAV_DESCRIPTION)
        }

        val listState = rememberLazyListState()

        BackHandler { navController.navigateUp() }
        AnimatedElevationScaffold(
            topBarTitle = stringResource(R.string.description),
            listState = listState,
            actions = {},
            navigationMode = NavigationBarMode.Back,
            onBack = { navController.navigateUp() },
            topBarPadding = PaddingValues(end = PaddingDefaults.Medium)
        ) {
            Column(Modifier.padding(PaddingDefaults.Large)) {
                Text(
                    title ?: stringResource(R.string.diga_description_screen_title),
                    style = AppTheme.typography.subtitle1
                )
                SpacerLarge()
                Text(
                    description ?: stringResource(R.string.diga_description_screen_information_missing),
                    style = AppTheme.typography.subtitle2l
                )
            }
        }
    }
}
