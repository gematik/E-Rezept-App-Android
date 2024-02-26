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

package de.gematik.ti.erp.app.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.utils.extensions.getUriDataTerms
import de.gematik.ti.erp.app.webview.WebViewScreen

@Requirement(
    "O.Purp_1#2",
    "O.Arch_8#6",
    "O.Plat_11#6",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Display data privacy as part of the onboarding. " +
        "Webview containing local html without javascript."
)
class SettingsDataProtectionScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        WebViewScreen(
            title = stringResource(R.string.onb_data_consent),
            onBack = navController::popBackStack,
            url = getUriDataTerms()
        )
    }
}
