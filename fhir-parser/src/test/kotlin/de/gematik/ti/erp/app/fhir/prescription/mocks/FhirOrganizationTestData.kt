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
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirOrganization
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirTelecom

internal object FhirOrganizationTestData {
    val fhirOrganization1_v103 = FhirOrganization(
        resourceType = "Organization",
        id = "5d3f4ac0-2b44-4d48-b363-e63efa72973b",
        meta = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.0.3")
        ),
        identifiers = listOf(
            FhirIdentifier(
                system = "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR",
                value = "721111100",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            system = "http://terminology.hl7.org/CodeSystem/v2-0203",
                            code = "BSNR"
                        )
                    )
                )
            )
        ),
        name = "MVZ",
        telecoms = listOf(
            FhirTelecom(system = "phone", value = "0301234567"),
            FhirTelecom(system = "fax", value = "030123456789"),
            FhirTelecom(system = "email", value = "mvz@e-mail.de")
        ),
        addresses = listOf(
            FhirAddress(
                type = "both",
                line = listOf("Herbert-Lewin-Platz 2"),
                city = "Berlin",
                postalCode = "10623",
                country = "D",
                extractedLine = mapOf(
                    "houseNumber" to "2",
                    "streetName" to "Herbert-Lewin-Platz"
                )
            )
        )
    )

    val fhirOrganization2_v103 = FhirOrganization(
        resourceType = "Organization",
        id = "cf042e44-086a-4d51-9c77-172f9a972e3b",
        meta = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.0.3")
        ),
        identifiers = listOf(
            FhirIdentifier(
                system = "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR",
                value = "031234567",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            system = "http://terminology.hl7.org/CodeSystem/v2-0203",
                            code = "BSNR"
                        )
                    )
                )
            )
        ),
        name = "Hausarztpraxis Dr. Topp-Glücklich",
        telecoms = listOf(
            FhirTelecom(system = "phone", value = "0301234567")
        ),
        addresses = listOf(
            FhirAddress(
                type = "both",
                line = listOf("Musterstr. 2"),
                city = "Berlin",
                postalCode = "10623",
                country = null,
                extractedLine = mapOf(
                    "houseNumber" to "2",
                    "streetName" to "Musterstr."
                )
            )
        )
    )

    val fhirOrganization1AllPresent_v110 = FhirOrganization(
        resourceType = "Organization",
        id = "5d3f4ac0-2b44-4d48-b363-e63efa72973b",
        meta = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.1.0")
        ),
        identifiers = listOf(
            FhirIdentifier(
                system = "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR",
                value = "721111100",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            system = "http://terminology.hl7.org/CodeSystem/v2-0203",
                            code = "BSNR"
                        )
                    )
                )
            )
        ),
        name = "MVZ",
        telecoms = listOf(
            FhirTelecom(system = "phone", value = "0301234567"),
            FhirTelecom(system = "fax", value = "030123456789"),
            FhirTelecom(system = "email", value = "mvz@e-mail.de")
        ),
        addresses = listOf(
            FhirAddress(
                type = "both",
                line = listOf("Herbert-Lewin-Platz 2"),
                city = "Berlin",
                postalCode = "10623",
                country = "D",
                extractedLine = mapOf(
                    "houseNumber" to "2",
                    "streetName" to "Herbert-Lewin-Platz"
                )
            )
        )
    )

    val fhirOrganization1NoFax_v110 = FhirOrganization(
        resourceType = "Organization",
        id = "5d3f4ac0-2b44-4d48-b363-e63efa72973b",
        meta = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.1.0")
        ),
        identifiers = listOf(
            FhirIdentifier(
                system = "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR",
                value = "721111100",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            system = "http://terminology.hl7.org/CodeSystem/v2-0203",
                            code = "BSNR"
                        )
                    )
                )
            )
        ),
        name = "MVZ",
        telecoms = listOf(
            FhirTelecom(system = "phone", value = "0301234567"),
            FhirTelecom(system = "email", value = "mvz@e-mail.de")
        ),
        addresses = listOf(
            FhirAddress(
                type = "both",
                line = listOf("Herbert-Lewin-Platz 2"),
                city = "Berlin",
                postalCode = "10623",
                country = "D"
            )
        )
    )

    val fhirOrganization1NoTelecom_v110 = FhirOrganization(
        resourceType = "Organization",
        id = "5d3f4ac0-2b44-4d48-b363-e63efa72973b",
        meta = FhirMeta(profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.1.0")),
        identifiers = listOf(
            FhirIdentifier(
                system = "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR",
                value = "721111100",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(system = "http://terminology.hl7.org/CodeSystem/v2-0203", code = "BSNR")
                    )
                )
            )
        ),
        name = "MVZ",
        telecoms = emptyList(),
        addresses = listOf(
            FhirAddress(
                type = "both",
                line = listOf("Herbert-Lewin-Platz 2"),
                city = "Berlin",
                postalCode = "10623",
                country = "D"
            )
        )
    )

    val fhirOrganization1NoAddress_v110 = fhirOrganization1NoTelecom_v110.copy(
        telecoms = listOf(
            FhirTelecom(system = "phone", value = "0301234567"),
            FhirTelecom(system = "fax", value = "030123456789"),
            FhirTelecom(system = "email", value = "mvz@e-mail.de")
        ),
        addresses = emptyList()
    )

    private val noEmail_v110 = fhirOrganization1NoAddress_v110.copy(
        addresses = listOf(
            FhirAddress(
                type = "both",
                line = listOf("Herbert-Lewin-Platz 2"),
                city = "Berlin",
                postalCode = "10623",
                country = "D",
                extractedLine = mapOf(
                    "houseNumber" to "2",
                    "streetName" to "Herbert-Lewin-Platz"
                )
            )
        )
    )

    private val addressWithoutLines = noEmail_v110.addresses?.first()
        ?.copy(extractedLine = emptyMap())

    val fhirOrganization1NoEmail_v110 = noEmail_v110.copy(
        telecoms = listOf(
            FhirTelecom(system = "phone", value = "0301234567"),
            FhirTelecom(system = "fax", value = "030123456789")
        ),
        addresses = listOf(addressWithoutLines!!)
    )

    val fhirOrganization2_v110 = FhirOrganization(
        resourceType = "Organization",
        id = "cf042e44-086a-4d51-9c77-172f9a972e3b",
        meta = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.1.0")
        ),
        identifiers = listOf(
            FhirIdentifier(
                system = "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR",
                value = "031234567",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            system = "http://terminology.hl7.org/CodeSystem/v2-0203",
                            code = "BSNR"
                        )
                    )
                )
            ),
            FhirIdentifier(
                system = "https://gematik.de/fhir/sid/telematik-id",
                value = "1-031234567",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            system = "http://terminology.hl7.org/CodeSystem/v2-0203",
                            code = "PRN"
                        )
                    )
                )
            )
        ),
        name = "Hausarztpraxis Dr. Topp-Glücklich",
        telecoms = listOf(
            FhirTelecom(system = "phone", value = "0301234567")
        ),
        addresses = listOf(
            FhirAddress(
                type = "both",
                line = listOf("Musterstr. 2"),
                city = "Berlin",
                postalCode = "10623",
                country = null,
                extractedLine = mapOf(
                    "houseNumber" to "2",
                    "streetName" to "Musterstr."
                )
            )
        )
    )

    val fhirOrganization3_v120 = FhirOrganization(
        resourceType = "Organization",
        id = "2a555cd3-0543-483c-88b3-f68647620962",
        meta = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.2")
        ),
        identifiers = listOf(
            FhirIdentifier(
                system = "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR",
                value = "724444400",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            system = "http://terminology.hl7.org/CodeSystem/v2-0203",
                            code = "BSNR"
                        )
                    )
                )
            )
        ),
        name = "Hausarztpraxis",
        telecoms = listOf(
            FhirTelecom(system = "phone", value = "030321654987"),
            FhirTelecom(system = "email", value = "hausarztpraxis@e-mail.de")
        ),
        addresses = listOf(
            FhirAddress(
                type = "both",
                line = listOf("Herbert-Lewin-Platz 2", "Erdgeschoss"),
                city = "Berlin",
                postalCode = "10623",
                country = "D",
                extractedLine = mapOf(
                    "houseNumber" to "2",
                    "streetName" to "Herbert-Lewin-Platz"
                )
            )
        )
    )

    val fhirOrganization4_v120 = FhirOrganization(
        resourceType = "Organization",
        id = "2a555cd3-0543-483c-88b3-f68647620962",
        meta = FhirMeta(
            profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.2")
        ),
        identifiers = listOf(
            FhirIdentifier(
                system = "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR",
                value = "724444400",
                type = FhirCodeableConcept(
                    coding = listOf(
                        FhirCoding(
                            system = "http://terminology.hl7.org/CodeSystem/v2-0203",
                            code = "BSNR"
                        )
                    )
                )
            )
        ),
        name = "Hausarztpraxis",
        telecoms = listOf(
            FhirTelecom(system = "phone", value = "030321654987"),
            FhirTelecom(system = "email", value = "hausarztpraxis@e-mail.de")
        ),
        addresses = listOf(
            FhirAddress(
                type = "both",
                line = listOf("Herbert-Lewin-Platz 2", "Erdgeschoss"),
                city = "Berlin",
                postalCode = "10623",
                country = "D",
                extractedLine = mapOf(
                    "houseNumber" to "2",
                    "streetName" to "Herbert-Lewin-Platz",
                    "additionalLocator" to "Erdgeschoss"
                )
            )
        )
    )
}
