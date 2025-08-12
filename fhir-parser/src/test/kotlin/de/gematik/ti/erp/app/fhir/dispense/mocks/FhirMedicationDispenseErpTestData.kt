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
import de.gematik.ti.erp.app.fhir.dispense.model.DispensedIngredientMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.DispensedPznMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.FhirDispensedFreeTextMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.FhirMedicationDispenseErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.IngredientContextualData
import de.gematik.ti.erp.app.fhir.dispense.model.PznContextualData
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIdentifierErpModel
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIngredientErpModel
import de.gematik.ti.erp.app.fhir.support.FhirQuantityErpModel
import de.gematik.ti.erp.app.fhir.support.FhirRatioErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

val erp_model_1_2 by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/erp_model_1_2.json"
    )
}

val erp_model_1_4_simple by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/erp_model_1_4_simple.json"
    )
}

val erp_model_1_4_complex by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/erp_model_1_4_complex.json"
    )
}

val erp_model_1_4_kombi_complex by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/erp_model_1_4_kombi_complex.json"
    )
}

val erp_model_1_4_multiple_simple by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/erp_model_1_4_multiple_simple.json"
    )
}

val erp_model_diga_deeplink by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/erp_model_diga_deeplink.json"
    )
}

val erp_model_diga_name_and_pzn by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/erp_model_diga_name_and_pzn.json"
    )
}

val erp_model_diga_no_redeem_code by lazy {
    getResourceAsString(
        "/fhir/dispense_parser/mocks/erp_model_diga_no_redeem_code.json"
    )
}

val simpleMedicationDispense = FhirMedicationDispenseErpModel(
    dispenseId = "160.000.000.031.686.59",
    patientId = "X110535541",
    substitutionAllowed = false,
    dosageInstruction = null,
    performer = "3-SMC-B-Testkarte-883110000116873",
    handedOver = FhirTemporal.LocalDate(LocalDate.parse("2022-07-12")),
    dispensedMedication = listOf(
        DispensedPznMedicationErpModel(
            text = "Defamipin",
            category = null,
            form = "FET",
            amount = FhirRatioErpModel(
                numerator = FhirQuantityErpModel(value = "18", unit = "Stk"),
                denominator = FhirQuantityErpModel(value = "1", unit = null)
            ),
            isVaccine = false,
            lotNumber = "8521037577",
            expirationDate = FhirTemporal.Instant(Instant.parse("2023-05-02T06:26:06Z")),
            contextualData = PznContextualData(
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = "06491772",
                    atc = null,
                    ask = null,
                    snomed = null
                ),
                normSizeCode = "Sonstiges"
            )
        )
    ),
    dispensedDeviceRequest = null
)

val unknownMedicationProfileDispense = FhirMedicationDispenseErpModel(
    dispenseId = "160.000.000.031.686.59",
    patientId = "X110535541",
    substitutionAllowed = false,
    dosageInstruction = null,
    performer = "3-SMC-B-Testkarte-883110000116873",
    handedOver = FhirTemporal.LocalDate(LocalDate.parse("2022-07-12")),
    dispensedMedication = listOf(
        DispensedPznMedicationErpModel(
            text = "Defamipin",
            category = null,
            form = "FET",
            amount = FhirRatioErpModel(
                numerator = FhirQuantityErpModel(
                    value = "18",
                    unit = "Stk"
                ),
                denominator = FhirQuantityErpModel(
                    value = "1",
                    unit = null
                )
            ),
            isVaccine = false,
            lotNumber = "8521037577",
            expirationDate = FhirTemporal.Instant(Instant.parse("2023-05-02T06:26:06Z")),
            contextualData = PznContextualData(
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = "06491772",
                    atc = null,
                    ask = null,
                    snomed = null
                ),
                normSizeCode = "Sonstiges"
            )
        )
    ),
    dispensedDeviceRequest = null
)

val multipleMedicationDispense = FhirMedicationDispenseErpModel(
    dispenseId = "160.000.000.099.999.99",
    patientId = "X000000000",
    substitutionAllowed = false,
    dosageInstruction = null,
    performer = "3-SMC-B-Testkarte-000000000000000",
    handedOver = FhirTemporal.LocalDate(LocalDate.parse("2025-03-31")),
    dispensedMedication = listOf(
        DispensedPznMedicationErpModel(
            text = "Ibuprofen",
            category = null,
            form = "TAB",
            amount = FhirRatioErpModel(
                numerator = FhirQuantityErpModel(value = "20", unit = "Stk"),
                denominator = FhirQuantityErpModel(value = "1", unit = null)
            ),
            isVaccine = false,
            lotNumber = null,
            expirationDate = null,
            contextualData = PznContextualData(
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = "12345678",
                    atc = null,
                    ask = null,
                    snomed = null
                ),
                normSizeCode = null
            )
        ),
        DispensedIngredientMedicationErpModel(
            text = "Compounded Cream",
            category = null,
            form = "Cream",
            amount = null,
            isVaccine = false,
            lotNumber = null,
            expirationDate = null,
            contextualData = IngredientContextualData(
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = null,
                    atc = null,
                    ask = null,
                    snomed = null
                ),
                normSizeCode = null,
                ingredients = listOf(
                    FhirMedicationIngredientErpModel(
                        text = "Clotrimazole",
                        amount = null,
                        form = null,
                        strengthRatio = null,
                        identifier = FhirMedicationIdentifierErpModel(
                            pzn = null,
                            atc = null,
                            ask = null,
                            snomed = null
                        )
                    )
                )
            )
        ),
        DispensedIngredientMedicationErpModel(
            text = "Compound X",
            category = null,
            form = null,
            amount = null,
            isVaccine = false,
            lotNumber = null,
            expirationDate = null,
            contextualData = IngredientContextualData(
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = null,
                    atc = null,
                    ask = null,
                    snomed = null
                ),
                normSizeCode = null,
                ingredients = listOf(
                    FhirMedicationIngredientErpModel(
                        text = null,
                        amount = null,
                        form = null,
                        strengthRatio = FhirRatioErpModel(
                            numerator = FhirQuantityErpModel(value = "2.8", unit = "mg"),
                            denominator = FhirQuantityErpModel(value = "1", unit = "Sprühstoß")
                        ),
                        identifier = FhirMedicationIdentifierErpModel(
                            pzn = null,
                            atc = null,
                            ask = null,
                            snomed = null
                        )
                    )
                )
            )
        ),
        FhirDispensedFreeTextMedicationErpModel(
            text = "Freestyle migraine drops - use as directed",
            category = null,
            form = "Liquid",
            amount = null,
            isVaccine = false,
            lotNumber = null,
            expirationDate = null
        )
    ),
    dispensedDeviceRequest = null
)

val medicationDispenseNoCategory = FhirMedicationDispenseErpModel(
    dispenseId = "160.000.000.031.686.59",
    patientId = "X110535541",
    substitutionAllowed = false,
    dosageInstruction = null,
    performer = "3-SMC-B-Testkarte-883110000116873",
    handedOver = FhirTemporal.LocalDate(value = LocalDate.parse("2022-07-12")),
    dispensedMedication = listOf(
        DispensedPznMedicationErpModel(
            text = "Defamipin",
            category = null,
            form = "FET",
            amount = FhirRatioErpModel(
                numerator = FhirQuantityErpModel(value = "18", unit = "Stk"),
                denominator = FhirQuantityErpModel(value = "1", unit = null)
            ),
            isVaccine = false,
            lotNumber = "8521037577",
            expirationDate = FhirTemporal.Instant(Instant.parse("2023-05-02T06:26:06Z")),
            contextualData = PznContextualData(
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = "06491772",
                    atc = null,
                    ask = null,
                    snomed = null
                ),
                normSizeCode = "Sonstiges"
            )
        )
    ),
    dispensedDeviceRequest = null
)
