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

package de.gematik.ti.erp.app.cardwall.ui.screens

import android.nfc.NfcAdapter
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.navOptions
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallScreen
import de.gematik.ti.erp.app.cardwall.presentation.CardWallSharedViewModel
import de.gematik.ti.erp.app.cardwall.presentation.SaveCredentialsController
import de.gematik.ti.erp.app.cardwall.presentation.rememberCardWallController
import de.gematik.ti.erp.app.cardwall.presentation.rememberCardWallNfcPositionState
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallAuthenticationData
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallAuthenticationDialog
import de.gematik.ti.erp.app.cardwall.ui.components.EnableNfcDialog
import de.gematik.ti.erp.app.cardwall.ui.components.ReadCardScreenScaffold
import de.gematik.ti.erp.app.cardwall.ui.components.rememberCardWallAuthenticationDialogState
import de.gematik.ti.erp.app.cardwall.ui.preview.NfcPositionPreview
import de.gematik.ti.erp.app.cardwall.ui.preview.NfcPositionPreviewParameter
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.troubleshooting.navigation.TroubleShootingRoutes
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import kotlinx.coroutines.launch

class CardWallReadCardScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val sharedViewModel: CardWallSharedViewModel
) : CardWallScreen() {
    @Composable
    override fun Content() {
        val cardWallController = rememberCardWallController()
        val dialogState = rememberCardWallAuthenticationDialogState()

        val nfcPositionState = rememberCardWallNfcPositionState()
        val nfcPos = nfcPositionState.state.nfcData.nfcPos

        // Allows the nfcTag in the activity to emit the tag
        val activity = LocalActivity.current as MainActivity
        val readerCallback = remember {
            NfcAdapter.ReaderCallback { tag ->
                activity.lifecycleScope.launch {
                    activity.nfcTag.emit(tag)
                }
            }
        }

        // Nfc reader mode is only enabled in this screen and is disabled when leaving the screen
        DisposableEffect(Unit) {
            activity.enableNfcReaderMode(readerCallback)
            onDispose {
                activity.disableNfcReaderMode()
            }
        }

        val profileId by sharedViewModel.profileId.collectAsStateWithLifecycle()
        val saveCredentials by sharedViewModel.saveCredentials.collectAsStateWithLifecycle()
        val can by sharedViewModel.can.collectAsStateWithLifecycle()
        val pin by sharedViewModel.pin.collectAsStateWithLifecycle()

        val authenticationData by remember(saveCredentials) {
            derivedStateOf {
                (saveCredentials as? SaveCredentialsController.AuthResult.Initialized)?.let {
                    CardWallAuthenticationData.SaveCredentialsWithHealthCard(
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

        val onBack by rememberUpdatedState { navController.popBackStack() }

        BackHandler { onBack() }
        ReadCardScreenScaffold(
            onBack = { onBack() },
            onClickTroubleshooting = {
                navController.navigate(
                    TroubleShootingRoutes.TroubleShootingIntroScreen.path()
                )
            },
            nfcPosition = nfcPos
        )
        if (!cardWallController.isNfcEnabled()) {
            EnableNfcDialog { onBack() }
        } else {
            CardWallAuthenticationDialog(
                dialogState = dialogState,
                cardWallController = cardWallController,
                authenticationData = authenticationData,
                profileId = profileId,
                troubleShootingEnabled = true,
                allowUserCancellation = true,
                onFinal = {
                    navController.popBackStack(CardWallRoutes.subGraphName(), inclusive = true)
                },
                onUnlockEgk = {
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
                        CardWallRoutes.CardWallPinScreen.path(
                            can = can,
                            profileIdentifier = profileId
                        ),
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

@LightDarkPreview
@Composable
fun CardWallReadCardScreenPreview(
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
