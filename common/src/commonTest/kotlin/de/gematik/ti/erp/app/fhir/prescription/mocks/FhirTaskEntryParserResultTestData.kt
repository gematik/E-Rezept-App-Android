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
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskEntryDataErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskEntryParserResultErpModel
import de.gematik.ti.erp.app.utils.FhirTemporal
import kotlinx.datetime.Instant

object FhirTaskEntryParserResultTestData {
    val taskEntryV_1_2 = FhirTaskEntryParserResultErpModel(
        bundleTotal = 1,
        taskEntries = listOf(
            FhirTaskEntryDataErpModel(
                id = "160.000.033.491.280.78",
                status = TaskStatus.Ready,
                lastModified = FhirTemporal.Instant(Instant.parse("2022-03-18T15:27:00Z"))
            )
        )
    )

    val taskEntryV_1_3 = FhirTaskEntryParserResultErpModel(
        bundleTotal = 3,
        taskEntries = listOf(
            FhirTaskEntryDataErpModel(
                id = "160.123.456.789.123.58",
                status = TaskStatus.Ready,
                lastModified = FhirTemporal.Instant(Instant.parse("2020-03-02T08:45:05Z"))
            ),
            FhirTaskEntryDataErpModel(
                id = "160.123.456.789.123.78",
                status = TaskStatus.Ready,
                lastModified = FhirTemporal.Instant(Instant.parse("2020-03-02T08:45:05Z"))
            ),
            FhirTaskEntryDataErpModel(
                id = "160.123.456.789.123.61",
                status = TaskStatus.InProgress,
                lastModified = FhirTemporal.Instant(Instant.parse("2020-03-02T08:45:05Z"))
            )
        )
    )
}
