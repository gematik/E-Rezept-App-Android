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

@file:Suppress("ktlint:max-line-length")

package de.gematik.ti.erp.app.data

// task metadata + kbv bundle
val taskMetadataBundleKbvBundle by lazy { getResourceAsString("/fhir/bundle_seperation_parser/task_metadata_bundle_kbv_bundle.json") }
val taskMetadataBundleKbvWithDigaBundle by lazy { getResourceAsString("/fhir/bundle_seperation_parser/task_metadata_bundle_kbv_bundle_with_diga.json") }

// task-initial-bundle
val task_bundle_vers_1_2 by lazy { getResourceAsString("/fhir/entry_parser/task_bundle_vers_1_2.json") }
val task_bundle_vers_1_3 by lazy { getResourceAsString("/fhir/entry_parser/task_bundle_vers_1_3.json") }
val task_bundle_vers_1_5 by lazy { getResourceAsString("/fhir/entry_parser/task_bundle_vers_1_5.json") }

// task metadata
val taskJson_vers_1_2 by lazy { getResourceAsString("/fhir/metadata_parser/task_vers_1_2.json") }
val taskJson_vers_1_3 by lazy { getResourceAsString("/fhir/metadata_parser/task_vers_1_3.json") }
val taskJson_vers_1_5 by lazy { getResourceAsString("/fhir/metadata_parser/task_vers_1_5.json") }

// insurance information
val coverage1_v103_json by lazy { getResourceAsString("/fhir/kbv_parser/coverage/coverage1_v103.json") }
val coverage2_v103_json by lazy { getResourceAsString("/fhir/kbv_parser/coverage/coverage2_v103.json") }
val coverage1_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/coverage/coverage1_v110.json") }
val coverage2_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/coverage/coverage2_v110.json") }
val coverage3_v12_json by lazy { getResourceAsString("/fhir/kbv_parser/coverage/coverage3_v1_2.json") }

// medication request
val medicationRequestJson_vers_1_0_2 by lazy { getResourceAsString("/fhir/kbv_parser/medication_request/medication_request_1_0_2.json") }
val medicationRequestJson_vers_1_1_0 by lazy { getResourceAsString("/fhir/kbv_parser/medication_request/medication_request_1_1_0.json") }
val medicationRequestJson_vers_1_1_0_with_accident by lazy {
    getResourceAsString(
        "/fhir/kbv_parser/medication_request/medication_request_1_1_0_with_accident.json"
    )
}
val medicationRequestJson_vers_1_2 by lazy {
    getResourceAsString(
        "/fhir/kbv_parser/medication_request/medication_request_1_2.json"
    )
}

val medicationRequestJson_vers_1_3 by lazy {
    getResourceAsString(
        "/fhir/kbv_parser/medication_request/medication_request_1_3.json"
    )
}

// medication
val medicationPznJson_vers_1_0_2 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_pzn_1_0_2.json") }
val medicationPznJson_vers_1_1_0 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_pzn_1_1_0.json") }
val medicationPznJson_vers_1_2 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_pzn_1_2.json") }
val medicationPznWithAmountJson_vers_1_2 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_pzn_1_2_with_amount.json") }
val medicationPznJson_vers_1_3 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_pzn_1_3.json") }
val medicationIngredientJson_vers_1_0_2 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_ingredient_1_0_2.json") }
val medicationIngredientJson_vers_1_1_0 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_ingredient_1_1_0.json") }
val medicationIngredientJson_vers_1_3 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_ingredient_1_3.json") }
val medicationCompoundingJson_vers_1_0_2 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_compounding_1_0_2.json") }
val medicationCompoundingJson_vers_1_1_0 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_compounding_1_1_0.json") }
val medicationCompoundingJson_vers_1_3 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_compounding_1_3.json") }
val medicationFreetextJson_vers_1_0_2 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_freetext_1_0_2.json") }
val medicationFreetextJson_vers_1_1_0 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_freetext_1_1_0.json") }
val medicationFreetextJson_vers_1_3 by lazy { getResourceAsString("/fhir/kbv_parser/medication/medication_freetext_1_3.json") }

// organization
val organization1_v103_json by lazy { getResourceAsString("/fhir/kbv_parser/organization/organization1_v103.json") }
val organization2_v103_json by lazy { getResourceAsString("/fhir/kbv_parser/organization/organization2_v103.json") }
val organization1AllPresent_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/organization/organization1_all_present_v110.json") }
val organization1NoAddress_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/organization/organization1_no_address_v110.json") }
val organization1NoEmail_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/organization/organization1_no_email_v110.json") }
val organization1NoFax_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/organization/organization1_no_fax_v110.json") }
val organization1NoTelecom_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/organization/organization1_no_telecom_v110.json") }
val organization2_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/organization/organization2_v110.json") }
val organization3_v12_json by lazy { getResourceAsString("/fhir/kbv_parser/organization/organization3_v1_2.json") }
val organization4_additionalData_v12_json by lazy { getResourceAsString("/fhir/kbv_parser/organization/organization4_additional_locator_v1_2.json") }

// patient
val patient1_v103_json by lazy { getResourceAsString("/fhir/kbv_parser/patient/patient1_v103.json") }
val patient2_v103_json by lazy { getResourceAsString("/fhir/kbv_parser/patient/patient2_v103.json") }
val patient1_incomplete_birth_date_v103_json by lazy { getResourceAsString("/fhir/kbv_parser/patient/patient1_incomplete_birth_date_v103.json") }
val patient1_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/patient/patient1_v110.json") }
val patient2_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/patient/patient2_v110.json") }
val patient3_pkv_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/patient/patient3_pkv_v110.json") }
val patient4_v12_json by lazy { getResourceAsString("/fhir/kbv_parser/patient/patient4_v1_2.json") }
val patient5_v12_json by lazy { getResourceAsString("/fhir/kbv_parser/patient/patient5_v1_2.json") }

// practitioner
val practitioner1_v103_json by lazy { getResourceAsString("/fhir/kbv_parser/practitioner/practitioner1_v103.json") }
val practitioner2_v103_json by lazy { getResourceAsString("/fhir/kbv_parser/practitioner/practitioner2_v103.json") }
val practitioner1_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/practitioner/practitioner1_v110.json") }
val practitioner2_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/practitioner/practitioner2_v110.json") }
val practitioner3_v12_json by lazy { getResourceAsString("/fhir/kbv_parser/practitioner/practitioner3_v1_2.json") }
val practitioner4_zanr_v12_json by lazy { getResourceAsString("/fhir/kbv_parser/practitioner/practitioner4_zanr_v1_2.json") }

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
val kbvBundle1_v102_json by lazy { getResourceAsString("/fhir/kbv_parser/bundle/kbvBundle1_v102.json") }
val kbvBundle1_incomplete_v102_json by lazy { getResourceAsString("/fhir/kbv_parser/bundle/kbvBundle1_incomplete_v102.json") }
val kbvBundle1_gkv_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/bundle/kbvBundle1_gkv_v110.json") }
val kbvBundle1_incomplete_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/bundle/kbvBundle1_incomplete_v110.json") }
val kbvBundle2_pkv_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/bundle/kbvBundle2_pkv_v110.json") }
val kbvBundle3_gkv_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/bundle/kbvBundle3_gkv_v110.json") }
val kbvBundle4_pkv_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/bundle/kbvBundle4_pkv_v110.json") }
val kbvBundle5_gkv_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/bundle/kbvBundle5_gkv_v110.json") }
val kbvBundle6_gkv_v110_json by lazy { getResourceAsString("/fhir/kbv_parser/bundle/kbvBundle6_gkv_v110.json") }
val kbvBundle_v1_2_json by lazy { getResourceAsString("/fhir/kbv_parser/bundle/kbv_bundle_v1_2.json") }
val kbvBundle_v1_3_json by lazy { getResourceAsString("/fhir/kbv_parser/bundle/kbv_bundle_v1_3.json") }
val kbvBundle_v1_3_example2_json by lazy { getResourceAsString("/fhir/kbv_parser/bundle/kbv_bundle_example_2_v1_3.json") }
val kbvBundle_v1_3_example3_json by lazy { getResourceAsString("/fhir/kbv_parser/bundle/kbv_bundle_example_3_v1_3.json") }

// kbv-diga-bundle
val kbvBundle_device_request_1_4 by lazy { getResourceAsString("/fhir/kbv_parser/bundle/kbv_with_diga.json") }

// fhir-vzd pharmacy bundle
val fhirVzdPharmacyBundle by lazy { getResourceAsString("/fhir/pharmacy_fhirvzd_parser/pharamcy_bundle_fhirvzd.json") }

// fhir-vzd diga bundle
val fhirVzdOrganizationBundle by lazy { getResourceAsString("/fhir/pharmacy_fhirvzd_parser/organization_bundle_fhirvzd.json") }
val fhirVzdCountriesBundle by lazy { getResourceAsString("/fhir/pharmacy_fhirvzd_parser/countries_bundle_fhirvzd.json") }

// speciality types pharmacy
val bundle_speciality_simple by lazy { getResourceAsString("/fhir/pharmacy_fhirvzd_parser/speciality_bundle_different_types.json") }
val bundle_speciality_complex by lazy { getResourceAsString("/fhir/pharmacy_fhirvzd_parser/speciality_bundle_duplicate_text.json") }
