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

package de.gematik.ti.erp.app.fhir.prescription.mocks

import de.gematik.ti.erp.app.fhir.common.model.erp.support.FhirQuantityErpModel
import de.gematik.ti.erp.app.fhir.common.model.erp.support.FhirRatioErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirMedicationIdentifierErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirMedicationIngredientErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvMedicationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskMedicationCategoryErpModel

object FhirMedicationErpTestData {
    val erpMedicationPznModelV102 = FhirTaskKbvMedicationErpModel(
        text = "Ich bin in Einlösung",
        type = "Medication_PZN",
        version = "1.0.2",
        form = "IHP",
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        amount = FhirRatioErpModel(
            numerator = FhirQuantityErpModel(value = "1", unit = "Diskus"),
            denominator = FhirQuantityErpModel(value = "1", unit = null)
        ),
        isVaccine = false,
        normSizeCode = "N1",
        compoundingInstructions = null,
        compoundingPackaging = null,
        ingredients = emptyList(),
        identifier = FhirMedicationIdentifierErpModel(
            pzn = "00427833",
            atc = null,
            ask = null,
            snomed = null
        ),
        lotNumber = null,
        expirationDate = null
    )

    val erpMedicationPznModelV110 = FhirTaskKbvMedicationErpModel(
        text = "Novaminsulfon 500 mg Lichtenstein 100 ml Tropf. N3",
        type = "Medication_PZN",
        version = "1.1.0",
        form = "TEI",
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        amount = FhirRatioErpModel(
            numerator = FhirQuantityErpModel(value = "100", unit = "ml"),
            denominator = FhirQuantityErpModel(value = "1", unit = "")
        ),
        isVaccine = false,
        normSizeCode = null,
        compoundingInstructions = null,
        compoundingPackaging = null,
        ingredients = emptyList(),
        identifier = FhirMedicationIdentifierErpModel(
            pzn = "03507952",
            atc = null,
            ask = null,
            snomed = null
        ),
        lotNumber = null,
        expirationDate = null
    )

    val erpMedicationIngredientModelV102 = FhirTaskKbvMedicationErpModel(
        text = null,
        type = "Medication_Ingredient",
        version = "1.0.2",
        form = "Flüssigkeiten",
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        amount = null,
        isVaccine = false,
        normSizeCode = "N1",
        compoundingInstructions = null,
        compoundingPackaging = null,
        ingredients = listOf(
            FhirMedicationIngredientErpModel(
                text = "Wirkstoff Paulaner Weissbier",
                amount = null,
                form = null,
                strengthRatio = FhirRatioErpModel(
                    numerator = FhirQuantityErpModel(value = "1", unit = "Maß"),
                    denominator = FhirQuantityErpModel(value = "1", unit = null)
                ),
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = null,
                    atc = null,
                    ask = "37197",
                    snomed = null
                )
            )
        ),
        identifier = FhirMedicationIdentifierErpModel(
            pzn = null,
            atc = null,
            ask = "37197",
            snomed = null
        ),
        lotNumber = null,
        expirationDate = null
    )

    val erpMedicationIngredientModelV110 = FhirTaskKbvMedicationErpModel(
        text = null,
        type = "Medication_Ingredient",
        version = "1.1.0",
        form = "Tabletten",
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        amount = FhirRatioErpModel(
            numerator = FhirQuantityErpModel(value = "100", unit = "Stück"),
            denominator = FhirQuantityErpModel(value = "1", unit = "")
        ),
        isVaccine = false,
        normSizeCode = "N3",
        compoundingInstructions = null,
        compoundingPackaging = null,
        ingredients = listOf(
            FhirMedicationIngredientErpModel(
                text = "Ramipril",
                amount = null,
                form = null,
                strengthRatio = FhirRatioErpModel(
                    numerator = FhirQuantityErpModel(value = "5", unit = "mg"),
                    denominator = FhirQuantityErpModel(value = "1", unit = null)
                ),
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = null,
                    atc = null,
                    ask = "22686",
                    snomed = null
                )
            )
        ),
        identifier = FhirMedicationIdentifierErpModel(
            pzn = null,
            atc = null,
            ask = "22686",
            snomed = null
        ),
        lotNumber = null,
        expirationDate = null
    )

    val erpMedicationCompoundingMedicationV102 = FhirTaskKbvMedicationErpModel(
        text = null,
        type = "Medication_Compounding",
        version = "1.0.2",
        form = "Lösung",
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        amount = FhirRatioErpModel(
            numerator = FhirQuantityErpModel(value = "100", unit = "ml"),
            denominator = FhirQuantityErpModel(value = "1", unit = null)
        ),
        isVaccine = false,
        normSizeCode = null,
        compoundingInstructions = null,
        compoundingPackaging = null,
        ingredients = listOf(
            FhirMedicationIngredientErpModel(
                text = "1_3 Graf 02.08.2022",
                amount = null,
                form = null,
                strengthRatio = FhirRatioErpModel(
                    numerator = FhirQuantityErpModel(value = "5", unit = "g"),
                    denominator = FhirQuantityErpModel(value = "1", unit = null)
                ),
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = null,
                    atc = null,
                    ask = null,
                    snomed = null
                )
            ),
            FhirMedicationIngredientErpModel(
                text = "2-propanol 70 %",
                amount = "Ad 100 g",
                form = null,
                strengthRatio = FhirRatioErpModel(
                    numerator = null,
                    denominator = null
                ),
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = null,
                    atc = null,
                    ask = null,
                    snomed = null
                )
            )
        ),
        identifier = FhirMedicationIdentifierErpModel(
            pzn = null,
            atc = null,
            ask = null,
            snomed = null
        ),
        lotNumber = null,
        expirationDate = null
    )

    val erpMedicationCompoundingMedicationV110 = FhirTaskKbvMedicationErpModel(
        text = null,
        type = "Medication_Compounding",
        version = "1.1.0",
        form = "Kapseln",
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        amount = FhirRatioErpModel(
            numerator = FhirQuantityErpModel(value = "50", unit = "Stück"),
            denominator = FhirQuantityErpModel(value = "1", unit = "")
        ),
        isVaccine = false,
        normSizeCode = null,
        compoundingInstructions = null,
        compoundingPackaging = null,
        ingredients = listOf(
            FhirMedicationIngredientErpModel(
                text = "Hydrocortison",
                amount = null,
                form = null,
                strengthRatio = FhirRatioErpModel(
                    numerator = FhirQuantityErpModel(value = "0.06", unit = "g"),
                    denominator = FhirQuantityErpModel(value = "1", unit = null)
                ),
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = null,
                    atc = null,
                    ask = null,
                    snomed = null
                )
            ),
            FhirMedicationIngredientErpModel(
                text = "Mannit",
                amount = null,
                form = null,
                strengthRatio = FhirRatioErpModel(
                    numerator = FhirQuantityErpModel(value = "12.5", unit = "g"),
                    denominator = FhirQuantityErpModel(value = "1", unit = null)
                ),
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = null,
                    atc = null,
                    ask = null,
                    snomed = null
                )
            ),
            FhirMedicationIngredientErpModel(
                text = "Siliciumdioxid",
                amount = null,
                form = null,
                strengthRatio = FhirRatioErpModel(
                    numerator = FhirQuantityErpModel(value = "0.5", unit = "g"),
                    denominator = FhirQuantityErpModel(value = "1", unit = null)
                ),
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = null,
                    atc = null,
                    ask = null,
                    snomed = null
                )
            )
        ),
        identifier = FhirMedicationIdentifierErpModel(
            pzn = null,
            atc = null,
            ask = null,
            snomed = null
        ),
        lotNumber = null,
        expirationDate = null
    )

    val erpMedicationFreeTextModelV102 = FhirTaskKbvMedicationErpModel(
        text = "Freitext",
        type = "Medication_FreeText",
        version = "1.0.2",
        form = null,
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        amount = null,
        isVaccine = false,
        normSizeCode = null,
        compoundingInstructions = null,
        compoundingPackaging = null,
        ingredients = emptyList(),
        identifier = FhirMedicationIdentifierErpModel(
            pzn = null,
            atc = null,
            ask = null,
            snomed = null
        ),
        lotNumber = null,
        expirationDate = null
    )

    val erpMedicationFreeTextModelV110 = FhirTaskKbvMedicationErpModel(
        text = "Metformin 850mg Tabletten N3",
        type = "Medication_FreeText",
        version = "1.1.0",
        form = null,
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        amount = null,
        isVaccine = false,
        normSizeCode = null,
        compoundingInstructions = null,
        compoundingPackaging = null,
        ingredients = emptyList(),
        identifier = FhirMedicationIdentifierErpModel(
            pzn = null,
            atc = null,
            ask = null,
            snomed = null
        ),
        lotNumber = null,
        expirationDate = null
    )
}
