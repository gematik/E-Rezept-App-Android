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

import de.gematik.ti.erp.app.fhir.model.TaskStatus
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskMetaDataErpModel
import de.gematik.ti.erp.app.utils.FhirTemporal
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

object FhirTaskMetaDataErpTestData {
    val taskMetaDataVersion_1_2 = FhirTaskMetaDataErpModel(
        taskId = "160.000.033.491.280.78",
        accessCode = "777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea",
        lastModified = FhirTemporal.Instant(Instant.parse("2022-03-18T15:29:00Z")),
        expiresOn = FhirTemporal.LocalDate(LocalDate.parse("2022-06-02")),
        acceptUntil = FhirTemporal.LocalDate(LocalDate.parse("2022-04-02")),
        authoredOn = FhirTemporal.Instant(Instant.parse("2022-03-18T15:26:00Z")),
        status = TaskStatus.Completed,
        lastMedicationDispense = null
    )

    val taskMetaDataVersion_1_3 = FhirTaskMetaDataErpModel(
        taskId = "160.123.456.789.123.61",
        accessCode = "777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607bl",
        lastModified = FhirTemporal.Instant(Instant.parse("2020-03-02T08:45:05Z")),
        expiresOn = FhirTemporal.LocalDate(LocalDate.parse("2020-06-02")),
        acceptUntil = FhirTemporal.LocalDate(LocalDate.parse("2020-04-01")),
        authoredOn = FhirTemporal.Instant(Instant.parse("2020-03-02T08:25:05Z")),
        status = TaskStatus.InProgress,
        lastMedicationDispense = FhirTemporal.Instant(Instant.parse("2020-04-01T15:37:17Z"))
    )
}
