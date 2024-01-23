/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.demomode.datasource

import de.gematik.ti.erp.app.demomode.datasource.data.DemoAuditEventInfo
import de.gematik.ti.erp.app.demomode.datasource.data.DemoPharmacyInfo.demoFavouritePharmacy
import de.gematik.ti.erp.app.demomode.datasource.data.DemoPrescriptionInfo.DemoScannedPrescription.demoScannedTask01
import de.gematik.ti.erp.app.demomode.datasource.data.DemoPrescriptionInfo.DemoScannedPrescription.demoScannedTask02
import de.gematik.ti.erp.app.demomode.datasource.data.DemoPrescriptionInfo.DemoSyncedPrescription.syncedTask
import de.gematik.ti.erp.app.demomode.datasource.data.DemoProfileInfo.demoProfile01
import de.gematik.ti.erp.app.demomode.datasource.data.DemoProfileInfo.demoProfile02
import de.gematik.ti.erp.app.demomode.model.DemoModeProfile
import de.gematik.ti.erp.app.demomode.model.DemoModeProfileLinkedCommunication
import de.gematik.ti.erp.app.idp.api.models.PairingData
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntry
import de.gematik.ti.erp.app.orders.repository.CachedPharmacy
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.protocol.model.AuditEventData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

const val INDEX_OUT_OF_BOUNDS = -1

class DemoModeDataSource {

    /**
     * Data sources for the [profiles] created in the demo-mode
     */
    val profiles: MutableStateFlow<MutableList<DemoModeProfile>> =
        MutableStateFlow(mutableListOf(demoProfile01, demoProfile02))

    /**
     * Data sources for the [syncedTasks] created in the demo-mode
     */
    val syncedTasks: MutableStateFlow<MutableList<SyncedTaskData.SyncedTask>> =
        MutableStateFlow(
            mutableListOf(
                syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Ready, index = 1),
                syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Completed, index = 2),
                syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.InProgress, index = 3),
                syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Canceled, index = 4),
                syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Ready, index = 5),
                syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Ready, index = 6),
                syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Ready, index = 7),
                syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Ready, index = 8),

                syncedTask(demoProfile02.id, status = SyncedTaskData.TaskStatus.Ready, index = 9),
                syncedTask(demoProfile02.id, status = SyncedTaskData.TaskStatus.Completed, index = 10),
                syncedTask(demoProfile02.id, status = SyncedTaskData.TaskStatus.Completed, index = 11),
                syncedTask(demoProfile02.id, status = SyncedTaskData.TaskStatus.InProgress, index = 12)
            )
        )

    /**
     * Data sources for the [scannedTasks] created in the demo-mode
     */
    val scannedTasks: MutableStateFlow<MutableList<ScannedTaskData.ScannedTask>> =
        MutableStateFlow(mutableListOf(demoScannedTask01, demoScannedTask02))

    /**
     * Data sources for the [favoritePharmacies] created in the demo-mode
     */
    val favoritePharmacies: MutableStateFlow<MutableList<OverviewPharmacyData.OverviewPharmacy>> =
        MutableStateFlow(mutableListOf(demoFavouritePharmacy))

    /**
     * Data sources for the [oftenUsedPharmacies] created in the demo-mode
     */
    val oftenUsedPharmacies: MutableStateFlow<MutableList<OverviewPharmacyData.OverviewPharmacy>> =
        MutableStateFlow(mutableListOf<OverviewPharmacyData.OverviewPharmacy>())

    /**
     * Data sources for the [auditEvents] created in the demo-mode
     */
    val auditEvents: MutableStateFlow<MutableList<AuditEventData.AuditEvent>> =
        MutableStateFlow(
            mutableListOf(
                DemoAuditEventInfo.downloadDispense(),
                DemoAuditEventInfo.downloadPrescription(),
                DemoAuditEventInfo.downloadDispense(),
                DemoAuditEventInfo.downloadDispense(),
                DemoAuditEventInfo.downloadDispense(),
                DemoAuditEventInfo.downloadPrescription(),
                DemoAuditEventInfo.downloadDispense(),
                DemoAuditEventInfo.downloadPrescription(),
                DemoAuditEventInfo.downloadDispense(),
                DemoAuditEventInfo.downloadPrescription(),
                DemoAuditEventInfo.downloadDispense(),
                DemoAuditEventInfo.downloadPrescription()
            )
        )

    /**
     * Data sources for the [communications] created in the demo-mode,
     * this is used as the source for communication between the user, pharmacy and the doctor
     */
    val communications: MutableStateFlow<MutableList<DemoModeProfileLinkedCommunication>> =
        MutableStateFlow(mutableListOf<DemoModeProfileLinkedCommunication>())

    /**
     * Data source for the a [profileCommunicationLog] communication log that a particular profile has downloaded the information
     */
    val profileCommunicationLog: MutableStateFlow<MutableMap<String, Boolean>> =
        MutableStateFlow(mutableMapOf("no-profile-id" to false))

    /**
     * Data source for the [cachedPharmacies] used for communications
     */
    val cachedPharmacies: MutableStateFlow<MutableList<CachedPharmacy>> =
        MutableStateFlow(mutableListOf())

    /**
     * Data source for the connected device [pairedDevices] that will be shown to the user
     */
    val pairedDevices: MutableStateFlow<MutableList<Pair<PairingResponseEntry, PairingData>>> =
        MutableStateFlow(
            mutableListOf(
                PairingResponseEntry(
                    name = "Pixel 10",
                    creationTime = Clock.System.now().minus(10.days).toEpochMilliseconds(),
                    signedPairingData = "pairing.data"
                ) to
                    PairingData(
                        subjectPublicKeyInfoOfSecureElement = "subjectPublicKeyInfoOfSecureElement",
                        keyAliasOfSecureElement = "keyAliasOfSecureElement",
                        productName = "productName",
                        serialNumberOfHealthCard = "serialNumberOfHealthCard",
                        issuerOfHealthCard = "issuerOfHealthCard",
                        subjectPublicKeyInfoOfHealthCard = "subjectPublicKeyInfoOfHealthCard",
                        validityUntilOfHealthCard = Clock.System.now().plus(365.days).toEpochMilliseconds()
                    )
            )
        )
}
