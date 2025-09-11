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

import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvPatientErpModel
import de.gematik.ti.erp.app.fhir.support.FhirTaskKbvAddressErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporalSerializationType
import de.gematik.ti.erp.app.fhir.temporal.Year
import kotlinx.datetime.LocalDate

object FhirPatientErpTestData {
    val erpPatient1_v103 = FhirTaskKbvPatientErpModel(
        name = "Prinzessin Lars Graf Freiherr von Schinder",
        birthDate = FhirTemporal.LocalDate(
            value = LocalDate.parse("1964-04-04"),
            type = FhirTemporalSerializationType.FhirTemporalLocalDate
        ),
        address = FhirTaskKbvAddressErpModel(
            streetName = "Siegburger Str.",
            houseNumber = "155",
            postalCode = "51105",
            city = "Köln"
        ),
        insuranceInformation = "X110535541"
    )

    val erpPatient2_v103 = FhirTaskKbvPatientErpModel(
        name = "Ludger Königsstein",
        birthDate = FhirTemporal.LocalDate(
            value = LocalDate.parse("1935-06-22"),
            type = FhirTemporalSerializationType.FhirTemporalLocalDate
        ),
        address = FhirTaskKbvAddressErpModel(
            streetName = "Musterstr.",
            houseNumber = "1",
            postalCode = "10623",
            city = "Berlin"
        ),
        insuranceInformation = "X234567890"
    )

    val erpPatient1IncompleteBirth_v103 = FhirTaskKbvPatientErpModel(
        name = "Prinzessin Lars Graf Freiherr von Schinder",
        birthDate = FhirTemporal.Year(
            value = Year.parse("1964"),
            type = FhirTemporalSerializationType.FhirTemporalYear
        ),
        address = FhirTaskKbvAddressErpModel(
            streetName = "Siegburger Str.",
            houseNumber = "155",
            postalCode = "51105",
            city = "Köln"
        ),
        insuranceInformation = "X110535541"
    )

    val erpPatient1_v110 = FhirTaskKbvPatientErpModel(
        name = "Ludger Königsstein",
        birthDate = FhirTemporal.LocalDate(
            value = LocalDate.parse("1935-06-22"),
            type = FhirTemporalSerializationType.FhirTemporalLocalDate
        ),
        address = FhirTaskKbvAddressErpModel(
            streetName = "Blumenweg",
            houseNumber = null,
            postalCode = "26427",
            city = "Esens"
        ),
        insuranceInformation = "K220635158"
    )

    val erpPatient2_v110 = FhirTaskKbvPatientErpModel(
        name = "Ludger Königsstein",
        birthDate = FhirTemporal.LocalDate(
            value = LocalDate.parse("1935-06-22"),
            type = FhirTemporalSerializationType.FhirTemporalLocalDate
        ),
        address = FhirTaskKbvAddressErpModel(
            streetName = "Musterstr.",
            houseNumber = "1",
            postalCode = "10623",
            city = "Berlin"
        ),
        insuranceInformation = "X234567890"
    )

    val erpPatient3_v110 = FhirTaskKbvPatientErpModel(
        name = "Paula Privati",
        birthDate = FhirTemporal.LocalDate(
            value = LocalDate.parse("1935-06-22"),
            type = FhirTemporalSerializationType.FhirTemporalLocalDate
        ),
        address = FhirTaskKbvAddressErpModel(
            streetName = "Blumenweg",
            houseNumber = null,
            postalCode = "26427",
            city = "Esens"
        ),
        insuranceInformation = "X110411675"
    )

    val erpPatient4_v12 = FhirTaskKbvPatientErpModel(
        name = "Prof. Dr. Dr. med Eva Kluge",
        birthDate = FhirTemporal.LocalDate(
            value = LocalDate.parse("1982-01-03")
        ),
        address = FhirTaskKbvAddressErpModel(
            streetName = "Pflasterhofweg",
            houseNumber = "111B",
            additionalAddressInformation = null,
            postalCode = "50999",
            city = "Köln"
        ),
        insuranceInformation = "K030182229"
    )

    val erpPatient5_v12 = FhirTaskKbvPatientErpModel(
        name = "Ingrid Erbprinzessin von und zu der Schimmelpfennig-Hammerschmidt Federmannssohn",
        birthDate = FhirTemporal.LocalDate(
            value = LocalDate.parse("2010-01-31")
        ),
        address = FhirTaskKbvAddressErpModel(
            streetName = "Anneliese- und Georg-von-Groscurth-Plaetzchen",
            houseNumber = "149-C",
            additionalAddressInformation = "5. OG - Hinterhof",
            postalCode = "60437",
            city = "Bad Homburg"
        ),
        insuranceInformation = "M310119802"
    )
}
