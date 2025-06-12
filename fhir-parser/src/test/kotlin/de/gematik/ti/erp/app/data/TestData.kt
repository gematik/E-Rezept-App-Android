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

@file:Suppress("ktlint:max-line-length")

package de.gematik.ti.erp.app.data

internal fun getResourceAsString(resourcePath: String): String {
    return object {}.javaClass.getResourceAsStream(resourcePath)
        ?.bufferedReader()?.use { it.readText() }
        ?: error("Test resource not found: $resourcePath")
}

// task metadata + kbv bundle
val taskMetadataBundleKbvBundle by lazy { getResourceAsString("/fhir/bundle_seperation_parser/task_metadata_bundle_kbv_bundle.json") }
val taskMetadataBundleKbvWithDigaBundle by lazy { getResourceAsString("/fhir/bundle_seperation_parser/task_metadata_bundle_kbv_bundle_with_diga.json") }

// task-initial-bundle
val task_bundle_vers_1_2 by lazy { getResourceAsString("/fhir/entry_parser/task_bundle_vers_1_2.json") }
val task_bundle_vers_1_3 by lazy { getResourceAsString("/fhir/entry_parser/task_bundle_vers_1_3.json") }

// task metadata
val taskJson_vers_1_2 by lazy { getResourceAsString("/fhir/metadata_parser/task_vers_1_2.json") }
val taskJson_vers_1_3 by lazy { getResourceAsString("/fhir/metadata_parser/task_vers_1_3.json") }

// insurance information
val insuranceInformationJson by lazy { getResourceAsString("/fhir/kbv_parser/coverage/insurance_information.json") }
val insuranceInformation110Json by lazy { getResourceAsString("/fhir/kbv_parser/coverage/coverage_vers_1_1_0.json") }

// medication request
val medicationRequestJson_vers_1_0_2 by lazy { getResourceAsString("/fhir/kbv_parser/medication_request/medication_request.json") }
val medicationRequestJson_vers_1_1_0 by lazy { getResourceAsString("/fhir/kbv_parser/medication_request/medication_request_vers_1_1_0.json") }
val medicationRequestJson_vers_1_1_0_with_accident by lazy {
    getResourceAsString(
        "/fhir/kbv_parser/medication_request/medication_request_1_1_0_with_accident.json"
    )
}

// medication
val medicationPznJson_vers_1_0_2 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_pzn.json") }
val medicationPznJson_vers_1_1_0 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_pzn_vers_1_1_0.json") }
val medicationIngredientJson_vers_1_0_2 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_ingredient.json") }
val medicationIngredientJson_vers_1_1_0 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_ingredient_vers_1_1_0.json") }
val medicationCompoundingJson_vers_1_0_2 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_compounding.json") }
val medicationCompoundingJson_vers_1_1_0 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_compounding_vers_1_1_0.json") }
val medicationFreetextJson_vers_1_0_2 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_freetext.json") }
val medicationFreetextJson_vers_1_1_0 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_freetext_vers_1_1_0.json") }

// organization
val organizationJson by lazy { getResourceAsString("/fhir/kbv_parser/organization/organization.json") }
val organizationAllPresentJson by lazy { getResourceAsString("/fhir/kbv_parser/organization/kbv_organization_all_present.json") }
val organizationNoAddressJson by lazy { getResourceAsString("/fhir/kbv_parser/organization/kbv_organization_no_address.json") }
val organizationNoEmailJson by lazy { getResourceAsString("/fhir/kbv_parser/organization/kbv_organization_no_email.json") }
val organizationNoFaxJson by lazy { getResourceAsString("/fhir/kbv_parser/organization/kbv_organization_no_fax.json") }
val organizationNoTelecomJson by lazy { getResourceAsString("/fhir/kbv_parser/organization/kbv_organization_no_telecom.json") }

// patient
val patientJson_vers_1_0_2 by lazy { getResourceAsString("/fhir/kbv_parser/patient/patient.json") }
val patientJson_vers_1_0_2_with_incomplete_birthDate by lazy { getResourceAsString("/fhir/kbv_parser/patient/patient_incomplete_birth_date.json") }
val patientJson_vers_1_1_0 by lazy { getResourceAsString("/fhir/kbv_parser/patient/patient_vers_1_1_0.json") }

// practitioner
val practitionerJson by lazy { getResourceAsString("/fhir/kbv_parser/practitioner/practitioner.json") }
val practitionerJson110 by lazy { getResourceAsString("/fhir/kbv_parser/practitioner/practitioner_vers_1_1_0.json") }

// diga
val digaSimple by lazy { getResourceAsString("/fhir/kbv_parser/diga/diga.json") }
val digaWithAccidentJson by lazy { getResourceAsString("/fhir/kbv_parser/diga/diga_arbeitsunfall.json") }
val digaWithAccident2Json by lazy { getResourceAsString("/fhir/kbv_parser/diga/diga_arbeitsunfall2.json") }
val digaWithAccident3Json by lazy { getResourceAsString("/fhir/kbv_parser/diga/diga_arbeitsunfall3.json") }
val digaOccupationalDiseaseJson by lazy { getResourceAsString("/fhir/kbv_parser/diga/diga_berufskrankheit.json") }
val digaHospitalWithAddressJson by lazy { getResourceAsString("/fhir/kbv_parser/diga/diga_krankenhaus_standortnummer.json") }
val digaSelJson by lazy { getResourceAsString("/fhir/kbv_parser/diga/diga_sel.json") }
val digaInjuryJson by lazy { getResourceAsString("/fhir/kbv_parser/diga/diga_unfall.json") }
val digaDentistJson by lazy { getResourceAsString("/fhir/kbv_parser/diga/diga_zahnarzt.json") }

// kbv-bundle
val kbvBundle_110_1 by lazy { getResourceAsString("/fhir/kbv_parser/kbv_cleaned_20250212_175155.json") }
val kbvBundle_110_2 by lazy { getResourceAsString("/fhir/kbv_parser/kbv_cleaned_20250212_175156.json") }
val kbvBundle_110_3 by lazy { getResourceAsString("/fhir/kbv_parser/kbv_with_different_full_url.json") }
val kbvBundle_device_request_1_4 by lazy { getResourceAsString("/fhir/kbv_parser/kbv_with_diga.json") }

// fhir-vzd pharmacy bundle
val fhirVzdPharmacyBundle by lazy { getResourceAsString("/fhir/pharmacy_fhirvzd_parser/pharamcy_bundle_fhirvzd.json") }

// fhir-vzd diga bundle
val fhirVzdOrganizationBundle by lazy { getResourceAsString("/fhir/pharmacy_fhirvzd_parser/organization_bundle_fhirvzd.json") }

// fhir-communication
val ReplyCommTestBundleV1_4 by lazy { getResourceAsString("/fhir/communications_parser/communications_reply_bundle_version_1_4.json") }
val DispenseCommTestBundleV1_4 by lazy { getResourceAsString("/fhir/communications_parser/communications_dispense_bundle_version_1_4.json") }
val MixedBundleV1_4Json by lazy { getResourceAsString("/fhir/communications_parser/communications_mix_bundle_version_1_4.json") }
val MixedBundleV1_2Json by lazy { getResourceAsString("/fhir/communications_parser/communications_mix_bundle_version_1_2.json") }
val SingleReplyCommV1_4 by lazy { getResourceAsString("/fhir/communications_parser/communications_reply_version_1_4.json") }
val SingleReplyCommV1_3 by lazy { getResourceAsString("/fhir/communications_parser/communications_reply_version_1_3.json") }
val SingleReplyCommV1_2 by lazy { getResourceAsString("/fhir/communications_parser/communications_reply_version_1_2.json") }
val SingleDispenseCommV1_4 by lazy { getResourceAsString("/fhir/communications_parser/communications_dispense_version_1_4.json") }
val SingleDispenseCommV1_2 by lazy { getResourceAsString("/fhir/communications_parser/communications_dispense_version_1_2.json") }
val communication_diga_dispense_1_4 by lazy { getResourceAsString("/fhir/communications_parser/communication_diga_dispense_1_4.json") }

// fhir-medication-dispense
val medication_dispense_legacy_simple by lazy { getResourceAsString("/fhir/dispense_parser/legacy/medication_dispense_legacy_simple.json") }
val medication_dispense_legacy_unknown_medication_profile by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/legacy/medication_dispense_legacy_unknown_medication_profile.json"
    )
}

// NOTE: [medication_dispense_legacy_unknown_medication_list] will never happen, test added to only harden parser
val medication_dispense_legacy_unknown_medication_list by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/legacy/medication_dispense_legacy_unknown_medication_list.json"
    )
}
val medication_dispense_legacy_without_category by lazy { getResourceAsString("/fhir/dispense_parser/legacy/medication_dispense_legacy_without_category.json") }
val medication_dispense_bundle_version_1_2 by lazy { getResourceAsString("/fhir/dispense_parser/legacy/bundle_med_dispense_version_1_2.json") }

// fhir 1.4 medication dispense medication
val medication_dispense_medication_1_4_complex by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/1_4_workflow/medication_dispense_1_4_medication_kombipackung.json"
    )
}
val medication_dispense_medication_1_4_pharmaceutical_product by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/1_4_workflow/medication_dispense_1_4_medication_pharmaceutical_product.json"
    )
}
val medication_dispense_medication_1_4_simple by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/1_4_workflow/medication_dispense_1_4_medication_simple.json"
    )
}

// fhir 1.4 medication dispense
val medication_dispense_1_4_compounding by lazy { getResourceAsString("/fhir/dispense_parser/1_4_workflow/medication_dispense_1_4_dispense_compounding.json") }
val medication_dispense_1_4_free_text by lazy { getResourceAsString("/fhir/dispense_parser/1_4_workflow/medication_dispense_1_4_dispense_free_text.json") }
val medication_dispense_1_4_simple by lazy { getResourceAsString("/fhir/dispense_parser/1_4_workflow/medication_dispense_1_4_dispense_simple.json") }
val medication_dispense_1_4_no_medication by lazy { getResourceAsString("/fhir/dispense_parser/1_4_workflow/medication_dispense_1_4_without_medication.json") }

// fhir DiGA medication dispense
val medication_dispense_diga_deeplink by lazy { getResourceAsString("/fhir/dispense_parser/diga/medication_dispense_diga_deeplink.json") }
val medication_dispense_diga_name_and_pzn by lazy { getResourceAsString("/fhir/dispense_parser/diga/medication_dispense_diga_name_and_pzn.json") }
val medication_dispense_diga_no_redeem_code by lazy { getResourceAsString("/fhir/dispense_parser/diga/medication_dispense_diga_no_redeem_code.json") }

// fhir-medication-dispense (bundle) 1.4 workflow
val bundle_dispense_1_4_complex_bundle by lazy { getResourceAsString("/fhir/dispense_parser/1_4_workflow/bundle_dispense_compounding_medication.json") }
val bundle_dispense_1_4_simple by lazy { getResourceAsString("/fhir/dispense_parser/1_4_workflow/bundle_dispense_simple_medication.json") }
val bundle_dispenses_1_4_multiple_simple_medications by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/1_4_workflow/bundle_multiple_dispenses_simple_medications.json"
    )
}

// fhir-audit events
val bundle_audit_events_1_2 by lazy { getResourceAsString("/fhir/audit_event_parser/audit_event_bundle_version_1_2.json") }
val bundle_audit_events_1_1 by lazy { getResourceAsString("/fhir/audit_event_parser/audit_event_bundle_version_1_1.json") }
