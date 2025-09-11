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

package de.gematik.ti.erp.app.debugsettings.logger.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirContactInformationErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPharmacyAddressErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPharmacyErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPharmacyErpModel.Companion.toJson
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPositionErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirVzdSpecialtyType.Delivery
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirVzdSpecialtyType.Pickup
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirVzdSpecialtyType.Shipment
import de.gematik.ti.erp.app.fhir.pharmacy.model.OpeningHoursErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.OpeningTimeErpModel
import de.gematik.ti.erp.app.logger.model.DbMigrationLogEntry
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime

data class DbMigrationLoggerScreenPreviewData(
    val name: String,
    val dbMigrationLogEntries: UiState<List<DbMigrationLogEntry>>,
    val expandedListItems: Set<String>,
    val searchValue: String
)

private val openingTimeAErpModel = OpeningTimeErpModel(LocalTime.parse("08:00:00"), LocalTime.parse("12:00:00"))
private val openingTimeBErpModel = OpeningTimeErpModel(LocalTime.parse("14:00:00"), LocalTime.parse("18:00:00"))
private val openingTimeCErpModel = OpeningTimeErpModel(LocalTime.parse("08:00:00"), LocalTime.parse("20:00:00"))

class DbMigrationLoggerScreenPreviewParameterProvider : PreviewParameterProvider<DbMigrationLoggerScreenPreviewData> {
    override val values = sequenceOf(
        DbMigrationLoggerScreenPreviewData(
            name = "Loading",
            dbMigrationLogEntries = UiState.Loading(),
            expandedListItems = emptySet(),
            searchValue = ""
        ),
        DbMigrationLoggerScreenPreviewData(
            name = "Empty",
            dbMigrationLogEntries = UiState.Empty(),
            expandedListItems = emptySet(),
            searchValue = ""
        ),
        DbMigrationLoggerScreenPreviewData(
            name = "Error",
            dbMigrationLogEntries = UiState.Error(Throwable()),
            expandedListItems = emptySet(),
            searchValue = ""
        ),
        DbMigrationLoggerScreenPreviewData(
            name = "Data not Expanded",
            dbMigrationLogEntries = UiState.Data(
                listOf(
                    DbMigrationLogEntry(
                        id = "1",
                        timestamp = Instant.DISTANT_PAST.toString(),
                        name = "Pharmacy",
                        v1 = FhirPharmacyErpModel(
                            id = "4b74c2b2-2275-4153-a94d-3ddc6bfb1362",
                            name = "Heide-Apotheke",
                            telematikId = "3-05.2.1007600000.080",
                            position = FhirPositionErpModel(
                                latitude = 8.597412,
                                longitude = 53.590027
                            ),
                            address = FhirPharmacyAddressErpModel(
                                lineAddress = "",
                                postalCode = "27578",
                                city = "Bremerhaven"
                            ),
                            contact = FhirContactInformationErpModel(
                                phone = "0471/87029",
                                mail = "info@heide-apotheke-bremerhaven.de",
                                url = "http://www.heide-apotheke-bremerhaven.de"
                            ),
                            specialities = listOf(Delivery, Pickup, Shipment),
                            hoursOfOperation = OpeningHoursErpModel(
                                openingTime = mapOf(
                                    DayOfWeek.MONDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.TUESDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.WEDNESDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.THURSDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.FRIDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.SATURDAY to listOf(openingTimeAErpModel)
                                )
                            ),
                            availableTime = OpeningHoursErpModel(
                                openingTime = mapOf(
                                    DayOfWeek.MONDAY to listOf(openingTimeCErpModel),
                                    DayOfWeek.TUESDAY to listOf(openingTimeCErpModel),
                                    DayOfWeek.WEDNESDAY to listOf(openingTimeCErpModel),
                                    DayOfWeek.THURSDAY to listOf(openingTimeCErpModel),
                                    DayOfWeek.FRIDAY to listOf(openingTimeCErpModel)
                                )
                            )
                        ).toString(),
                        v2 = FhirPharmacyErpModel(
                            id = "4b74c2b2-2275-4153-a94d-3ddc6bfb1362",
                            name = "Heide-Apotheke",
                            telematikId = "3-05.2.1007600000.080",
                            position = FhirPositionErpModel(
                                latitude = 8.597412,
                                longitude = 53.590027
                            ),
                            address = FhirPharmacyAddressErpModel(
                                lineAddress = "",
                                postalCode = "27578",
                                city = "Bremerhaven"
                            ),
                            contact = FhirContactInformationErpModel(
                                phone = "0471/87029",
                                mail = "info@heide-apotheke-bremerhaven.de",
                                url = "http://www.heide-apotheke-bremerhaven.de"
                            ),
                            specialities = listOf(Delivery, Pickup, Shipment),
                            hoursOfOperation = OpeningHoursErpModel(
                                openingTime = mapOf(
                                    DayOfWeek.MONDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.TUESDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.WEDNESDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.THURSDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.FRIDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.SATURDAY to listOf(openingTimeAErpModel)
                                )
                            ),
                            availableTime = OpeningHoursErpModel(
                                openingTime = mapOf(
                                    DayOfWeek.MONDAY to listOf(openingTimeCErpModel),
                                    DayOfWeek.TUESDAY to listOf(openingTimeCErpModel),
                                    DayOfWeek.WEDNESDAY to listOf(openingTimeCErpModel),
                                    DayOfWeek.THURSDAY to listOf(openingTimeCErpModel),
                                    DayOfWeek.FRIDAY to listOf(openingTimeCErpModel)
                                )
                            )
                        ).toString()
                    ),
                    DbMigrationLogEntry(
                        id = "2",
                        timestamp = Instant.DISTANT_PAST.toString(),
                        name = "Settings",
                        v1 = "1",
                        v2 = ""
                    )
                )
            ),
            expandedListItems = emptySet(),
            searchValue = ""
        ),
        DbMigrationLoggerScreenPreviewData(
            name = "Data not Expanded",
            dbMigrationLogEntries = UiState.Data(
                listOf(
                    DbMigrationLogEntry(
                        id = "1",
                        timestamp = Instant.DISTANT_PAST.toString(),
                        name = "Pharmacy",
                        v1 = FhirPharmacyErpModel(
                            id = "4b74c2b2-2275-4153-a94d-3ddc6bfb1362",
                            name = "Heide-Apotheke",
                            telematikId = "3-05.2.1007600000.080",
                            position = FhirPositionErpModel(
                                latitude = 8.597412,
                                longitude = 53.590027
                            ),
                            address = FhirPharmacyAddressErpModel(
                                lineAddress = "",
                                postalCode = "27578",
                                city = "Bremerhaven"
                            ),
                            contact = FhirContactInformationErpModel(
                                phone = "0471/87029",
                                mail = "info@heide-apotheke-bremerhaven.de",
                                url = "http://www.heide-apotheke-bremerhaven.de"
                            ),
                            specialities = listOf(Delivery, Pickup, Shipment),
                            hoursOfOperation = OpeningHoursErpModel(
                                openingTime = mapOf(
                                    DayOfWeek.MONDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.TUESDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.WEDNESDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.THURSDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.FRIDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.SATURDAY to listOf(openingTimeAErpModel)
                                )
                            ),
                            availableTime = OpeningHoursErpModel(
                                openingTime = mapOf(
                                    DayOfWeek.MONDAY to listOf(openingTimeCErpModel),
                                    DayOfWeek.TUESDAY to listOf(openingTimeCErpModel),
                                    DayOfWeek.WEDNESDAY to listOf(openingTimeCErpModel),
                                    DayOfWeek.THURSDAY to listOf(openingTimeCErpModel),
                                    DayOfWeek.FRIDAY to listOf(openingTimeCErpModel)
                                )
                            )
                        ).toJson(),
                        v2 = FhirPharmacyErpModel(
                            id = "4b74c2b2-2275-4153-a94d-3ddc6bfb1362",
                            name = "Heide-Apotheke",
                            telematikId = "3-05.2.1007600000.080",
                            position = FhirPositionErpModel(
                                latitude = 8.597412,
                                longitude = 53.590027
                            ),
                            address = FhirPharmacyAddressErpModel(
                                lineAddress = "",
                                postalCode = "27578",
                                city = "Bremerhaven"
                            ),
                            contact = FhirContactInformationErpModel(
                                phone = "0471/87029",
                                mail = "info@heide-apotheke-bremerhaven.de",
                                url = "http://www.heide-apotheke-bremerhaven.de"
                            ),
                            specialities = listOf(Delivery, Pickup, Shipment),
                            hoursOfOperation = OpeningHoursErpModel(
                                openingTime = mapOf(
                                    DayOfWeek.MONDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.TUESDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.WEDNESDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.THURSDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.FRIDAY to listOf(openingTimeAErpModel, openingTimeBErpModel),
                                    DayOfWeek.SATURDAY to listOf(openingTimeAErpModel)
                                )
                            ),
                            availableTime = OpeningHoursErpModel(
                                openingTime = mapOf(
                                    DayOfWeek.MONDAY to listOf(openingTimeCErpModel),
                                    DayOfWeek.TUESDAY to listOf(openingTimeCErpModel),
                                    DayOfWeek.WEDNESDAY to listOf(openingTimeCErpModel),
                                    DayOfWeek.THURSDAY to listOf(openingTimeCErpModel),
                                    DayOfWeek.FRIDAY to listOf(openingTimeCErpModel)
                                )
                            )
                        ).toJson()
                    ),
                    DbMigrationLogEntry(
                        id = "2",
                        timestamp = Instant.DISTANT_PAST.toString(),
                        name = "Settings",
                        v1 = "1",
                        v2 = ""
                    )
                )
            ),
            expandedListItems = setOf("1"),
            searchValue = ""
        )
    )
}
