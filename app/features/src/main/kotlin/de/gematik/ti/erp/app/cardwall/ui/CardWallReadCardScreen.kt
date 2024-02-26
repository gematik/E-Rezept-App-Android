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

package de.gematik.ti.erp.app.cardwall.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.navOptions
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallScreen
import de.gematik.ti.erp.app.cardwall.presentation.AltPairingProvider
import de.gematik.ti.erp.app.cardwall.presentation.CardWallGraphController
import de.gematik.ti.erp.app.cardwall.presentation.rememberCardWallController
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallAuthenticationData
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallAuthenticationDialog
import de.gematik.ti.erp.app.cardwall.ui.components.EnableNfcDialog
import de.gematik.ti.erp.app.cardwall.ui.components.ReadCardScreenComposable
import de.gematik.ti.erp.app.cardwall.ui.components.rememberCardWallAuthenticationDialogState
import de.gematik.ti.erp.app.troubleshooting.navigation.TroubleShootingRoutes

class CardWallReadCardScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: CardWallGraphController
) : CardWallScreen() {
    @Composable
    override fun Content() {
        val cardWallController = rememberCardWallController()
        val dialogState = rememberCardWallAuthenticationDialogState()

        val profileId by graphController.profileId.collectAsStateWithLifecycle()
        val altPairing by graphController.altPairing.collectAsStateWithLifecycle()
        val can by graphController.can.collectAsStateWithLifecycle()
        val pin by graphController.pin.collectAsStateWithLifecycle()

        val authenticationData by remember(altPairing) {
            derivedStateOf {
                (altPairing as? AltPairingProvider.AuthResult.Initialized)?.let {
                    CardWallAuthenticationData.AltPairingWithHealthCard(
                        cardAccessNumber = can,
                        personalIdentificationNumber = pin,
                        initialPairingData = it
                    )
                } ?: CardWallAuthenticationData.HealthCard(
                    cardAccessNumber = can,
                    personalIdentificationNumber = pin
                )
            }
        }

        ReadCardScreenComposable(
            onBack = {
                navController.popBackStack()
            },
            onClickTroubleshooting = {
                navController.navigate(
                    TroubleShootingRoutes.TroubleShootingIntroScreen.path()
                )
            }
        )
        if (!cardWallController.checkNfcEnabled()) {
            EnableNfcDialog {
                navController.popBackStack()
            }
        } else {
            CardWallAuthenticationDialog(
                dialogState = dialogState,
                cardWallController = cardWallController,
                authenticationData = authenticationData,
                profileId = profileId,
                troubleShootingEnabled = true,
                allowUserCancellation = true,
                onFinal = {
                    graphController.reset()
                    navController.popBackStack(CardWallRoutes.CardWallIntroScreen.route, inclusive = true)
                },
                onUnlockEgk = {
                    graphController.reset()
                    navController.navigate(
                        CardUnlockRoutes.CardUnlockIntroScreen.path(unlockMethod = UnlockMethod.ResetRetryCounter.name),
                        navOptions = navOptions {
                            popUpTo(CardWallRoutes.CardWallIntroScreen.route) {
                                inclusive = true
                            }
                        }
                    )
                },
                onRetryCan = {
                    navController.navigate(
                        CardWallRoutes.CardWallCanScreen.path(),
                        navOptions = navOptions {
                            popUpTo(CardWallRoutes.CardWallCanScreen.route) {
                                inclusive = true
                            }
                        }
                    )
                },
                onRetryPin = {
                    navController.navigate(
                        CardWallRoutes.CardWallPinScreen.path(),
                        navOptions = navOptions {
                            popUpTo(CardWallRoutes.CardWallPinScreen.route) {
                                inclusive = true
                            }
                        }
                    )
                },
                onClickTroubleshooting = {
                    navController.navigate(
                        TroubleShootingRoutes.TroubleShootingIntroScreen.path()
                    )
                }
            )
        }
    }
}
