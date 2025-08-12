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

package de.gematik.ti.erp.app.prescription.ui.components

import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.consent.model.ConsentState
import de.gematik.ti.erp.app.pkv.presentation.ConsentValidator
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.pulltorefresh.extensions.trigger
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.uistate.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileLoadingSection(
    profileData: UiState<ProfilesUseCaseData.Profile>,
    pullToRefreshState: PullToRefreshState,
    consentState: ConsentState,
    onGetConsent: (id: ProfileIdentifier) -> Unit,
    onChooseAuthenticationMethod: (profile: ProfilesUseCaseData.Profile) -> Unit,
    onRefresh: () -> Unit
) {
    UiStateMachine(
        state = profileData,
        onError = {
            ErrorScreenComponent(
                onClickRetry = onRefresh
            )
        },
        onEmpty = {
            Center {
                CircularProgressIndicator()
            }
        },
        onLoading = {
            Center {
                CircularProgressIndicator()
            }
        },
        onContent = { activeProfile ->
            val ssoTokenValid = activeProfile.isSSOTokenValid()
            LaunchedEffect(activeProfile) {
                if (activeProfile.isPkv()) {
                    ConsentValidator.validateAndExecute(
                        isSsoTokenValid = ssoTokenValid,
                        consentState = consentState,
                        getChargeConsent = {
                            onRefresh()
                            onGetConsent(activeProfile.id)
                        },
                        onConsentGranted = onRefresh
                    )
                } else {
                    onRefresh()
                }
            }

            @Requirement(
                "A_24857#1",
                sourceSpecification = "gemSpec_eRp_FdV",
                rationale = "Refreshing the prescription list happens only if the user is authenticated. " +
                    "If the user is not authenticated, the user is prompted to authenticate."
            )
            with(pullToRefreshState) {
                trigger(
                    block = {
                        if (activeProfile.isPkv()) {
                            ConsentValidator.validateAndExecute(
                                isSsoTokenValid = ssoTokenValid,
                                consentState = consentState,
                                getChargeConsent = {
                                    onRefresh()
                                    onGetConsent(activeProfile.id)
                                },
                                onConsentGranted = onRefresh
                            )
                        } else {
                            onRefresh()
                        }
                    },
                    onNavigation = {
                        if (!ssoTokenValid) {
                            endRefresh()
                            onChooseAuthenticationMethod(activeProfile)
                        }
                    }
                )
            }
        }
    )
}
