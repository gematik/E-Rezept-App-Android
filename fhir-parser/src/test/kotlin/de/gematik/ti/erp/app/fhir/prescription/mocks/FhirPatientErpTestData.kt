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

import de.gematik.ti.erp.app.fhir.common.model.erp.support.FhirTaskKbvAddressErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvPatientErpModel
import de.gematik.ti.erp.app.utils.FhirTemporal.LocalDate
import de.gematik.ti.erp.app.utils.FhirTemporal.Year
import de.gematik.ti.erp.app.utils.FhirTemporalSerializationType.FhirTemporalLocalDate
import de.gematik.ti.erp.app.utils.FhirTemporalSerializationType.FhirTemporalYear

object FhirPatientErpTestData {
    val erpPatient103 = FhirTaskKbvPatientErpModel(
        name = "Prinzessin Lars Graf Freiherr von Schinder",
        birthDate = LocalDate(
            value = kotlinx.datetime.LocalDate.parse("1964-04-04"),
            type = FhirTemporalLocalDate
        ),
        address = FhirTaskKbvAddressErpModel(
            streetName = "Siegburger Str.",
            houseNumber = "155",
            postalCode = "51105",
            city = "Köln"
        ),
        insuranceInformation = "X110535541"
    )

    val erpPatient103IncompleteBirth = FhirTaskKbvPatientErpModel(
        name = "Prinzessin Lars Graf Freiherr von Schinder",
        birthDate = Year(
            value = de.gematik.ti.erp.app.fhir.parser.Year.parse("1964"),
            type = FhirTemporalYear
        ),
        address = FhirTaskKbvAddressErpModel(
            streetName = "Siegburger Str.",
            houseNumber = "155",
            postalCode = "51105",
            city = "Köln"
        ),
        insuranceInformation = "X110535541"
    )

    val erpPatient110 = FhirTaskKbvPatientErpModel(
        name = "Ludger Königsstein",
        birthDate = LocalDate(
            value = kotlinx.datetime.LocalDate.parse("1935-06-22"),
            type = FhirTemporalLocalDate
        ),
        address = FhirTaskKbvAddressErpModel(
            streetName = "Blumenweg",
            houseNumber = null,
            postalCode = "26427",
            city = "Esens"
        ),
        insuranceInformation = "K220635158"
    )
}
