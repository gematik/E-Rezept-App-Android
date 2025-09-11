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

import de.gematik.ti.erp.app.fhir.FhirTaskEntryParserResultErpModel
import de.gematik.ti.erp.app.fhir.support.FhirTaskEntryDataErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.task.model.TaskStatus
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
        selfPageUrl = "https://erp.app.ti-dienste.de/Task/",
        nextPageUrl = "https://erp.app.ti-dienste.de/Task/?_count=10&_offset=10",
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

    val taskEntryV_1_5 = FhirTaskEntryParserResultErpModel(
        bundleTotal = 6,
        taskEntries = listOf(
            FhirTaskEntryDataErpModel(
                id = "160.000.113.612.488.29",
                status = TaskStatus.InProgress,
                lastModified = FhirTemporal.Instant(
                    value = Instant.parse("2025-05-12T09:57:08.460Z")
                )
            ),
            FhirTaskEntryDataErpModel(
                id = "162.000.000.004.510.98",
                status = TaskStatus.Ready,
                lastModified = FhirTemporal.Instant(
                    value = Instant.parse("2025-05-21T12:19:41.851Z")
                )
            ),
            FhirTaskEntryDataErpModel(
                id = "162.000.000.005.408.23",
                status = TaskStatus.Ready,
                lastModified = FhirTemporal.Instant(
                    value = Instant.parse("2025-06-10T11:16:16.188Z")
                )
            ),
            FhirTaskEntryDataErpModel(
                id = "162.000.000.005.596.41",
                status = TaskStatus.Ready,
                lastModified = FhirTemporal.Instant(
                    value = Instant.parse("2025-06-13T12:16:05.871Z")
                )
            ),
            FhirTaskEntryDataErpModel(
                id = "160.000.114.336.581.65",
                status = TaskStatus.Ready,
                lastModified = FhirTemporal.Instant(
                    value = Instant.parse("2025-08-14T09:52:19.200Z")
                )
            ),
            FhirTaskEntryDataErpModel(
                id = "160.000.114.336.582.62",
                status = TaskStatus.Ready,
                lastModified = FhirTemporal.Instant(
                    value = Instant.parse("2025-08-14T09:52:22.386Z")
                )
            )
        ),
        firstPageUrl = "https://erp-dev.zentral.erp.splitdns.ti-dienste.de/Task?_sort=modified&_count=50&__offset=0",
        previousPageUrl = null,
        selfPageUrl = "https://erp-dev.zentral.erp.splitdns.ti-dienste.de/Task?_sort=modified",
        nextPageUrl = null
    )
}
