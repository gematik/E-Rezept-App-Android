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

package de.gematik.ti.erp.app.prescription.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.LayoutDirection
import de.gematik.ti.erp.app.app.ApplicationInnerPadding
import de.gematik.ti.erp.app.consent.model.ConsentState
import de.gematik.ti.erp.app.mainscreen.model.MultiProfileAppBarWrapper
import de.gematik.ti.erp.app.mainscreen.model.ProfileLifecycleState
import de.gematik.ti.erp.app.prescription.ui.model.ConsentClickAction
import de.gematik.ti.erp.app.prescription.ui.model.MultiProfileTopAppBarClickAction
import de.gematik.ti.erp.app.prescription.ui.model.PrescriptionsScreenContentClickAction
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow

data class PrescriptionScreenPreviewData(
    val name: String,
    val multiProfileAppBarWrapper: MultiProfileAppBarWrapper,
    val profileData: UiState<ProfilesUseCaseData.Profile>,
    val activePrescription: UiState<List<Prescription>>,
    val isArchivedEmpty: Boolean,
    val isOnlyDigas: Boolean,
    val hasRedeemableTasks: Boolean,
    val consentState: ConsentState = ConsentState.ValidState.NotGranted,
    val isTopBarElevated: Boolean = false,
    val fabPadding: ApplicationInnerPadding = ApplicationInnerPadding(layoutDirection = LayoutDirection.Ltr),
    val prescriptionsClickAction: PrescriptionsScreenContentClickAction = PrescriptionsScreenContentClickAction(
        onClickLogin = {},
        onClickAvatar = {},
        onClickArchive = {},
        onClickPrescription = { _, _, _ -> },
        onChooseAuthenticationMethod = {},
        onClickRedeem = {},
        onClickRefresh = {}
    ),
    val consentClickAction: ConsentClickAction = ConsentClickAction(
        onGetChargeConsent = {}
    ),
    val topAppBarClickAction: MultiProfileTopAppBarClickAction = MultiProfileTopAppBarClickAction(
        onClickAddProfile = {},
        onClickChangeProfileName = {},
        onClickAddScannedPrescription = {},
        onSwitchActiveProfile = {},
        onElevateTopAppBar = {}
    )
)

class PrescriptionScreenPreviewParameterProvider : PreviewParameterProvider<PrescriptionScreenPreviewData> {
    override val values: Sequence<PrescriptionScreenPreviewData>
        get() = sequenceOf(
            PrescriptionScreenPreviewData(
                name = "some-prescriptions-user-error",
                multiProfileAppBarWrapper = MultiProfileAppBarWrapper(
                    profileLifecycleState = ProfileLifecycleState(
                        isProfileRefreshing = MutableStateFlow(false),
                        networkStatus = MutableStateFlow(false),
                        isTokenValid = MutableStateFlow(true),
                        isRegistered = MutableStateFlow(true)
                    ),
                    activeProfile = MutableStateFlow(MOCK_MODEL_PROFILE),
                    existingProfiles = MutableStateFlow(listOf(MOCK_MODEL_PROFILE, MOCK_MODEL_PROFILE_2))
                ),
                profileData = UiState.Data(MOCK_MODEL_PROFILE),
                activePrescription = UiState.Data(
                    listOf(
                        MOCK_PRESCRIPTION_SELF_PAYER,
                        MOCK_PRESCRIPTION_DIRECT_ASSIGNMENT
                    )
                ),
                isArchivedEmpty = true,
                hasRedeemableTasks = false,
                isOnlyDigas = false
            ),
            PrescriptionScreenPreviewData(
                name = "empty-prescriptions-user-logged-out",
                multiProfileAppBarWrapper = MultiProfileAppBarWrapper(
                    profileLifecycleState = ProfileLifecycleState(
                        isProfileRefreshing = MutableStateFlow(false),
                        networkStatus = MutableStateFlow(true),
                        isTokenValid = MutableStateFlow(false),
                        isRegistered = MutableStateFlow(true)
                    ),
                    activeProfile = MutableStateFlow(MOCK_MODEL_PROFILE),
                    existingProfiles = MutableStateFlow(listOf(MOCK_MODEL_PROFILE, MOCK_MODEL_PROFILE_2))
                ),
                profileData = UiState.Data(MOCK_MODEL_PROFILE),
                activePrescription = UiState.Empty(),
                isArchivedEmpty = true,
                hasRedeemableTasks = false,
                isOnlyDigas = false
            ),
            PrescriptionScreenPreviewData(
                name = "with-prescriptions-user-logged-in",
                multiProfileAppBarWrapper = MultiProfileAppBarWrapper(
                    existingProfiles = MutableStateFlow(listOf(MOCK_MODEL_PROFILE_LOGGED_IN)),
                    activeProfile = MutableStateFlow(MOCK_MODEL_PROFILE_LOGGED_IN),
                    profileLifecycleState = ProfileLifecycleState(
                        isProfileRefreshing = MutableStateFlow(false),
                        networkStatus = MutableStateFlow(true),
                        isRegistered = MutableStateFlow(true),
                        isTokenValid = MutableStateFlow(true)
                    )
                ),
                profileData = UiState.Data(MOCK_MODEL_PROFILE_LOGGED_IN),
                activePrescription = UiState.Data(
                    listOf(
                        MOCK_PRESCRIPTION_SELF_PAYER,
                        MOCK_PRESCRIPTION_DIRECT_ASSIGNMENT,
                        MOCK_PRESCRIPTION_EXPIRED,
                        MOCK_PRESCRIPTION_DELETED
                    )
                ),
                isArchivedEmpty = false,
                hasRedeemableTasks = true,
                isTopBarElevated = true,
                isOnlyDigas = false
            ),
            /* Todo: Date conflicts causing below test data to fail. After refactoring the component, this test data should be enabled.
            PrescriptionScreenPreviewData(
                name = "with-ready-prescriptions-user-logged-in",
                multiProfileAppBarFlowWrapper = MultiProfileAppBarFlowWrapper(
                    activeProfile = MutableStateFlow(MOCK_MODEL_PROFILE_LOGGED_IN),
                    existingProfiles = MutableStateFlow(listOf(MOCK_MODEL_PROFILE_LOGGED_IN)),
                    isProfileRefreshing = MutableStateFlow(false)
                ),
                profileData = UiState.Data(MOCK_MODEL_PROFILE_LOGGED_IN),
                activePrescription = UiState.Data(
                    listOf(MOCK_PRESCRIPTION_READY)
                ),
                isArchivedEmpty = false,
                hasRedeemableTasks = true
            ),
            ),
             */
            PrescriptionScreenPreviewData(
                name = "with-prescriptions-user-invalid",
                multiProfileAppBarWrapper = MultiProfileAppBarWrapper(
                    existingProfiles = MutableStateFlow(listOf(MOCK_MODEL_PROFILE_LOGGED_INVALID, MOCK_MODEL_PROFILE)),
                    activeProfile = MutableStateFlow(MOCK_MODEL_PROFILE_LOGGED_INVALID),
                    profileLifecycleState = ProfileLifecycleState(
                        isProfileRefreshing = MutableStateFlow(false),
                        networkStatus = MutableStateFlow(true),
                        isRegistered = MutableStateFlow(true),
                        isTokenValid = MutableStateFlow(false)
                    )
                ),
                profileData = UiState.Data(MOCK_MODEL_PROFILE_LOGGED_INVALID),
                activePrescription = UiState.Data(
                    listOf(
                        MOCK_PRESCRIPTION_PENDING,
                        MOCK_PRESCRIPTION_IN_PROGRESS,
                        MOCK_PRESCRIPTION_LATER_REDEEMABLE,
                        MOCK_PRESCRIPTION_OTHER
                    )
                ),
                isArchivedEmpty = true,
                hasRedeemableTasks = false,
                isOnlyDigas = false
            )
        )
}
