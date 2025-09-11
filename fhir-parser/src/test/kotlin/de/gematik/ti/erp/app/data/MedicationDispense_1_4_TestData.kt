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

// fhir 1.4 medication dispense
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
val medication_dispense_1_4_compounding by lazy { getResourceAsString("/fhir/dispense_parser/1_4_workflow/medication_dispense_1_4_dispense_compounding.json") }
val medication_dispense_1_4_free_text by lazy { getResourceAsString("/fhir/dispense_parser/1_4_workflow/medication_dispense_1_4_dispense_free_text.json") }
val medication_dispense_1_4_simple by lazy { getResourceAsString("/fhir/dispense_parser/1_4_workflow/medication_dispense_1_4_dispense_simple.json") }
val medication_dispense_1_4_no_medication by lazy { getResourceAsString("/fhir/dispense_parser/1_4_workflow/medication_dispense_1_4_without_medication.json") }

// fhir DiGA medication dispense (1.4)
val medication_dispense_1_4_diga_deeplink by lazy { getResourceAsString("/fhir/dispense_parser/diga/medication_dispense_diga_deeplink.json") }
val medication_dispense_1_4_diga_name_and_pzn by lazy { getResourceAsString("/fhir/dispense_parser/diga/medication_dispense_diga_name_and_pzn.json") }
val medication_dispense_1_4_diga_no_redeem_code by lazy { getResourceAsString("/fhir/dispense_parser/diga/medication_dispense_diga_no_redeem_code.json") }

// fhir-medication-dispense (bundle) 1.4 workflow
val bundle_dispense_1_4_complex_bundle by lazy { getResourceAsString("/fhir/dispense_parser/1_4_workflow/bundle_dispense_compounding_medication.json") }
val bundle_dispense_1_4_simple by lazy { getResourceAsString("/fhir/dispense_parser/1_4_workflow/bundle_dispense_simple_medication.json") }
val bundle_dispenses_1_4_multiple_simple_medications by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/1_4_workflow/bundle_multiple_dispenses_simple_medications.json"
    )
}
