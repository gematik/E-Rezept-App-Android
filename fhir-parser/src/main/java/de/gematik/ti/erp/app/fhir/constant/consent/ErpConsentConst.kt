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

package de.gematik.ti.erp.app.fhir.constant.consent

import de.gematik.ti.erp.app.fhir.constant.FhirIdentifierSystems

/**
 * **Consent Constants Base**
 *
 * Common constants shared between ERP Charge Consent (PKV) and EU Consent.
 * Using a sealed class to provide type-safe access to consent-type-specific values.
 */
sealed class ConsentConstants(
    val consentId: String,
    val profileUrl: String,
    val patientSystem: String,
    val categorySystem: String,
    val categoryCode: String,
    val categoryDisplay: String
) {
    /**
     * Common constants shared across all consent types
     */
    companion object {
        const val STATUS_ACTIVE = "active"

        // Scope values (identical for all consent types)
        const val SCOPE_SYSTEM = "http://terminology.hl7.org/CodeSystem/consentscope"
        const val SCOPE_CODE_PATIENT_PRIVACY = "patient-privacy"
        const val SCOPE_DISPLAY_PRIVACY_CONSENT = "Privacy Consent"

        // Policy rule values (identical for all consent types)
        const val POLICY_RULE_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-ActCode"
        const val POLICY_RULE_CODE_OPTIN = "OPTIN"
    }

    /**
     * ERP Charge Consent (PKV) versions
     */
    sealed class ErpCharge(
        consentId: String,
        patientSystem: String,
        profileUrl: String
    ) : ConsentConstants(
        consentId = consentId,
        profileUrl = profileUrl,
        patientSystem = patientSystem,
        categorySystem = "https://gematik.de/fhir/erpchrg/CodeSystem/GEM_ERPCHRG_CS_ConsentType",
        categoryCode = "CHARGCONS",
        categoryDisplay = "Consent for charging"
    ) {
        /** ERP Charge Consent - profile version 1.0 */
        object V1_0 : ErpCharge(
            consentId = "erp-charge-01-POST-Consent",
            patientSystem = FhirIdentifierSystems.Patient.KVNR_PKV,
            profileUrl = "https://gematik.de/fhir/erpchrg/StructureDefinition/GEM_ERPCHRG_PR_Consent|1.0"
        )

        /** ERP Charge Consent - profile version 1.1 */
        object V1_1 : ErpCharge(
            consentId = "erp-charge-01-POST-Consent",
            patientSystem = FhirIdentifierSystems.Patient.KVNR_GKV,
            profileUrl = "https://gematik.de/fhir/erpchrg/StructureDefinition/GEM_ERPCHRG_PR_Consent|1.1"
        )

        companion object {
            /** Default version to use for ERP Charge Consent */
            val DEFAULT: ErpCharge = V1_0
        }
    }

    /**
     * EU E-Prescription Consent - profile version 1.1
     */
    class ErpEu : ConsentConstants(
        consentId = "erp-eprescription-01-POST-Consent",
        profileUrl = "https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_Consent|1.1",
        patientSystem = FhirIdentifierSystems.Patient.KVNR_GKV,
        categorySystem = "https://gematik.de/fhir/erp-eu/CodeSystem/GEM_ERPEU_CS_ConsentType",
        categoryCode = "EUDISPCONS",
        categoryDisplay = "Consent for redeeming e-prescriptions in EU countries"
    )
}
