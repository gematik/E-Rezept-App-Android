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

package de.gematik.ti.erp.app.eurezept.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.eurezept.ui.model.EuConsentViewState
import de.gematik.ti.erp.app.fhir.FhirConsentErpModelCollection
import de.gematik.ti.erp.app.fhir.consent.model.FhirCodeableConceptErp
import de.gematik.ti.erp.app.fhir.consent.model.FhirCodingErp
import de.gematik.ti.erp.app.fhir.consent.model.FhirConsentErpModel
import de.gematik.ti.erp.app.utils.uistate.UiState

data class EuConsentPreviewParameter(
    val name: String,
    val uiState: UiState<EuConsentViewState>
)

class EuConsentPreviewParameterProvider : PreviewParameterProvider<EuConsentPreviewParameter> {
    override val values: Sequence<EuConsentPreviewParameter>
        get() = sequenceOf(
            EuConsentPreviewParameter(
                name = "Loading",
                uiState = UiState.Loading()
            ),
            EuConsentPreviewParameter(
                name = "Error",
                uiState = UiState.Error(Exception())
            ),
            EuConsentPreviewParameter(
                name = "Eu ConsentData",
                uiState = UiState.Data(
                    EuConsentViewState(
                        consentData = mockConsent,
                        isGrantingConsent = false,
                        grantConsentError = null
                    )
                )
            )
        )
}

private val mockConsentModel = FhirConsentErpModel(
    resourceType = "Consent",
    id = "consent-789",
    status = "inactive",
    category = listOf(
        FhirCodeableConceptErp(
            coding = listOf(
                FhirCodingErp(
                    system = "http://terminology.hl7.org/CodeSystem/consentscope",
                    code = "patient-privacy",
                    display = "Privacy Consent"
                )
            )
        )
    ),
    policyRule = FhirCodeableConceptErp(
        coding = listOf(
            FhirCodingErp(
                system = "http://terminology.hl7.org/CodeSystem/v3-ActCode",
                code = "OPTIN",
                display = "Opt In"
            )
        )
    ),
    dateTime = "2024-01-15T10:30:00Z",
    scope = FhirCodeableConceptErp(
        coding = listOf(
            FhirCodingErp(
                system = "http://terminology.hl7.org/CodeSystem/consentscope",
                code = "patient-privacy",
                display = "Privacy Consent"
            )
        )
    )
)

private val mockConsent = FhirConsentErpModelCollection(
    consent = listOf(mockConsentModel)
)
