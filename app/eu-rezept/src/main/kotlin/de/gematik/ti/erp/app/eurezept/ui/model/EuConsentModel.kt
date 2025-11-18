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

package de.gematik.ti.erp.app.eurezept.ui.model

import de.gematik.ti.erp.app.fhir.FhirConsentErpModelCollection

data class EuConsentViewState(
    val consentData: FhirConsentErpModelCollection,
    val isGrantingConsent: Boolean = false,
    val grantConsentError: Throwable? = null,
    val isRevokingConsent: Boolean = false,
    val revokeConsentError: Throwable? = null
)

data class EuConsentScreenActions(
    val onBack: () -> Unit,
    val onCancel: () -> Unit,
    val onAcceptEuConsent: () -> Unit,
    val onDeclineEuConsent: () -> Unit,
    val onRevokeEuConsent: () -> Unit = {},
    val onRetry: () -> Unit
)

sealed class EuConsentNavigationEvent {
    object NavigateToRedeem : EuConsentNavigationEvent()
    object NavigateBack : EuConsentNavigationEvent()
    object CancelFlow : EuConsentNavigationEvent()
}
