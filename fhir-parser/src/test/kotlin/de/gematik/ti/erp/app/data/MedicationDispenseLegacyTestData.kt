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

package de.gematik.ti.erp.app.data

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
