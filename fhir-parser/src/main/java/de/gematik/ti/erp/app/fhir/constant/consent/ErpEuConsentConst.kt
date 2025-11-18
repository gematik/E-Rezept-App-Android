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

object ErpEuConsentConst {
    // Top-level
    const val RESOURCE_TYPE = "Consent"
    const val CONSENT_ID = "erp-eprescription-01-POST-Consent"
    const val STATUS_ACTIVE = "active"

    // Meta
    const val PROFILE_URL =
        "https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_Consent|1.0"

    // Patient identifier
    const val PATIENT_SYSTEM_GKV_KVID10 = "http://fhir.de/sid/gkv/kvid-10"
    const val PATIENT_VALUE = "X123456789"

    // Scope
    const val SCOPE_SYSTEM = "http://terminology.hl7.org/CodeSystem/consentscope"
    const val SCOPE_CODE_PATIENT_PRIVACY = "patient-privacy"
    const val SCOPE_DISPLAY_PRIVACY_CONSENT = "Privacy Consent"

    // Category (Consent Type)
    const val CATEGORY_SYSTEM =
        "https://gematik.de/fhir/erp-eu/CodeSystem/GEM_ERPEU_CS_ConsentType"
    const val CATEGORY_CODE_EUDISPCONS = "EUDISPCONS"
    const val CATEGORY_DISPLAY_EU_REDEEM =
        "Consent for redeeming e-prescriptions in EU countries"

    // Policy rule
    const val POLICY_RULE_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-ActCode"
    const val POLICY_RULE_CODE_OPTIN = "OPTIN"
}
