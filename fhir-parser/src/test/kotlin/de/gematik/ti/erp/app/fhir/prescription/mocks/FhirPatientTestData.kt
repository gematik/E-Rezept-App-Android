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

import de.gematik.ti.erp.app.fhir.common.model.original.FhirAddress
import de.gematik.ti.erp.app.fhir.common.model.original.FhirCodeableConcept
import de.gematik.ti.erp.app.fhir.common.model.original.FhirCoding
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtensionReduced
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.common.model.original.FhirName
import de.gematik.ti.erp.app.fhir.common.model.original.FhirNameFamilyExtension
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirPatient

internal object FhirPatientTestData {
    val fhirPatient1_v103 = FhirPatient(
        resourceType = "Patient",
        id = "fc0d145b-09b4-4af6-b477-935c1862ac7f",
        meta = FhirMeta(profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3")),
        identifiers = listOf(
            FhirIdentifier(
                system = "http://fhir.de/NamingSystem/gkv/kvid-10",
                value = "X110535541",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            system = "http://fhir.de/CodeSystem/identifier-type-de-basis",
                            code = "GKV"
                        )
                    )
                )
            )
        ),
        names = listOf(
            FhirName(
                use = "official",
                family = "Graf Freiherr von Schinder",
                familyExtension = FhirNameFamilyExtension(
                    extensions = listOf(
                        FhirExtensionReduced(
                            url = "http://fhir.de/StructureDefinition/humanname-namenszusatz",
                            valueString = "Graf Freiherr"
                        ),
                        FhirExtensionReduced(
                            url = "http://hl7.org/fhir/StructureDefinition/humanname-own-prefix",
                            valueString = "von"
                        ),
                        FhirExtensionReduced(
                            url = "http://hl7.org/fhir/StructureDefinition/humanname-own-name",
                            valueString = "Schinder"
                        )
                    )
                ),
                given = listOf("Lars"),
                prefix = listOf("Prinzessin")
            )
        ),
        birthDate = "1964-04-04",
        addresses = listOf(
            FhirAddress(
                type = "both",
                line = listOf("Siegburger Str. 155"),
                city = "Köln",
                postalCode = "51105",
                country = "D",
                extractedLine = mapOf("houseNumber" to "155", "streetName" to "Siegburger Str.")
            )
        )
    )

    val fhirPatient2_v103 = FhirPatient(
        resourceType = "Patient",
        id = "9774f67f-a238-4daf-b4e6-679deeef3811",
        meta = FhirMeta(profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3")),
        identifiers = listOf(
            FhirIdentifier(
                system = "http://fhir.de/NamingSystem/gkv/kvid-10",
                value = "X234567890",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            system = "http://fhir.de/CodeSystem/identifier-type-de-basis",
                            code = "GKV"
                        )
                    )
                )
            )
        ),
        names = listOf(
            FhirName(
                use = "official",
                family = "Königsstein",
                familyExtension = FhirNameFamilyExtension(
                    extensions = listOf(
                        FhirExtensionReduced(
                            url = "http://hl7.org/fhir/StructureDefinition/humanname-own-name",
                            valueString = "Königsstein"
                        )
                    )
                ),
                given = listOf("Ludger")
            )
        ),
        birthDate = "1935-06-22",
        addresses = listOf(
            FhirAddress(
                type = "both",
                line = listOf("Musterstr. 1"),
                city = "Berlin",
                postalCode = "10623",
                country = null,
                extractedLine = mapOf("houseNumber" to "1", "streetName" to "Musterstr.")
            )
        )
    )

    val fhirPatient1IncompleteBirth_v103 = FhirPatient(
        resourceType = "Patient",
        id = "fc0d145b-09b4-4af6-b477-935c1862ac7f",
        meta = FhirMeta(profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3")),
        identifiers = listOf(
            FhirIdentifier(
                system = "http://fhir.de/NamingSystem/gkv/kvid-10",
                value = "X110535541",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            system = "http://fhir.de/CodeSystem/identifier-type-de-basis",
                            code = "GKV"
                        )
                    )
                )
            )
        ),
        names = listOf(
            FhirName(
                use = "official",
                family = "Graf Freiherr von Schinder",
                familyExtension = FhirNameFamilyExtension(
                    extensions = listOf(
                        FhirExtensionReduced(
                            url = "http://fhir.de/StructureDefinition/humanname-namenszusatz",
                            valueString = "Graf Freiherr"
                        ),
                        FhirExtensionReduced(
                            url = "http://hl7.org/fhir/StructureDefinition/humanname-own-prefix",
                            valueString = "von"
                        ),
                        FhirExtensionReduced(
                            url = "http://hl7.org/fhir/StructureDefinition/humanname-own-name",
                            valueString = "Schinder"
                        )
                    )
                ),
                given = listOf("Lars"),
                prefix = listOf("Prinzessin")
            )
        ),
        birthDate = "1964",
        addresses = listOf(
            FhirAddress(
                type = "both",
                line = listOf("Siegburger Str. 155"),
                city = "Köln",
                postalCode = "51105",
                country = "D",
                extractedLine = mapOf("houseNumber" to "155", "streetName" to "Siegburger Str.")
            )
        )
    )

    val fhirPatient1_v110 = FhirPatient(
        resourceType = "Patient",
        id = "93866fdc-3e50-4902-a7e9-891b54737b5e",
        meta = FhirMeta(profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.1.0")),
        identifiers = listOf(
            FhirIdentifier(
                system = "http://fhir.de/sid/gkv/kvid-10",
                value = "K220635158",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            system = "http://fhir.de/CodeSystem/identifier-type-de-basis",
                            code = "GKV"
                        )
                    )
                )
            )
        ),
        names = listOf(
            FhirName(
                use = "official",
                family = "Königsstein",
                familyExtension = FhirNameFamilyExtension(
                    extensions = listOf(
                        FhirExtensionReduced(
                            url = "http://hl7.org/fhir/StructureDefinition/humanname-own-name",
                            valueString = "Königsstein"
                        )
                    )
                ),
                given = listOf("Ludger"),
                prefix = emptyList()
            )
        ),
        birthDate = "1935-06-22",
        addresses = listOf(
            FhirAddress(
                type = "both",
                line = listOf("Blumenweg"),
                city = "Esens",
                postalCode = "26427",
                country = "D",
                extractedLine = mapOf("streetName" to "Blumenweg")
            )
        )
    )

    val fhirPatient2_v110 = FhirPatient(
        resourceType = "Patient",
        id = "9774f67f-a238-4daf-b4e6-679deeef3811",
        meta = FhirMeta(profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.1.0")),
        identifiers = listOf(
            FhirIdentifier(
                system = "http://fhir.de/sid/gkv/kvid-10",
                value = "X234567890",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            system = "http://fhir.de/CodeSystem/identifier-type-de-basis",
                            code = "GKV"
                        )
                    )
                )
            )
        ),
        names = listOf(
            FhirName(
                use = "official",
                family = "Königsstein",
                familyExtension = FhirNameFamilyExtension(
                    extensions = listOf(
                        FhirExtensionReduced(
                            url = "http://hl7.org/fhir/StructureDefinition/humanname-own-name",
                            valueString = "Königsstein"
                        )
                    )
                ),
                given = listOf("Ludger")
            )
        ),
        birthDate = "1935-06-22",
        addresses = listOf(
            FhirAddress(
                type = "both",
                line = listOf("Musterstr. 1"),
                city = "Berlin",
                postalCode = "10623",
                country = null,
                extractedLine = mapOf("houseNumber" to "1", "streetName" to "Musterstr.")
            )
        )
    )

    val fhirPatient3_v110 = FhirPatient(
        resourceType = "Patient",
        id = "93866fdc-3e50-4902-a7e9-891b54737b5e",
        meta = FhirMeta(profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.1.0")),
        identifiers = listOf(
            FhirIdentifier(
                system = "http://fhir.de/sid/pkv/kvid-10",
                value = "X110411675",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            system = "http://fhir.de/CodeSystem/identifier-type-de-basis",
                            code = "PKV"
                        )
                    )
                )
            )
        ),
        names = listOf(
            FhirName(
                use = "official",
                family = "Privati",
                familyExtension = FhirNameFamilyExtension(
                    extensions = listOf(
                        FhirExtensionReduced(
                            url = "http://hl7.org/fhir/StructureDefinition/humanname-own-name",
                            valueString = "Privati"
                        )
                    )
                ),
                given = listOf("Paula")
            )
        ),
        birthDate = "1935-06-22",
        addresses = listOf(
            FhirAddress(
                type = "both",
                line = listOf("Blumenweg"),
                city = "Esens",
                postalCode = "26427",
                country = "D",
                extractedLine = mapOf("streetName" to "Blumenweg")
            )
        )
    )
}
