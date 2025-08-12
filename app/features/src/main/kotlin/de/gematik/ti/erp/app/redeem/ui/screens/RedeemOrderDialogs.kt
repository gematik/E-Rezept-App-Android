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

package de.gematik.ti.erp.app.redeem.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import de.gematik.ti.erp.app.authentication.ui.components.AuthenticationFailureDialog
import de.gematik.ti.erp.app.mainscreen.presentation.AppController
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.redeem.presentation.OnlineRedeemSharedViewModel
import de.gematik.ti.erp.app.redeem.presentation.RedeemOrderOverviewScreenController
import de.gematik.ti.erp.app.redeem.ui.components.ErrorOnRedeemablePrescriptionDialog
import de.gematik.ti.erp.app.redeem.ui.components.PrescriptionRedeemAlertDialog
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.extract
import kotlinx.coroutines.launch

/**
 * Displays all dialogs related to the redemption flow on the `RedeemOrderOverviewScreen`.
 *
 * This composable is responsible for managing and rendering:
 * - [AuthenticationFailureDialog]: Shown when user authentication fails.
 * - [PrescriptionRedeemAlertDialog]: Shown after a successful redemption to confirm the flow.
 * - [ErrorOnRedeemablePrescriptionDialog]: Shown when an order contains prescriptions that are
 *   either invalid or incomplete and require user intervention.
 *
 * It consolidates dialog logic to simplify the main screen and ensures proper interaction with
 * the controllers and shared view model state.
 *
 * @param dialog The [DialogScaffold] used to display dialogs within the screen.
 * @param orderOverviewController Controller for managing dialog-related events in the redeem screen.
 * @param appController Application-wide controller for finalizing the redemption operation.
 * @param sharedViewModel The shared view model between redeem and pharmacy graphs.
 * @param pharmacy The currently selected pharmacy, may be null.
 * @param orderHasError Indicates whether the current redemption order has any validation issues.
 * @param onDismiss Called after the final dialog interaction is handled and the state must be cleaned up.
 * @param redeemOrder Function to trigger redemption logic after incomplete order correction. Called with the active [Profile].
 */
@Composable
internal fun RedeemOrderDialogs(
    dialog: DialogScaffold,
    orderOverviewController: RedeemOrderOverviewScreenController,
    appController: AppController,
    sharedViewModel: OnlineRedeemSharedViewModel,
    pharmacy: PharmacyUseCaseData.Pharmacy?,
    orderHasError: Boolean,
    onDismiss: () -> Unit,
    redeemOrder: (ProfilesUseCaseData.Profile) -> Unit
) {
    val scope = rememberCoroutineScope()

    AuthenticationFailureDialog(
        event = orderOverviewController.showAuthenticationErrorDialog,
        dialogScaffold = dialog
    )

    PrescriptionRedeemAlertDialog(
        event = orderOverviewController.showPrescriptionRedeemAlertDialogEvent,
        dialog = dialog,
        onDismiss = {
            orderOverviewController.disableLoadingIndicator()
            appController.onOrdered(hasError = orderHasError)
            sharedViewModel.onResetPrescriptionSelection()
            onDismiss()
        }
    )

    ErrorOnRedeemablePrescriptionDialog(
        event = orderOverviewController.showErrorOnRedeemAlertDialogEvent,
        dialog = dialog,
        onClickForInvalidOrder = {
            sharedViewModel.onResetPrescriptionSelection()
            onDismiss()
        },
        onClickForIncompleteOrder = { nonRedeemableTaskIds ->
            sharedViewModel.deselectInvalidPrescriptions(nonRedeemableTaskIds)
            scope.launch {
                sharedViewModel.activeProfile.extract()?.let { profile ->
                    redeemOrder(profile)
                }
            }
        },
        onClickToCancel = {
            // no-op for now
        }
    )
}
