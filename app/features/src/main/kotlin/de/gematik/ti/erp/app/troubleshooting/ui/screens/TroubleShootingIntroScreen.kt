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

package de.gematik.ti.erp.app.troubleshooting.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.troubleshooting.navigation.TroubleShootingRoutes
import de.gematik.ti.erp.app.troubleshooting.ui.components.TroubleShootingNextTipButton
import de.gematik.ti.erp.app.troubleshooting.ui.components.TroubleShootingScaffold
import de.gematik.ti.erp.app.troubleshooting.ui.components.TroubleShootingTip
import de.gematik.ti.erp.app.troubleshooting.ui.components.TroubleShootingTryMeButton
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

class TroubleShootingIntroScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        TroubleShootingScaffold(
            title = stringResource(R.string.cdw_troubleshooting_page_a_title),
            onBack = { navController.popBackStack() },
            bottomBarButton = {
                TroubleShootingNextTipButton(
                    onClick = { navController.navigate(TroubleShootingRoutes.TroubleShootingDeviceOnTopScreen.path()) }
                )
            }
        ) {
            TroubleShootingIntroScreenContent(
                onClickTryMe = {
                    navController.popBackStack(TroubleShootingRoutes.TroubleShootingIntroScreen.route, inclusive = true)
                }
            )
        }
    }
}

@Composable
private fun TroubleShootingIntroScreenContent(
    onClickTryMe: () -> Unit
) {
    Column {
        TroubleShootingTip(stringResource(R.string.cdw_troubleshooting_page_a_tip1))
        SpacerMedium()
        TroubleShootingTip(stringResource(R.string.cdw_troubleshooting_page_a_tip2))
        SpacerMedium()
        TroubleShootingTip(stringResource(R.string.cdw_troubleshooting_page_a_tip3))
        SpacerLarge()
        TroubleShootingTryMeButton {
            onClickTryMe()
        }
    }
}

@LightDarkPreview
@Composable
fun TroubleShootingIntroScreenPreview() {
    PreviewAppTheme {
        TroubleShootingScaffold(
            title = stringResource(R.string.cdw_troubleshooting_page_a_title),
            onBack = {},
            bottomBarButton = {
                TroubleShootingNextTipButton(
                    onClick = {}
                )
            }
        ) {
            TroubleShootingIntroScreenContent(
                onClickTryMe = {}
            )
        }
    }
}