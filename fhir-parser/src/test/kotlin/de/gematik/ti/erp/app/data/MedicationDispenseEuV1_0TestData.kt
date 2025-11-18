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

val medication_dispense_eu_1_0_single_dispense by lazy {
    getResourceAsString("/fhir/dispense_parser/eu/bundle_medication_dispense_eu_v1_0_single_dispense.json")
}
val medication_dispense_eu_1_0_multiple_dispense by lazy {
    getResourceAsString("/fhir/dispense_parser/eu/bundle_medication_dispense_eu_v1_0_multiple_dispense.json")
}

val medication_dispense_eu_1_0_single_dispense_urn by lazy {
    getResourceAsString("/fhir/dispense_parser/eu/bundle_medication_dispense_eu_v1_0_single_dispense_urn.json")
}

val fhir_model_medication_eu_dispense by lazy {
    getResourceAsString("/fhir/dispense_parser/eu/fhir_model_medication_eu_dispense.json")
}

val fhir_model_medication_eu_multiple_dispense by lazy {
    getResourceAsString("/fhir/dispense_parser/eu/fhir_model_medication_eu_multiple_dispense.json")
}

val fhir_model_medication_eu_single_dispense_urn by lazy {
    getResourceAsString("/fhir/dispense_parser/eu/fhir_model_medication_eu_dispense_urn.json")
}
