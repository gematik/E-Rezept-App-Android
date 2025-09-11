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

import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileType
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileVersion
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationProfileErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskMedicationCategoryErpModel
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIdentifierErpModel
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIngredientErpModel
import de.gematik.ti.erp.app.fhir.support.FhirQuantityErpModel
import de.gematik.ti.erp.app.fhir.support.FhirRatioErpModel

object FhirMedicationErpTestData {

    val erpMedicationPznModelV13 = FhirTaskKbvMedicationErpModel(
        text = "Neupro 4MG/24H PFT 7ST",
        form = "PFT",
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.PZN,
            version = ErpMedicationProfileVersion.V_13
        ),
        amount = null,
        isVaccine = false,
        normSizeCode = "N1",
        compoundingInstructions = null,
        compoundingPackaging = null,
        ingredients = listOf(
            FhirMedicationIngredientErpModel(
                text = "Rotigotin",
                amount = null,
                form = null,
                strengthRatio = FhirRatioErpModel(
                    numerator = FhirQuantityErpModel(
                        value = "4",
                        unit = "mg/24 h"
                    ),
                    denominator = FhirQuantityErpModel(
                        value = "1",
                        unit = "Stück"
                    )
                ),
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = "11164213",
                    atc = null,
                    ask = "30404",
                    snomed = null
                )
            )
        ),
        identifier = FhirMedicationIdentifierErpModel(
            pzn = "11164213",
            atc = null,
            ask = "30404",
            snomed = null
        ),
        lotNumber = null,
        expirationDate = null
    )

    val erpMedicationPznModelV12 = FhirTaskKbvMedicationErpModel(
        text = "Venlafaxin - 1 A Pharma® 75mg 100 Tabl. N3",
        form = "TAB",
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.PZN,
            version = ErpMedicationProfileVersion.V_12
        ),
        amount = null,
        isVaccine = false,
        normSizeCode = "N3",
        compoundingInstructions = null,
        compoundingPackaging = null,
        ingredients = listOf(
            FhirMedicationIngredientErpModel(
                text = "Venlafaxinhydrochlorid",
                amount = null,
                form = null,
                strengthRatio = FhirRatioErpModel(
                    numerator = FhirQuantityErpModel(value = "84.88", unit = "mg"),
                    denominator = FhirQuantityErpModel(value = "1", unit = null)
                ),
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = "05392039",
                    atc = null,
                    ask = null,
                    snomed = null
                )
            )
        ),
        identifier = FhirMedicationIdentifierErpModel(
            pzn = "05392039",
            atc = null,
            ask = null,
            snomed = null
        ),
        lotNumber = null,
        expirationDate = null
    )

    val erpMedicationPznWithAmountModelV12 = FhirTaskKbvMedicationErpModel(
        text = "Olanzapin Heumann 20mg 70 Schmelztbl. N3",
        form = "SMT",
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.PZN,
            version = ErpMedicationProfileVersion.V_12
        ),
        amount = FhirRatioErpModel(
            numerator = FhirQuantityErpModel(value = "70", unit = "Stück"),
            denominator = FhirQuantityErpModel(value = "1", unit = "")
        ),
        isVaccine = true,
        normSizeCode = "N3",
        compoundingInstructions = null,
        compoundingPackaging = null,
        ingredients = listOf(
            FhirMedicationIngredientErpModel(
                text = "Olanzapin",
                amount = null,
                form = null,
                strengthRatio = FhirRatioErpModel(
                    numerator = FhirQuantityErpModel(value = "20", unit = "mg"),
                    denominator = FhirQuantityErpModel(value = "1", unit = null)
                ),
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = "08850519",
                    atc = null,
                    ask = null,
                    snomed = null
                )
            )
        ),
        identifier = FhirMedicationIdentifierErpModel(
            pzn = "08850519",
            atc = null,
            ask = null,
            snomed = null
        ),
        lotNumber = null,
        expirationDate = null
    )
    val erpMedicationPznModelV102 = FhirTaskKbvMedicationErpModel(
        text = "Ich bin in Einlösung",
        form = "IHP",
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.PZN,
            version = ErpMedicationProfileVersion.V_102
        ),
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
        form = "TEI",
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.PZN,
            version = ErpMedicationProfileVersion.V_110
        ),
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
        form = "Flüssigkeiten",
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.Ingredient,
            version = ErpMedicationProfileVersion.V_102
        ),
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
        form = "Tabletten",
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.Ingredient,
            version = ErpMedicationProfileVersion.V_110
        ),
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

    val erpMedicationIngredientModelV13 = FhirTaskKbvMedicationErpModel(
        text = null,
        form = "Tabletten",
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.Ingredient,
            version = ErpMedicationProfileVersion.V_13
        ),
        amount = null,
        isVaccine = false,
        normSizeCode = "N3",
        compoundingInstructions = null,
        compoundingPackaging = null,
        ingredients = listOf(
            FhirMedicationIngredientErpModel(
                text = "Simvastatin",
                amount = null,
                form = null,
                strengthRatio = FhirRatioErpModel(
                    numerator = FhirQuantityErpModel(
                        value = "20",
                        unit = "mg"
                    ),
                    denominator = FhirQuantityErpModel(
                        value = "1",
                        unit = "Stück"
                    )
                ),
                identifier = FhirMedicationIdentifierErpModel(
                    pzn = null,
                    atc = null,
                    ask = "23816",
                    snomed = null
                )
            )
        ),
        identifier = FhirMedicationIdentifierErpModel(
            pzn = null,
            atc = null,
            ask = "23816",
            snomed = null
        ),
        lotNumber = null,
        expirationDate = null
    )

    val erpMedicationCompoundingMedicationV102 = FhirTaskKbvMedicationErpModel(
        text = null,
        form = "Lösung",
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.Compounding,
            version = ErpMedicationProfileVersion.V_102
        ),
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
        form = "Kapseln",
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.Compounding,
            version = ErpMedicationProfileVersion.V_110
        ),
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

    val erpMedicationCompoundingMedicationV13 = FhirTaskKbvMedicationErpModel(
        text = null,
        form = "Lösung",
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.Compounding,
            version = ErpMedicationProfileVersion.V_13
        ),
        amount = FhirRatioErpModel(
            numerator = FhirQuantityErpModel(
                value = "100",
                unit = "ml"
            ),
            denominator = FhirQuantityErpModel(
                value = "1",
                unit = ""
            )
        ),
        isVaccine = false,
        normSizeCode = null,
        compoundingInstructions = null,
        compoundingPackaging = null,
        ingredients = listOf(
            FhirMedicationIngredientErpModel(
                text = "Salicylsäure",
                amount = null,
                form = null,
                strengthRatio = FhirRatioErpModel(
                    numerator = FhirQuantityErpModel(
                        value = "5",
                        unit = "g"
                    ),
                    denominator = FhirQuantityErpModel(
                        value = "1",
                        unit = null
                    )
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

    val erpMedicationFreeTextModelV102 = FhirTaskKbvMedicationErpModel(
        text = "Freitext",
        form = null,
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.FreeText,
            version = ErpMedicationProfileVersion.V_102
        ),
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
        form = null,
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.FreeText,
            version = ErpMedicationProfileVersion.V_110
        ),
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

    val erpMedicationFreeTextModelV13 = FhirTaskKbvMedicationErpModel(
        text = "Dummy-Impfstoff als Freitext",
        form = null,
        medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.FreeText,
            version = ErpMedicationProfileVersion.V_13
        ),
        amount = null,
        isVaccine = true,
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
