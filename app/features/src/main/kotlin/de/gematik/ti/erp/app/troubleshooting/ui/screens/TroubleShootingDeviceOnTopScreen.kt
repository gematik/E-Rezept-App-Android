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

package de.gematik.ti.erp.app.troubleshooting.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.app_core.R
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

class TroubleShootingDeviceOnTopScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        TroubleShootingScaffold(
            title = stringResource(R.string.cdw_troubleshooting_page_b_title),
            onBack = { navController.popBackStack() },
            bottomBarButton = {
                TroubleShootingNextTipButton(
                    onClick = {
                        navController.navigate(TroubleShootingRoutes.TroubleShootingFindNfcPositionScreen.path())
                    }
                )
            }
        ) {
            TroubleShootingDeviceOnTopScreenContent(
                onClickTryMe = {
                    navController.popBackStack(TroubleShootingRoutes.TroubleShootingIntroScreen.route, inclusive = true)
                }
            )
        }
    }
}

@Composable
private fun TroubleShootingDeviceOnTopScreenContent(
    onClickTryMe: () -> Unit
) {
    Column {
        TroubleShootingTip(stringResource(R.string.cdw_troubleshooting_page_b_tip1))
        SpacerMedium()
        TroubleShootingTip(stringResource(R.string.cdw_troubleshooting_page_b_tip2))
        SpacerLarge()
        TroubleShootingTryMeButton {
            onClickTryMe()
        }
    }
}

@LightDarkPreview
@Composable
fun TroubleShootingScaffoldPreview() {
    PreviewAppTheme {
        TroubleShootingScaffold(
            title = stringResource(R.string.cdw_troubleshooting_page_b_title),
            onBack = { },
            bottomBarButton = {
                TroubleShootingNextTipButton(
                    onClick = { }
                )
            }
        ) {
            TroubleShootingDeviceOnTopScreenContent(
                onClickTryMe = {}
            )
        }
    }
}
