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

package de.gematik.ti.erp.app.troubleshooting.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.troubleshooting.navigation.TroubleShootingRoutes
import de.gematik.ti.erp.app.troubleshooting.ui.components.TroubleShootingNextButton
import de.gematik.ti.erp.app.troubleshooting.ui.components.TroubleShootingScaffold
import de.gematik.ti.erp.app.troubleshooting.ui.components.TroubleShootingTip
import de.gematik.ti.erp.app.troubleshooting.ui.components.TroubleShootingTryMeButton
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.annotatedLinkString
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource

class TroubleShootingFindNfcPositionScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val uriHandler = LocalUriHandler.current
        TroubleShootingScaffold(
            title = stringResource(R.string.cdw_troubleshooting_page_c_title),
            onBack = { navController.popBackStack() },
            bottomBarButton = {
                TroubleShootingNextButton(
                    onClick = {
                        navController.navigate(TroubleShootingRoutes.TroubleShootingNoSuccessScreen.path())
                    }
                )
            }
        ) {
            TroubleShootingFindNfcPositionScreenContent(
                uriHandler = uriHandler,
                onClickTryMe = {
                    navController.popBackStack(TroubleShootingRoutes.TroubleShootingIntroScreen.route, inclusive = true)
                }
            )
        }
    }
}

@Composable
private fun TroubleShootingFindNfcPositionScreenContent(
    uriHandler: UriHandler,
    onClickTryMe: () -> Unit
) {
    Column {
        val firstTip = annotatedStringResource(
            R.string.cdw_troubleshooting_page_c_tip1,
            annotatedLinkString(
                stringResource(R.string.cdw_troubleshooting_page_c_tip1_samsung_url),
                stringResource(R.string.cdw_troubleshooting_page_c_tip1_samsung)
            )
        )
        val secondTip = annotatedStringResource(
            R.string.cdw_troubleshooting_page_c_tip2,
            annotatedLinkString(
                stringResource(R.string.cdw_troubleshooting_page_c_tip2_google_url),
                stringResource(R.string.cdw_troubleshooting_page_c_tip2_google)
            )
        )
        TroubleShootingTip(firstTip, onClickText = { tag, item ->
            when (tag) {
                "URL" -> uriHandler.openUri(item)
            }
        })
        SpacerMedium()
        TroubleShootingTip(secondTip, onClickText = { tag, item ->
            when (tag) {
                "URL" -> uriHandler.openUri(item)
            }
        })
        SpacerLarge()
        TroubleShootingTryMeButton {
            onClickTryMe()
        }
    }
}
