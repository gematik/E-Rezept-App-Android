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

import de.gematik.ti.erp.app.fhir.common.model.original.FhirCodeableConcept
import de.gematik.ti.erp.app.fhir.common.model.original.FhirCoding
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirDeviceRequestModel

internal object FhirDeviceRequestTestData {

    val fhirDeviceRequestModelWithoutAccidentInfo = FhirDeviceRequestModel(
        id = "a1533e28-4631-4afa-b5e6-f233fad87f53",
        resourceType = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_EVDGA_HealthAppRequest|1.0")
        ),
        extension = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_EVDGA_SER",
                valueBoolean = false
            )
            // No accident info included
        ),
        status = "active",
        intent = "order",
        authoredOn = "2023-03-26",
        code = FhirCodeableConcept(
            coding = listOf(
                FhirCoding(
                    system = "http://fhir.de/CodeSystem/ifa/pzn",
                    code = "19205615"
                )
            ),
            text = "Vantis KHK und Herzinfarkt 001"
        )
    )

    val fhirDeviceRequestModelWithAccidentInfo = FhirDeviceRequestModel(
        id = "a6528123-f17c-4a67-bdbc-7509a8ccdb47",
        resourceType = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_EVDGA_HealthAppRequest|1.0")
        ),
        extension = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_EVDGA_SER",
                valueBoolean = false
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Accident",
                extensions = listOf(
                    FhirExtension(
                        url = "Unfallkennzeichen",
                        valueCoding = FhirCoding(
                            system = "https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Ursache_Type",
                            code = "2"
                        )
                    ),
                    FhirExtension(
                        url = "Unfalltag",
                        valueDate = "2023-03-26"
                    ),
                    FhirExtension(
                        url = "Unfallbetrieb",
                        valueString = "Dummy-Betrieb"
                    )
                )
            )
        ),
        status = "active",
        intent = "order",
        authoredOn = "2023-03-26",
        code = FhirCodeableConcept(
            coding = listOf(
                FhirCoding(
                    system = "http://fhir.de/CodeSystem/ifa/pzn",
                    code = "17850263"
                )
            ),
            text = "companion patella"
        )
    )

    val fhirDeviceRequestModelWithAccident2 = FhirDeviceRequestModel(
        id = null,
        resourceType = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_EVDGA_HealthAppRequest|1.0")
        ),
        extension = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_EVDGA_SER",
                valueBoolean = false
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Accident",
                extensions = listOf(
                    FhirExtension(
                        url = "Unfallkennzeichen",
                        valueCoding = FhirCoding(
                            system = "https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Ursache_Type",
                            code = "2" // Arbeitsunfall
                        )
                    ),
                    FhirExtension(
                        url = "Unfalltag",
                        valueDate = "2023-03-26"
                    ),
                    FhirExtension(
                        url = "Unfallbetrieb",
                        valueString = "Dummy-Betrieb"
                    )
                )
            )
        ),
        status = "active",
        intent = "order",
        authoredOn = "2023-03-26",
        code = FhirCodeableConcept(
            coding = listOf(
                FhirCoding(
                    system = "http://fhir.de/CodeSystem/ifa/pzn",
                    code = "17850263"
                )
            ),
            text = "companion patella"
        )
    )

    val fhirDeviceRequestModelAccident3 = FhirDeviceRequestModel(
        id = "a6528123-f17c-4a67-bdbc-7509a8ccdb47",
        resourceType = FhirMeta(
            profiles = listOf(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_EVDGA_HealthAppRequest|1.0"
            )
        ),
        extension = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_EVDGA_SER",
                valueBoolean = false
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Accident",
                extensions = listOf(
                    FhirExtension(
                        url = "Unfallkennzeichen",
                        valueCoding = FhirCoding(
                            system = "https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Ursache_Type",
                            code = "2" // Arbeitsunfall
                        )
                    ),
                    FhirExtension(
                        url = "Unfalltag",
                        valueDate = "2023-03-26"
                    ),
                    FhirExtension(
                        url = "Unfallbetrieb",
                        valueString = "Dummy-Betrieb"
                    )
                )
            )
        ),
        status = "active",
        intent = "order",
        authoredOn = "2023-03-26",
        code = FhirCodeableConcept(
            coding = listOf(
                FhirCoding(
                    system = "http://fhir.de/CodeSystem/ifa/pzn",
                    code = "17850263"
                )
            ),
            text = "companion patella"
        )
    )

    val fhirDeviceRequestModelWithOccupationalDisease = FhirDeviceRequestModel(
        id = "a6528123-f17c-4a67-bdbc-7509a8ccdb47",
        resourceType = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_EVDGA_HealthAppRequest|1.0")
        ),
        extension = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_EVDGA_SER",
                valueBoolean = false
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Accident",
                extensions = listOf(
                    FhirExtension(
                        url = "Unfallkennzeichen",
                        valueCoding = FhirCoding(
                            system = "https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Ursache_Type",
                            code = "4"
                        )
                    )
                )
            )
        ),
        status = "active",
        intent = "order",
        authoredOn = "2023-03-26",
        code = FhirCodeableConcept(
            coding = listOf(
                FhirCoding(
                    system = "http://fhir.de/CodeSystem/ifa/pzn",
                    code = "17622734"
                )
            ),
            text = "Mawendo 001"
        )
    )

    val fhirDeviceRequestModelTinnitus = FhirDeviceRequestModel(
        id = "d41f1c25-bf46-4226-aceb-9948ab2b5bdd",
        resourceType = FhirMeta(
            profiles = listOf(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_EVDGA_HealthAppRequest|1.0"
            )
        ),
        extension = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_EVDGA_SER",
                valueBoolean = false
            )
        ),
        status = "active",
        intent = "order",
        authoredOn = "2023-03-26",
        code = FhirCodeableConcept(
            coding = listOf(
                FhirCoding(
                    system = "http://fhir.de/CodeSystem/ifa/pzn",
                    code = "18053770"
                )
            ),
            text = "Meine Tinnitus App 001"
        )
    )

    val fhirDeviceRequestModelWithoutSelfUse = FhirDeviceRequestModel(
        id = "d933d532-ecba-44f5-8a6d-c40376ffcf04",
        resourceType = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_EVDGA_HealthAppRequest|1.0")
        ),
        extension = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_EVDGA_SER",
                valueBoolean = false
            )
        ),
        status = "active",
        intent = "order",
        authoredOn = "2023-03-26",
        code = FhirCodeableConcept(
            coding = listOf(
                FhirCoding(
                    system = "http://fhir.de/CodeSystem/ifa/pzn",
                    code = "19205615"
                )
            ),
            text = "Vantis KHK und Herzinfarkt 001"
        )
    )

    val fhirDeviceRequestModelWithInjury = FhirDeviceRequestModel(
        id = "a1533e28-4631-4afa-b5e6-f233fad87f53",
        resourceType = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_EVDGA_HealthAppRequest|1.0")
        ),
        extension = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_EVDGA_SER",
                valueBoolean = false
            ),
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Accident",
                extensions = listOf(
                    FhirExtension(
                        url = "Unfallkennzeichen",
                        valueCoding = FhirCoding(
                            system = "https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Ursache_Type",
                            code = "1" // 1 = Schulunfall (school injury)
                        )
                    ),
                    FhirExtension(
                        url = "Unfalltag",
                        valueDate = "2023-03-26"
                    )
                )
            )
        ),
        status = "active",
        intent = "order",
        authoredOn = "2023-03-26",
        code = FhirCodeableConcept(
            coding = listOf(
                FhirCoding(
                    system = "http://fhir.de/CodeSystem/ifa/pzn",
                    code = "19205615"
                )
            ),
            text = "Vantis KHK und Herzinfarkt 001"
        )
    )

    val fhirDeviceRequestModelWithDentist = FhirDeviceRequestModel(
        id = "625e0b13-3a43-43ee-98f2-be7f8539089d",
        resourceType = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_EVDGA_HealthAppRequest|1.0")
        ),
        extension = listOf(
            FhirExtension(
                url = "https://fhir.kbv.de/StructureDefinition/KBV_EX_EVDGA_SER",
                valueBoolean = false
            )
            // No accident block here, assuming this is a standard request for dental therapy
        ),
        status = "active",
        intent = "order",
        authoredOn = "2023-03-26",
        code = FhirCodeableConcept(
            coding = listOf(
                FhirCoding(
                    system = "http://fhir.de/CodeSystem/ifa/pzn",
                    code = "17946626"
                )
            ),
            text = "HelloBetter Schmerzen 001"
        )
    )
}
