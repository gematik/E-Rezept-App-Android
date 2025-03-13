/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.fhir.prescription.mocks

import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirCodeableConcept
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirCoding
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirExtensionReduced
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedication
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationAmount
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationAmountNumerator
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationAmountValueExtension
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationIngredient
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirRatio
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirRatioValue

object FhirMedicationTestData {
    val fhirMedicationPznModelV102 = FhirMedication(
        resourceType = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.2")
        ),
        extensions = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category",
                valueCoding = FhirCoding(
                    system = "https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category",
                    code = "00",
                    version = null,
                    display = null
                ),
                valueCodeableConcept = null,
                valueCode = null,
                valueString = null,
                valueDate = null,
                valueBoolean = null,
                valueRatio = null,
                valuePeriod = null,
                valueIdentifier = null,
                extensions = emptyList()
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine",
                valueCoding = null,
                valueCodeableConcept = null,
                valueCode = null,
                valueString = null,
                valueDate = null,
                valueBoolean = false,
                valueRatio = null,
                valuePeriod = null,
                valueIdentifier = null,
                extensions = emptyList()
            ),
            FhirExtension(
                url = "http://fhir.de/StructureDefinition/normgroesse",
                valueCoding = null,
                valueCodeableConcept = null,
                valueCode = "N1",
                valueString = null,
                valueDate = null,
                valueBoolean = null,
                valueRatio = null,
                valuePeriod = null,
                valueIdentifier = null,
                extensions = emptyList()
            )
        ),
        code = FhirCodeableConcept(
            coding = listOf(
                FhirCoding(
                    system = "http://fhir.de/CodeSystem/ifa/pzn",
                    code = "00427833",
                    version = null,
                    display = null
                )
            ),
            text = "Ich bin in Einlösung"
        ),
        form = FhirCodeableConcept(
            coding = listOf(
                FhirCoding(
                    system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM",
                    code = "IHP",
                    version = null,
                    display = null
                )
            ),
            text = null
        ),
        amount = FhirMedicationAmount(
            numerator = FhirMedicationAmountNumerator(
                valueDirect = "1",
                valueFromExtension = emptyList(),
                unit = "Diskus",
                system = "http://unitsofmeasure.org",
                code = "{tbl}"
            ),
            denominator = FhirRatioValue(value = "1", unit = null)
        )
    )

    val fhirMedicationPznModelV110 = FhirMedication(
        resourceType = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.1.0")
        ),
        extensions = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_Base_Medication_Type",
                valueCoding = null,
                valueCodeableConcept = FhirCoding(
                    system = null,
                    coding = listOf(
                        FhirCoding(
                            system = "http://snomed.info/sct",
                            code = "763158003",
                            version = "http://snomed.info/sct/900000000000207008/version/20220331",
                            display = "Medicinal product (product)"
                        )
                    ),
                    code = null,
                    version = null,
                    display = null
                ),
                valueCode = null,
                valueString = null,
                valueDate = null,
                valueBoolean = null,
                valueRatio = null,
                valuePeriod = null,
                valueIdentifier = null,
                extensions = emptyList()
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category",
                valueCoding = FhirCoding(
                    system = "https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category",
                    code = "00",
                    version = null,
                    display = null
                ),
                valueCodeableConcept = null,
                valueCode = null,
                valueString = null,
                valueDate = null,
                valueBoolean = null,
                valueRatio = null,
                valuePeriod = null,
                valueIdentifier = null,
                extensions = emptyList()
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine",
                valueCoding = null,
                valueCodeableConcept = null,
                valueCode = null,
                valueString = null,
                valueDate = null,
                valueBoolean = false,
                valueRatio = null,
                valuePeriod = null,
                valueIdentifier = null,
                extensions = emptyList()
            )
        ),
        code = FhirCodeableConcept(
            coding = listOf(
                FhirCoding(
                    system = "http://fhir.de/CodeSystem/ifa/pzn",
                    code = "03507952",
                    version = null,
                    display = null
                )
            ),
            text = "Novaminsulfon 500 mg Lichtenstein 100 ml Tropf. N3"
        ),
        form = FhirCodeableConcept(
            coding = listOf(
                FhirCoding(
                    system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM",
                    code = "TEI",
                    version = null,
                    display = null
                )
            ),
            text = null
        ),
        amount = FhirMedicationAmount(
            numerator = FhirMedicationAmountNumerator(
                valueDirect = null,
                valueFromExtension = listOf(
                    FhirMedicationAmountValueExtension(
                        url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_PackagingSize",
                        valueString = "100"
                    )
                ),
                unit = "ml",
                system = "http://unitsofmeasure.org",
                code = "mL"
            ),
            denominator = FhirRatioValue(value = "1", unit = null)
        )
    )

    val medicationIngredientModelV102 = FhirMedication(
        resourceType = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Ingredient|1.0.2")
        ),
        extensions = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category",
                valueCoding = FhirCoding(
                    system = "https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category",
                    code = "00",
                    version = null,
                    display = null
                ),
                valueCodeableConcept = null,
                valueCode = null,
                valueString = null,
                valueDate = null,
                valueBoolean = null,
                valueRatio = null,
                valuePeriod = null,
                valueIdentifier = null,
                extensions = emptyList()
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine",
                valueCoding = null,
                valueCodeableConcept = null,
                valueCode = null,
                valueString = null,
                valueDate = null,
                valueBoolean = false,
                valueRatio = null,
                valuePeriod = null,
                valueIdentifier = null,
                extensions = emptyList()
            ),
            FhirExtension(
                url = "http://fhir.de/StructureDefinition/normgroesse",
                valueCoding = null,
                valueCodeableConcept = null,
                valueCode = "N1",
                valueString = null,
                valueDate = null,
                valueBoolean = null,
                valueRatio = null,
                valuePeriod = null,
                valueIdentifier = null,
                extensions = emptyList()
            )
        ),
        code = FhirCodeableConcept(
            coding = listOf(
                FhirCoding(
                    system = "https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Type",
                    code = "wirkstoff",
                    version = null,
                    display = null
                )
            ),
            text = null
        ),
        form = FhirCodeableConcept(
            coding = emptyList(),
            text = "Flüssigkeiten"
        ),
        amount = null,
        ingredients = listOf(
            FhirMedicationIngredient(
                itemCodeableConcept = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            system = "http://fhir.de/CodeSystem/ask",
                            code = "37197",
                            version = null,
                            display = null
                        )
                    ),
                    text = "Wirkstoff Paulaner Weissbier"
                ),
                strength = FhirRatio(
                    numerator = FhirRatioValue(value = "1", unit = "Maß"),
                    denominator = FhirRatioValue(value = "1", unit = null)
                )
            )
        )
    )

    val medicationIngredientModelV110 = FhirMedication(
        resourceType = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Ingredient|1.1.0")
        ),
        extensions = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine",
                valueCoding = null,
                valueCodeableConcept = null,
                valueCode = null,
                valueString = null,
                valueDate = null,
                valueBoolean = false,
                valueRatio = null,
                valuePeriod = null,
                valueIdentifier = null,
                extensions = emptyList()
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category",
                valueCoding = FhirCoding(
                    system = "https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category",
                    code = "00",
                    version = null,
                    display = null
                ),
                valueCodeableConcept = null,
                valueCode = null,
                valueString = null,
                valueDate = null,
                valueBoolean = null,
                valueRatio = null,
                valuePeriod = null,
                valueIdentifier = null,
                extensions = emptyList()
            ),
            FhirExtension(
                url = "http://fhir.de/StructureDefinition/normgroesse",
                valueCoding = null,
                valueCodeableConcept = null,
                valueCode = "N3",
                valueString = null,
                valueDate = null,
                valueBoolean = null,
                valueRatio = null,
                valuePeriod = null,
                valueIdentifier = null,
                extensions = emptyList()
            )
        ),
        code = FhirCodeableConcept(
            coding = listOf(
                FhirCoding(
                    system = "https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Type",
                    code = "wirkstoff",
                    version = null,
                    display = null
                )
            ),
            text = null
        ),
        form = FhirCodeableConcept(
            coding = emptyList(),
            text = "Tabletten"
        ),
        amount = FhirMedicationAmount(
            numerator = FhirMedicationAmountNumerator(
                valueDirect = null,
                valueFromExtension = listOf(
                    FhirMedicationAmountValueExtension(
                        url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_PackagingSize",
                        valueString = "100"
                    )
                ),
                unit = "Stück",
                system = null,
                code = null
            ),
            denominator = FhirRatioValue(value = "1", unit = null)
        ),
        ingredients = listOf(
            FhirMedicationIngredient(
                itemCodeableConcept = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            system = "http://fhir.de/CodeSystem/ask",
                            code = "22686",
                            version = null,
                            display = null
                        )
                    ),
                    text = "Ramipril"
                ),
                strength = FhirRatio(
                    numerator = FhirRatioValue(value = "5", unit = "mg"),
                    denominator = FhirRatioValue(value = "1", unit = null)
                )
            )
        )
    )

    val fhirMedicationCompoundingModelV102 = FhirMedication(
        resourceType = FhirMeta(profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Compounding|1.0.2")),
        extensions = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category",
                valueCoding = FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category", code = "00"),
                extensions = emptyList()
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine",
                valueBoolean = false,
                extensions = emptyList()
            )
        ),
        code = FhirCodeableConcept(
            coding = listOf(FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Type", code = "rezeptur")),
            text = null
        ),
        form = FhirCodeableConcept(text = "Lösung"),
        amount = FhirMedicationAmount(
            numerator = FhirMedicationAmountNumerator(valueDirect = "100", unit = "ml"),
            denominator = FhirRatioValue(value = "1")
        ),
        ingredients = listOf(
            FhirMedicationIngredient(
                itemCodeableConcept = FhirCodeableConcept(text = "1_3 Graf 02.08.2022"),
                strength = FhirRatio(
                    numerator = FhirRatioValue(value = "5", unit = "g"),
                    denominator = FhirRatioValue(value = "1")
                )
            ),
            FhirMedicationIngredient(
                itemCodeableConcept = FhirCodeableConcept(text = "2-propanol 70 %"),
                strength = FhirRatio(
                    extensions = listOf(
                        FhirExtensionReduced(
                            url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Ingredient_Amount",
                            valueString = "Ad 100 g"
                        )
                    )
                )
            )
        )
    )

    val fhirMedicationCompoundingModelV110 = FhirMedication(
        resourceType = FhirMeta(profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Compounding|1.1.0")),
        extensions = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_Base_Medication_Type",
                valueCodeableConcept = FhirCoding(
                    coding = listOf(
                        FhirCoding(
                            system = "http://snomed.info/sct",
                            code = "373873005:860781008=362943005",
                            version = "http://snomed.info/sct/900000000000207008/version/20220331",
                            display = "Pharmaceutical / biologic product (product) : " +
                                "Has product characteristic (attribute) = Manual method (qualifier value)"
                        )
                    )
                )
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category",
                valueCoding = FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category", code = "00")
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine",
                valueBoolean = false
            )
        ),
        code = FhirCodeableConcept(
            coding = listOf(FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Type", code = "rezeptur"))
        ),
        form = FhirCodeableConcept(text = "Kapseln"),
        amount = FhirMedicationAmount(
            numerator = FhirMedicationAmountNumerator(
                valueFromExtension = listOf(
                    FhirMedicationAmountValueExtension(
                        url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_PackagingSize",
                        valueString = "50"
                    )
                ),
                unit = "Stück"
            ),
            denominator = FhirRatioValue(value = "1")
        ),
        ingredients = listOf(
            FhirMedicationIngredient(
                itemCodeableConcept = FhirCodeableConcept(text = "Hydrocortison"),
                strength = FhirRatio(
                    numerator = FhirRatioValue(value = "0.06", unit = "g"),
                    denominator = FhirRatioValue(value = "1")
                )
            ),
            FhirMedicationIngredient(
                itemCodeableConcept = FhirCodeableConcept(text = "Mannit"),
                strength = FhirRatio(
                    numerator = FhirRatioValue(value = "12.5", unit = "g"),
                    denominator = FhirRatioValue(value = "1")
                )
            ),
            FhirMedicationIngredient(
                itemCodeableConcept = FhirCodeableConcept(text = "Siliciumdioxid"),
                strength = FhirRatio(
                    numerator = FhirRatioValue(value = "0.5", unit = "g"),
                    denominator = FhirRatioValue(value = "1")
                )
            )
        )
    )

    val fhirMedicationFreeTextModelV102 = FhirMedication(
        resourceType = FhirMeta(profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText|1.0.2")),
        extensions = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category",
                valueCoding = FhirCoding(
                    coding = emptyList(),
                    system = "https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category",
                    code = "00",
                    version = null,
                    display = null
                ),
                valueCodeableConcept = null,
                valueCode = null,
                valueString = null,
                valueDate = null,
                valueBoolean = null,
                valueRatio = null,
                valuePeriod = null,
                valueIdentifier = null,
                extensions = emptyList()
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine",
                valueCoding = null,
                valueCodeableConcept = null,
                valueCode = null,
                valueString = null,
                valueDate = null,
                valueBoolean = false,
                valueRatio = null,
                valuePeriod = null,
                valueIdentifier = null,
                extensions = emptyList()
            )
        ),
        code = FhirCodeableConcept(
            coding = listOf(
                FhirCoding(
                    coding = emptyList(),
                    system = "https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Type",
                    code = "freitext",
                    version = null,
                    display = null
                )
            ),
            text = "Freitext"
        ),
        form = null,
        amount = null,
        ingredients = emptyList()
    )

    val fhirMedicationFreeTextModelV110 = FhirMedication(
        resourceType = FhirMeta(profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText|1.1.0")),
        extensions = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category",
                valueCoding = FhirCoding(
                    coding = emptyList(),
                    system = "https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category",
                    code = "00",
                    version = null,
                    display = null
                ),
                valueCodeableConcept = null,
                valueCode = null,
                valueString = null,
                valueDate = null,
                valueBoolean = null,
                valueRatio = null,
                valuePeriod = null,
                valueIdentifier = null,
                extensions = emptyList()
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine",
                valueCoding = null,
                valueCodeableConcept = null,
                valueCode = null,
                valueString = null,
                valueDate = null,
                valueBoolean = false,
                valueRatio = null,
                valuePeriod = null,
                valueIdentifier = null,
                extensions = emptyList()
            )
        ),
        code = FhirCodeableConcept(
            coding = listOf(
                FhirCoding(
                    coding = emptyList(),
                    system = "https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Type",
                    code = "freitext",
                    version = null,
                    display = null
                )
            ),
            text = "Metformin 850mg Tabletten N3"
        ),
        form = null,
        amount = null,
        ingredients = emptyList()
    )
}
