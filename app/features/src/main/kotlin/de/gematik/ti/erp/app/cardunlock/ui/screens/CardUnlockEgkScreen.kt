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

package de.gematik.ti.erp.app.cardunlock.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.navOptions
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockRoutes
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockScreen
import de.gematik.ti.erp.app.cardunlock.presentation.CardUnlockGraphController
import de.gematik.ti.erp.app.cardunlock.ui.components.UnlockEgkDialog
import de.gematik.ti.erp.app.cardunlock.ui.components.rememberUnlockEgkDialogState
import de.gematik.ti.erp.app.cardwall.presentation.rememberCardWallNfcPositionState
import de.gematik.ti.erp.app.cardwall.ui.components.ReadCardScreenScaffold
import de.gematik.ti.erp.app.cardwall.ui.preview.NfcPositionPreview
import de.gematik.ti.erp.app.cardwall.ui.preview.NfcPositionPreviewParameter
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.troubleshooting.navigation.TroubleShootingRoutes
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import org.kodein.di.compose.rememberInstance

class CardUnlockEgkScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: CardUnlockGraphController
) : CardUnlockScreen() {
    @Composable
    override fun Content() {
        val unlockMethod by graphController.unlockMethod.collectAsStateWithLifecycle()

        val dialogState = rememberUnlockEgkDialogState()
        val buildConfig by rememberInstance<BuildConfigInformation>()

        val can by graphController.can.collectAsStateWithLifecycle()
        val puk by graphController.puk.collectAsStateWithLifecycle()
        val oldPin by graphController.oldPin.collectAsStateWithLifecycle()
        val newPin by graphController.newPin.collectAsStateWithLifecycle()

        val nfcPositionState = rememberCardWallNfcPositionState()
        val nfcPos = nfcPositionState.state.nfcData.nfcPos

        UnlockEgkDialog(
            buildConfig = buildConfig,
            unlockMethod = unlockMethod.name,
            dialogState = dialogState,
            graphController = graphController,
            cardAccessNumber = can,
            personalUnblockingKey = puk,
            troubleShootingEnabled = true,
            onClickTroubleshooting = {
                navController.navigate(TroubleShootingRoutes.TroubleShootingIntroScreen.path())
            },
            oldPin = oldPin,
            newPin = newPin,
            onRetryCan = {
                navController.navigate(
                    route = CardUnlockRoutes.CardUnlockCanScreen.route,
                    navOptions = navOptions {
                        popUpTo(CardUnlockRoutes.CardUnlockCanScreen.route) {
                            inclusive = true
                        }
                    }
                )
            },
            onRetryOldSecret = {
                navController.navigate(
                    route = CardUnlockRoutes.CardUnlockOldSecretScreen.route,
                    navOptions = navOptions {
                        popUpTo(CardUnlockRoutes.CardUnlockOldSecretScreen.route) {
                            inclusive = true
                        }
                    }
                )
            },
            onRetryPuk = {
                navController.navigate(
                    route = CardUnlockRoutes.CardUnlockPukScreen.route,
                    navOptions = navOptions {
                        popUpTo(CardUnlockRoutes.CardUnlockPukScreen.route) {
                            inclusive = true
                        }
                    }
                )
            },
            onFinishUnlock = {
                graphController.reset()
                navController.popBackStack(CardUnlockRoutes.CardUnlockIntroScreen.route, inclusive = true)
            },
            onAssignPin = {
                graphController.setUnlockMethodForGraph(UnlockMethod.ChangeReferenceData)
                navController.popBackStack(CardUnlockRoutes.CardUnlockIntroScreen.route, inclusive = true)
            }
        )
        ReadCardScreenScaffold(
            onBack = { navController.popBackStack() },
            onClickTroubleshooting = {
                navController.navigate(TroubleShootingRoutes.TroubleShootingIntroScreen.path())
            },
            nfcPosition = nfcPos
        )
    }
}

@LightDarkPreview
@Composable
fun CardUnlockEgkScreenPreview(
    @PreviewParameter(NfcPositionPreviewParameter::class) previewData: NfcPositionPreview
) {
    PreviewAppTheme {
        ReadCardScreenScaffold(
            onBack = {},
            onClickTroubleshooting = {},
            nfcPosition = previewData.nfcPos
        )
    }
}
