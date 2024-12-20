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

package de.gematik.ti.erp.app.redeem.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.model.PrescriptionRedeemArguments.Companion.from
import de.gematik.ti.erp.app.pharmacy.model.orderID
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.redeem.model.RedeemDialogParameters
import de.gematik.ti.erp.app.redeem.model.RedeemPrescriptionDialogMessageState
import de.gematik.ti.erp.app.redeem.model.RedeemPrescriptionDialogMessageState.Companion.toDialogMessageState
import de.gematik.ti.erp.app.redeem.model.RedeemedPrescriptionState
import de.gematik.ti.erp.app.redeem.presentation.rememberRedeemPrescriptionsController
import de.gematik.ti.erp.app.redeem.ui.screens.PrescriptionRedeemAlertDialog
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerXLarge
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonLarge
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.letNotNull

// A composable button that can handle the redemption process and gives the user feedback about the process
@Suppress("CyclomaticComplexMethod")
@Composable
fun RedeemButton(
    profile: ProfilesUseCaseData.Profile,
    order: PharmacyUseCaseData.OrderState,
    selectedPharmacy: PharmacyUseCaseData.Pharmacy,
    selectedOrderOption: PharmacyScreenData.OrderOption,
    shippingContactCompleted: Boolean,
    isRedemptionPossible: Boolean,
    onNotRedeemable: () -> Unit,
    onFinish: (Boolean) -> Unit,
    onProcessStarted: () -> Unit,
    onProcessEnded: () -> Unit
) {
    val dialog = LocalDialog.current

    val showDialogEvent: ComposableEvent<RedeemDialogParameters> = remember { ComposableEvent() }

    val redeemController = rememberRedeemPrescriptionsController()

    val redeemedState by redeemController.redeemedState.collectAsStateWithLifecycle()

    val processStartedEvent = redeemController.onProcessStartEvent
    val processEndEvent = redeemController.onProcessEndEvent

    var uploadInProgress by remember { mutableStateOf(false) }
    var orderHasError by remember { mutableStateOf(false) }

    LaunchedEffect(redeemedState) {
        redeemedState.isOrderCompletedState { state ->
            try {
                orderHasError = state.results.values.containsError()
                val dialogMessageState = obtainDialogParameters(state.results.values)
                showDialogEvent.trigger(dialogMessageState.redeemDialogParameters)
            } catch (e: Throwable) {
                showDialogEvent.trigger(RedeemPrescriptionDialogMessageState.Unknown().redeemDialogParameters)
            }
        }
    }

    processStartedEvent.listen {
        uploadInProgress = true
        onProcessStarted()
    }

    processEndEvent.listen {
        uploadInProgress = false
        onProcessEnded()
    }

    showDialogEvent.listen { dialogParams ->
        dialog.show {
            PrescriptionRedeemAlertDialog(
                title = stringResource(dialogParams.title),
                description = stringResource(dialogParams.description),
                onDismiss = {
                    it.dismiss()
                    onFinish(orderHasError)
                }
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        elevation = SizeDefaults.half
    ) {
        Column(Modifier.navigationBarsPadding()) {
            SpacerMedium()
            PrimaryButtonLarge(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag(TestTag.PharmacySearch.OrderSummary.SendOrderButton),
                enabled = shippingContactCompleted && !uploadInProgress,
                onClick = {
                    if (isRedemptionPossible) {
                        letNotNull(
                            first = selectedOrderOption,
                            second = selectedPharmacy
                        ) { orderOption, pharmacy ->
                            redeemController.processPrescriptionRedemptions(
                                arguments = orderID().from(
                                    profile = profile,
                                    order = order,
                                    redeemOption = orderOption,
                                    pharmacy = pharmacy
                                )
                            )
                        }
                    } else {
                        onNotRedeemable()
                    }
                }
            ) {
                Text(stringResource(R.string.pharmacy_order_send))
            }
            SpacerXLarge()
        }
    }
}

private fun obtainDialogParameters(
    results: Collection<RedeemedPrescriptionState?>
): RedeemPrescriptionDialogMessageState =
    when {
        // case 1: When one prescription is transferred.
        results.size == 1 -> results.firstNotNullOf { it?.toDialogMessageState() }

        // case 2.1: When multiple prescriptions are transferred Successfully.
        results.containsNoError() -> RedeemPrescriptionDialogMessageState.Success()

        // case 2.2: When any multiple prescription are transferred Unsuccessfully. Show a generic error message.
        else -> RedeemPrescriptionDialogMessageState.MultiplePrescriptionsFailed()
    }

private fun Collection<RedeemedPrescriptionState?>.containsError(): Boolean =
    any { it is RedeemedPrescriptionState.Error }

private fun Collection<RedeemedPrescriptionState?>.containsNoError(): Boolean =
    any { it !is RedeemedPrescriptionState.Error }
