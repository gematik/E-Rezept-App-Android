/*
 * Copyright 2025, gematik GmbH
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

import de.gematik.ti.erp.app.fhir.common.model.original.FhirCoding
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.common.model.original.FhirPeriod
import de.gematik.ti.erp.app.fhir.common.model.original.FhirRatio
import de.gematik.ti.erp.app.fhir.common.model.original.FhirRatioValue
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequest
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestComments
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestDispenseRequest
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestDosageInstruction
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestQuantityValue
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestSubstitution
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestText

internal object FhirMedicationRequestTestData {
    val fhirMedicationRequestModelV102 = FhirMedicationRequest(
        commentsSection = null,
        resourceType = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.0.2")
        ),
        extensions = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG",
                valueBoolean = true
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Accident",
                extensions = listOf(
                    FhirExtension(
                        url = "unfallkennzeichen",
                        valueCoding = FhirCoding(
                            system = "https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Ursache_Type",
                            code = "2"
                        )
                    ),
                    FhirExtension(
                        url = "unfallbetrieb",
                        valueString = "Dummy-Betrieb"
                    ),
                    FhirExtension(
                        url = "unfalltag",
                        valueDate = "2022-06-29"
                    )
                )
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee",
                valueBoolean = false
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription",
                extensions = listOf(
                    FhirExtension(
                        url = "Kennzeichen",
                        valueBoolean = true
                    ),
                    FhirExtension(
                        url = "Nummerierung",
                        valueRatio = FhirRatio(
                            numerator = FhirRatioValue(value = "1"),
                            denominator = FhirRatioValue(value = "4")
                        )
                    ),
                    FhirExtension(
                        url = "Zeitraum",
                        valuePeriod = FhirPeriod(
                            start = "2022-08-17",
                            end = "2022-11-25"
                        )
                    ),
                    FhirExtension(
                        url = "ID",
                        valueIdentifier = FhirIdentifier(
                            system = "urn:ietf:rfc:3986",
                            value = "urn:uuid:24e2e10d-e962-4d1c-be4f-8760e690a5f0"
                        )
                    )
                )
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment",
                valueCoding = FhirCoding(
                    system = "https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_StatusCoPayment",
                    code = "2"
                )
            )
        ),
        status = "active",
        intent = "order",
        authoredOn = "2022-08-17",
        dosageInstruction = listOf(
            FhirMedicationRequestDosageInstruction(
                text = "1-2-1-2-0",
                extensions = listOf(
                    FhirExtension(
                        url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag",
                        valueBoolean = true
                    )
                )
            )
        ),
        note = listOf(
            FhirMedicationRequestText(text = "Bitte laengliche Tabletten.")
        ),
        dispenseRequest = FhirMedicationRequestDispenseRequest(
            quantity = FhirMedicationRequestQuantityValue(
                value = "1.0",
                system = "http://unitsofmeasure.org",
                code = "{Package}"
            )
        ),
        substitution = FhirMedicationRequestSubstitution(allowed = true)
    )

    val fhirMedicationRequestModelV110 = FhirMedicationRequest(
        commentsSection = FhirMedicationRequestComments(
            comments = listOf("Beispiel MedicationRequest für eine PZN-Verordnung ")
        ),
        resourceType = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.1.0")
        ),
        extensions = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_StatusCoPayment",
                valueCoding = FhirCoding(
                    system = "https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_StatusCoPayment",
                    code = "0"
                )
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee",
                valueBoolean = false
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG",
                valueBoolean = false
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription",
                extensions = listOf(
                    FhirExtension(
                        url = "Kennzeichen",
                        valueBoolean = true
                    ),
                    FhirExtension(
                        url = "Nummerierung",
                        valueRatio = FhirRatio(
                            numerator = FhirRatioValue(value = "1"),
                            denominator = FhirRatioValue(value = "4")
                        )
                    ),
                    FhirExtension(
                        url = "Zeitraum",
                        valuePeriod = FhirPeriod(
                            start = "2022-05-20",
                            end = "2022-06-30"
                        )
                    ),
                    FhirExtension(
                        url = "ID",
                        valueIdentifier = FhirIdentifier(
                            system = "urn:ietf:rfc:3986",
                            value = "urn:uuid:24e2e10d-e962-4d1c-be4f-8760e690a5f0"
                        )
                    )
                )
            )
        ),
        status = "active",
        intent = "order",
        authoredOn = "2022-05-20",
        dosageInstruction = listOf(
            FhirMedicationRequestDosageInstruction(
                text = null,
                extensions = listOf(
                    FhirExtension(
                        url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag",
                        valueBoolean = false
                    )
                )
            )
        ),
        note = emptyList(),
        dispenseRequest = FhirMedicationRequestDispenseRequest(
            quantity = FhirMedicationRequestQuantityValue(
                value = "1",
                system = "http://unitsofmeasure.org",
                code = "{Package}"
            )
        ),
        substitution = FhirMedicationRequestSubstitution(allowed = false)
    )
}
