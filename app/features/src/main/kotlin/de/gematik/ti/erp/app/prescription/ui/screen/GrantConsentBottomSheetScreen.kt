/*
 * Copyright 2025, gematik GmbH
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

@file:Suppress("UsingMaterialAndMaterial3Libraries")

package de.gematik.ti.erp.app.prescription.ui.screen

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.base.presentation.rememberGetActiveProfileController
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.navigation.BottomSheetScreen
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes
import de.gematik.ti.erp.app.pkv.presentation.rememberConsentController
import de.gematik.ti.erp.app.prescription.ui.components.CommonDrawerScreenContent
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.FullScreenLoadingIndicator
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbar

/**
 * This drawer is hidden from the user if the user clicks on one of the buttons when it is shown
 */
class GrantConsentBottomSheetScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : BottomSheetScreen(forceToMaxHeight = true) {
    @Composable
    override fun Content() {
        val controller = rememberGetActiveProfileController()
        val consentController = rememberConsentController()
        val profileData by controller.activeProfile.collectAsStateWithLifecycle()

        val snackbar = LocalSnackbar.current
        val consentGrantedInfo = stringResource(R.string.consent_granted_info)

        UiStateMachine(
            state = profileData,
            onError = {
                ErrorScreenComponent()
            },
            onLoading = {
                FullScreenLoadingIndicator()
            },
            onContent = { profile ->
                GrantConsentScreenContent(
                    onClickGrantConsent = {
                        consentController.saveConsentDrawerShown(profile.id)
                        consentController.grantChargeConsent(profile.id)
                        snackbar.show(
                            text = consentGrantedInfo,
                            actionTextId = R.string.consent_action_to_invoices,
                            onClickAction = {
                                profileData.data?.id?.let {
                                    navController.navigate(PkvRoutes.InvoiceListScreen.path(it))
                                }
                            }
                        )
                        navController.popBackStack()
                    },
                    onCancel = {
                        consentController.saveConsentDrawerShown(profile.id)
                        navController.popBackStack()
                    }
                )
            }
        )
    }
}

@Composable
private fun GrantConsentScreenContent(
    onClickGrantConsent: () -> Unit,
    onCancel: () -> Unit
) {
    CommonDrawerScreenContent(
        modifierPrimary = Modifier
            .testTag(TestTag.Main.MainScreenBottomSheet.GetConsentButton)
            .wrapContentSize(),
        header = stringResource(R.string.give_consent_bottom_sheet_header),
        info = stringResource(R.string.give_consent_bottom_sheet_info),
        image = painterResource(R.drawable.pharmacist_circle_blue),
        connectButtonText = stringResource(R.string.give_consent_bottom_sheet_activate),
        cancelButtonText = stringResource(R.string.give_consent_bottom_sheet_activate_later),
        onClickConnect = onClickGrantConsent,
        onCancel = onCancel
    )
}

@LightDarkPreview
@Composable
fun GrantConsentBottomSheetScreenPreview() {
    PreviewAppTheme {
        GrantConsentScreenContent(
            onClickGrantConsent = {},
            onCancel = {}
        )
    }
}
