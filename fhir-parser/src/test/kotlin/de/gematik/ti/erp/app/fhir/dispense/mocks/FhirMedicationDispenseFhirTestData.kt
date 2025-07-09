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

package de.gematik.ti.erp.app.fhir.dispense.mocks

import de.gematik.ti.erp.app.data.getResourceAsString

/**
 * These JSON objects represent encoded FHIR models.
 *
 * They serve as **snapshots** of the actual FHIR resources received from the backend or external systems.
 * The structure reflects the serialized form of domain models defined in the application, typically using
 * `kotlinx.serialization`.
 *
 * These snapshots are useful for:
 * - Debugging and validation of incoming FHIR payloads.
 * - Ensuring backwards compatibility across FHIR version changes.
 * - Serving as reference fixtures for unit or integration tests.
 *
 * ⚠️ Note: The content and structure of these JSONs are version-specific and may evolve as
 * the FHIR specification or server implementation changes.
 */

val fhir_model_medication_dispense by lazy { getResourceAsString("/fhir/dispense_parser/mocks/fhir_model_medication_dispense_legacy.json") }
val fhir_model_medication_dispense_unknown_medication_profile by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_dispense_legacy_unknown_medication_profile.json"
    )
}
val fhir_model_medication_dispense_without_category by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_dispense_legacy_without_category.json"
    )
}

val fhir_model_medication_dispense_compounding by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_dispense_compounding.json"
    )
}

val fhir_model_medication_dispense_free_text by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_dispense_free_text.json"
    )
}

val fhir_model_medication_dispense_simple by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_dispense_simple.json"
    )
}

val fhir_model_medication_1_4_complex by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_1_4_complex.json"
    )
}

val fhir_model_medication_1_4_pharma_product by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_1_4_pharma_product.json"
    )
}

val fhir_model_medication_1_4_simple by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_1_4_simple.json"
    )
}

val fhir_model_medication_dispense_diga_deeplink by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_dispense_diga_deeplink.json"
    )
}

val fhir_model_medication_dispense_diga_name_and_pzn by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_dispense_diga_name_and_pzn.json"
    )
}

val fhir_model_medication_dispense_diga_no_redeem_code by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/fhir_model_medication_dispense_diga_no_redeem_code.json"
    )
}
