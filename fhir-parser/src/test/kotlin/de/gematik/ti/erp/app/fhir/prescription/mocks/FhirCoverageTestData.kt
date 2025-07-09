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

import de.gematik.ti.erp.app.fhir.common.model.original.FhirCoding
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirCoverageExtension
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirCoverageModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirCoverageType
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirPayer

internal object FhirCoverageTestData {
    val fhirCoverage1_v103 = FhirCoverageModel(
        meta = FhirMeta(profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3")),
        extensions = listOf(
            FhirCoverageExtension(
                url = "http://fhir.de/StructureDefinition/gkv/besondere-personengruppe",
                coding = FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PERSONENGRUPPE", code = "00")
            ),
            FhirCoverageExtension(
                url = "http://fhir.de/StructureDefinition/gkv/dmp-kennzeichen",
                coding = FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DMP", code = "00")
            ),
            FhirCoverageExtension(
                url = "http://fhir.de/StructureDefinition/gkv/wop",
                coding = FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP", code = "38")
            ),
            FhirCoverageExtension(
                url = "http://fhir.de/StructureDefinition/gkv/versichertenart",
                coding = FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS", code = "3")
            )
        ),
        type = FhirCoverageType(
            coding = listOf(
                FhirCoding(system = "http://fhir.de/CodeSystem/versicherungsart-de-basis", code = "GKV")
            )
        ),
        payer = listOf(
            FhirPayer(
                name = "HEK",
                identifier = FhirIdentifier(system = "http://fhir.de/NamingSystem/arge-ik/iknr", value = "101570104")
            )
        )
    )

    val fhirCoverage2_v103 = FhirCoverageModel(
        meta = FhirMeta(profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3")),
        extensions = listOf(
            FhirCoverageExtension(
                url = "http://fhir.de/StructureDefinition/gkv/besondere-personengruppe",
                coding = FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PERSONENGRUPPE", code = "00")
            ),
            FhirCoverageExtension(
                url = "http://fhir.de/StructureDefinition/gkv/dmp-kennzeichen",
                coding = FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DMP", code = "00")
            ),
            FhirCoverageExtension(
                url = "http://fhir.de/StructureDefinition/gkv/wop",
                coding = FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP", code = "03")
            ),
            FhirCoverageExtension(
                url = "http://fhir.de/StructureDefinition/gkv/versichertenart",
                coding = FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS", code = "1")
            )
        ),
        type = FhirCoverageType(
            coding = listOf(
                FhirCoding(system = "http://fhir.de/CodeSystem/versicherungsart-de-basis", code = "GKV")
            )
        ),
        payer = listOf(
            FhirPayer(
                name = "AOK Rheinland/Hamburg",
                identifier = FhirIdentifier(system = "http://fhir.de/NamingSystem/arge-ik/iknr", value = "104212059")
            )
        )
    )

    val fhirCoverage1_v110 = FhirCoverageModel(
        meta = FhirMeta(profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.1.0")),
        extensions = listOf(
            FhirCoverageExtension(
                url = "http://fhir.de/StructureDefinition/gkv/besondere-personengruppe",
                coding = FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PERSONENGRUPPE", code = "00")
            ),
            FhirCoverageExtension(
                url = "http://fhir.de/StructureDefinition/gkv/dmp-kennzeichen",
                coding = FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DMP", code = "05")
            ),
            FhirCoverageExtension(
                url = "http://fhir.de/StructureDefinition/gkv/wop",
                coding = FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP", code = "17")
            ),
            FhirCoverageExtension(
                url = "http://fhir.de/StructureDefinition/gkv/versichertenart",
                coding = FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS", code = "5")
            )
        ),
        type = FhirCoverageType(
            coding = listOf(
                FhirCoding(system = "http://fhir.de/CodeSystem/versicherungsart-de-basis", code = "GKV")
            )
        ),
        payer = listOf(
            FhirPayer(
                name = "AOK Nordost",
                identifier = FhirIdentifier(system = "http://fhir.de/sid/arge-ik/iknr", value = "109719018")
            )
        )
    )

    val fhirCoverage2_v110 = FhirCoverageModel(
        meta = FhirMeta(profiles = listOf("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.1.0")),
        extensions = listOf(
            FhirCoverageExtension(
                url = "http://fhir.de/StructureDefinition/gkv/besondere-personengruppe",
                coding = FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PERSONENGRUPPE", code = "00")
            ),
            FhirCoverageExtension(
                url = "http://fhir.de/StructureDefinition/gkv/dmp-kennzeichen",
                coding = FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DMP", code = "00")
            ),
            FhirCoverageExtension(
                url = "http://fhir.de/StructureDefinition/gkv/wop",
                coding = FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP", code = "03")
            ),
            FhirCoverageExtension(
                url = "http://fhir.de/StructureDefinition/gkv/versichertenart",
                coding = FhirCoding(system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS", code = "1")
            )
        ),
        type = FhirCoverageType(
            coding = listOf(
                FhirCoding(system = "http://fhir.de/CodeSystem/versicherungsart-de-basis", code = "GKV")
            )
        ),
        payer = listOf(
            FhirPayer(
                name = "AOK Rheinland/Hamburg",
                identifier = FhirIdentifier(system = "http://fhir.de/sid/arge-ik/iknr", value = "104212059")
            )
        )
    )
}
