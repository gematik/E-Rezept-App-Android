/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.digas.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.digas.navigation.DigasRoutes
import de.gematik.ti.erp.app.navigation.BottomSheetScreen
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.extensions.openUriWhenValid

class DigaHelpAndSupportBottomSheetScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : BottomSheetScreen(forceToMaxHeight = false) {
    @Composable
    override fun Content() {
        val urlLink = remember { navBackStackEntry.arguments?.getString(DigasRoutes.DIGAS_NAV_LINK) }
        if (urlLink != null) {
            DigaSupportBottomSheetScreenContent(urlLink)
        } else {
            ErrorScreenComponent()
        }
    }
}

@Composable
private fun DigaSupportBottomSheetScreenContent(
    urlLink: String
) {
    val uriHandler = LocalUriHandler.current
    Column(
        Modifier
            .padding(horizontal = PaddingDefaults.Medium)
            .padding(top = PaddingDefaults.Small, bottom = PaddingDefaults.XXLarge),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        SpacerMedium()
        Text(
            stringResource(R.string.help_and_support),
            style = AppTheme.typography.subtitle1,
            color = AppTheme.colors.neutral900
        )
        SpacerSmall()
        Row {
            Text(
                stringResource(
                    R.string.manufacturer_website_info,
                    urlLink
                ),
                style = AppTheme.typography.body2l
            )
        }
        SpacerMedium()
        PrimaryButtonSmall(
            onClick = { uriHandler.openUriWhenValid(urlLink) }
        ) {
            Text(stringResource(R.string.open_link))
        }
    }
}

@Suppress("UnusedPrivateMember")
@LightDarkPreview
@Composable
private fun DigaSupportBottomSheetScreenPreview() {
    PreviewTheme {
        DigaSupportBottomSheetScreenContent(
            urlLink = "https://digas.support.bottomsheet.com"
        )
    }
}
