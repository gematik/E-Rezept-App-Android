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

import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirExtensionReduced
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirName
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirNameFamilyExtension
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirPractitioner
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirQualificationCode
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirQualificationText

object FhirPractitionerTestData {
    val fhirPractitioner103 = FhirPractitioner(
        qualifications = listOf(
            FhirQualificationCode(code = FhirQualificationText(text = null)),
            FhirQualificationCode(code = FhirQualificationText(text = "Fachärztin für Innere Medizin"))
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

    val fhirPractitioner110 = FhirPractitioner(
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
}
