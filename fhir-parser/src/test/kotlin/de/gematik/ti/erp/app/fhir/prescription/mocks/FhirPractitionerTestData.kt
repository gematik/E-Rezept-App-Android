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

import de.gematik.ti.erp.app.fhir.common.model.original.FhirCodeableConcept
import de.gematik.ti.erp.app.fhir.common.model.original.FhirCoding
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtensionReduced
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.common.model.original.FhirName
import de.gematik.ti.erp.app.fhir.common.model.original.FhirNameFamilyExtension
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirPractitioner
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirQualificationCode
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirQualificationText

internal object FhirPractitionerTestData {
    val fhirPractitioner1_v103 = FhirPractitioner(
        resourceType = "Practitioner",
        id = "667ffd79-42a3-4002-b7ca-6b9098f20ccb",
        meta = FhirMeta(
            profiles = listOf(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3"
            )
        ),
        qualifications = listOf(
            FhirQualificationCode(code = FhirQualificationText(text = null)),
            FhirQualificationCode(code = FhirQualificationText(text = "Fachärztin für Innere Medizin"))
        ),
        identifiers = listOf(
            FhirIdentifier(
                system = "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR",
                value = "987654423",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            coding = emptyList(),
                            system = "http://terminology.hl7.org/CodeSystem/v2-0203",
                            code = "LANR",
                            version = null,
                            display = null
                        )
                    ),
                    text = null
                )
            )
        ),
        names = listOf(
            FhirName(
                use = "official",
                family = "Schneider",
                familyExtension = FhirNameFamilyExtension(
                    extensions = listOf(
                        FhirExtensionReduced(
                            url = "http://hl7.org/fhir/StructureDefinition/humanname-own-name",
                            valueString = "Schneider"
                        )
                    )
                ),
                given = listOf("Emma"),
                prefix = listOf("Dr. med.")
            )
        )
    )
    val fhirPractitioner2_v103 = FhirPractitioner(
        resourceType = "Practitioner",
        id = "20597e0e-cb2a-45b3-95f0-dc3dbdb617c3",
        meta = FhirMeta(
            profiles = listOf(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3"
            )
        ),
        identifiers = listOf(
            FhirIdentifier(
                system = "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR",
                value = "838382202",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            coding = emptyList(),
                            system = "http://terminology.hl7.org/CodeSystem/v2-0203",
                            code = "LANR",
                            version = null,
                            display = null
                        )
                    )
                )
            )
        ),
        qualifications = listOf(
            FhirQualificationCode(code = FhirQualificationText(text = null)),
            FhirQualificationCode(code = FhirQualificationText(text = "Hausarzt"))
        ),
        names = listOf(
            FhirName(
                use = "official",
                family = "Topp-Glücklich",
                familyExtension = FhirNameFamilyExtension(
                    extensions = listOf(
                        FhirExtensionReduced(
                            url = "http://hl7.org/fhir/StructureDefinition/humanname-own-name",
                            valueString = "Topp-Glücklich"
                        )
                    )
                ),
                given = listOf("Hans"),
                prefix = listOf("Dr. med.")
            )
        )
    )

    val fhirPractitioner1_v110 = FhirPractitioner(
        resourceType = "Practitioner",
        id = "cb7558e2-0fdf-4107-93f6-07f13f39e067",
        meta = FhirMeta(
            profiles = listOf(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.1.0"
            )
        ),
        qualifications = listOf(
            FhirQualificationCode(code = FhirQualificationText(text = null)),
            FhirQualificationCode(code = FhirQualificationText(text = "Facharzt für Innere Medizin")),
            FhirQualificationCode(code = FhirQualificationText(text = null))
        ),
        names = listOf(
            FhirName(
                use = "official",
                family = "Fischer",
                familyExtension = FhirNameFamilyExtension(
                    extensions = listOf(
                        FhirExtensionReduced(
                            url = "http://hl7.org/fhir/StructureDefinition/humanname-own-name",
                            valueString = "Fischer"
                        )
                    )
                ),
                given = listOf("Alexander"),
                prefix = emptyList()
            )
        )
    )

    val fhirPractitioner2_v110 = FhirPractitioner(
        resourceType = "Practitioner",
        id = "20597e0e-cb2a-45b3-95f0-dc3dbdb617c3",
        meta = FhirMeta(
            profiles = listOf(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.1.0"
            )
        ),
        identifiers = listOf(
            FhirIdentifier(
                system =
                "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR",
                value = "838382202",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            coding = emptyList(),
                            system = "http://terminology.hl7.org/CodeSystem/v2-0203",
                            code = "LANR",
                            version = null,
                            display = null
                        )
                    ),
                    text = null
                )
            ),
            FhirIdentifier(
                system = "https://gematik.de/fhir/sid/telematik-id",
                value = "1-838382202",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            coding = emptyList(),
                            system = "http://terminology.hl7.org/CodeSystem/v2-0203",
                            code = "PRN",
                            version = null,
                            display = null
                        )
                    ),
                    text = null
                )
            )
        ),
        qualifications = listOf(
            FhirQualificationCode(code = FhirQualificationText(text = null)),
            FhirQualificationCode(code = FhirQualificationText(text = "Hausarzt"))
        ),
        names = listOf(
            FhirName(
                use = "official",
                family = "Topp-Glücklich",
                familyExtension = FhirNameFamilyExtension(
                    extensions = listOf(
                        FhirExtensionReduced(
                            url = "http://hl7.org/fhir/StructureDefinition/humanname-own-name",
                            valueString = "Topp-Glücklich"
                        )
                    )
                ),
                given = listOf("Hans"),
                prefix = listOf("Dr. med.")
            )
        )
    )

    val fhirPractitioner3_v12 = FhirPractitioner(
        resourceType = "Practitioner",
        id = "bc329f24-3d65-4286-bf06-b54dd6cad655",
        meta = FhirMeta(
            profiles = listOf(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.2"
            )
        ),
        identifiers = listOf(
            FhirIdentifier(
                system = "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR",
                value = "123456628",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            coding = emptyList(),
                            system = "http://terminology.hl7.org/CodeSystem/v2-0203",
                            code = "LANR",
                            version = null,
                            display = null
                        )
                    ),
                    text = null
                )
            )
        ),
        qualifications = listOf(
            FhirQualificationCode(
                code = FhirQualificationText(text = null)
            ),
            FhirQualificationCode(
                code = FhirQualificationText(text = "Facharzt für Innere Medizin: Kardiologie")
            )
        ),
        names = listOf(
            FhirName(
                use = "official",
                family = "Freiherr von Müller",
                familyExtension = FhirNameFamilyExtension(
                    extensions = listOf(
                        FhirExtensionReduced(
                            url = "http://hl7.org/fhir/StructureDefinition/humanname-own-prefix",
                            valueString = "von",
                            valueCode = null
                        ),
                        FhirExtensionReduced(
                            url = "http://fhir.de/StructureDefinition/humanname-namenszusatz",
                            valueString = "Freiherr",
                            valueCode = null
                        ),
                        FhirExtensionReduced(
                            url = "http://hl7.org/fhir/StructureDefinition/humanname-own-name",
                            valueString = "Müller",
                            valueCode = null
                        )
                    )
                ),
                given = listOf("Paul"),
                prefix = listOf("Dr. med.")
            )
        )
    )

    val fhirPractitioner4_v12 = FhirPractitioner(
        resourceType = "Practitioner",
        id = "bc329f24-3d65-4286-bf06-b54dd6cad655",
        meta = FhirMeta(
            profiles = listOf(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.2"
            )
        ),
        identifiers = listOf(
            FhirIdentifier(
                system = "http://fhir.de/sid/kzbv/zahnarztnummer",
                value = "123456",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            coding = emptyList(),
                            system = "http://fhir.de/CodeSystem/identifier-type-de-basis",
                            code = "ZANR",
                            version = null,
                            display = null
                        )
                    ),
                    text = null
                )
            ),
            FhirIdentifier(
                system = "https://gematik.de/fhir/sid/telematik-id",
                value = "3BB-98z349",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            coding = emptyList(),
                            system = "http://terminology.hl7.org/CodeSystem/v2-0203",
                            code = "PRN",
                            version = null,
                            display = null
                        )
                    ),
                    text = null
                )
            )
        ),
        qualifications = listOf(
            FhirQualificationCode(
                code = FhirQualificationText(text = null)
            ),
            FhirQualificationCode(
                code = FhirQualificationText(text = "Facharzt für Innere Medizin: Kardiologie")
            )
        ),
        names = listOf(
            FhirName(
                use = "official",
                family = "Freiherr von Müller",
                familyExtension = FhirNameFamilyExtension(
                    extensions = listOf(
                        FhirExtensionReduced(
                            url = "http://hl7.org/fhir/StructureDefinition/humanname-own-prefix",
                            valueString = "von",
                            valueCode = null
                        ),
                        FhirExtensionReduced(
                            url = "http://fhir.de/StructureDefinition/humanname-namenszusatz",
                            valueString = "Freiherr",
                            valueCode = null
                        ),
                        FhirExtensionReduced(
                            url = "http://hl7.org/fhir/StructureDefinition/humanname-own-name",
                            valueString = "Müller",
                            valueCode = null
                        )
                    )
                ),
                given = listOf("Paul"),
                prefix = listOf("Dr. med.")
            )
        )
    )
}
