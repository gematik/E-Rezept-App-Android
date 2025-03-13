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

import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvPractitionerErpModel

object FhirPractitionerErpTestData {
    val erpPractitioner103 = FhirTaskKbvPractitionerErpModel(
        name = "Dr. med. Emma Schneider",
        qualification = "Fachärztin für Innere Medizin",
        practitionerIdentifier = "987654423"
    )

    val erpPractitioner110 = FhirTaskKbvPractitionerErpModel(
        name = "Alexander Fischer",
        qualification = "Facharzt für Innere Medizin",
        practitionerIdentifier = null
    )
}
