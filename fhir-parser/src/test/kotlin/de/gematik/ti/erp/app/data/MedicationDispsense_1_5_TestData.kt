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

@file:Suppress("ktlint:standard:max-line-length")

package de.gematik.ti.erp.app.data

// fhir 1.5 medication dispense

val medication_dispense_1_5_diga_deeplink by lazy { getResourceAsString("/fhir/dispense_parser/1_5_workflow/medication_dispense_1_5_diga_deeplink.json") }
val medication_dispense_1_5_diga_name_and_pzn by lazy {
    getResourceAsString("/fhir/dispense_parser/1_5_workflow/medication_dispense_1_5_diga_name_and_pzn.json")
}
val medication_dispense_1_5_diga_no_redeem_code by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/1_5_workflow/medication_dispense_1_5_diga_no_redeem_code.json"
    )
}
val medication_dispense_1_5_kombipackung by lazy { getResourceAsString("/fhir/dispense_parser/1_5_workflow/medication_dispense_1_5_kombipackung.json") }
val medication_dispense_1_5_rezeptur by lazy { getResourceAsString("/fhir/dispense_parser/1_5_workflow/medication_dispense_1_5_rezeptur.json") }
val medication_dispense_1_5_without_medication by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/1_5_workflow/medication_dispense_1_5_without_medication.json"
    )
}
val medication_dispense_medication_resource_1_5_without_strength_numerator by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/1_5_workflow/medication_dispense_medication_1_5_without_strength_numerator.json"
    )
}

// fhir-medication-dispense (bundle) 1.5 workflow
val bundle_dispense_1_5_single by lazy { getResourceAsString("/fhir/dispense_parser/1_5_workflow/bundle_dispense_single_1_5_pzn.json") }
val bundle_dispense_1_5_multiple by lazy { getResourceAsString("/fhir/dispense_parser/1_5_workflow/bundle_dispense_multiple_1_5_pzn.json") }
