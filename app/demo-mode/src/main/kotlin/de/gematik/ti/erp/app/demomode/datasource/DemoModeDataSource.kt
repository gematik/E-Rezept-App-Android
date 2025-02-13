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

package de.gematik.ti.erp.app.demomode.datasource

import de.gematik.ti.erp.app.db.entities.v1.InAppMessageEntity
import de.gematik.ti.erp.app.demomode.datasource.data.DemoAuditEventInfo
import de.gematik.ti.erp.app.demomode.datasource.data.DemoPharmacyInfo.demoFavouritePharmacy
import de.gematik.ti.erp.app.demomode.datasource.data.DemoPrescriptionInfo.DemoScannedPrescription.demoScannedTask01
import de.gematik.ti.erp.app.demomode.datasource.data.DemoPrescriptionInfo.DemoScannedPrescription.demoScannedTask02
import de.gematik.ti.erp.app.demomode.datasource.data.DemoPrescriptionInfo.DemoSyncedPrescription.syncedTask
import de.gematik.ti.erp.app.demomode.datasource.data.DemoProfileInfo.demoProfile01
import de.gematik.ti.erp.app.demomode.datasource.data.DemoProfileInfo.demoProfile02
import de.gematik.ti.erp.app.demomode.datasource.data.inAppMessageEntity
import de.gematik.ti.erp.app.demomode.model.DemoModeProfile
import de.gematik.ti.erp.app.demomode.model.DemoModeProfileLinkedCommunication
import de.gematik.ti.erp.app.idp.api.models.PairingData
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntry
import de.gematik.ti.erp.app.messages.repository.CachedPharmacy
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.prescription.model.CommunicationProfile.ErxCommunicationDispReq
import de.gematik.ti.erp.app.prescription.model.CommunicationProfile.ErxCommunicationReply
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.protocol.model.AuditEventData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

const val INDEX_OUT_OF_BOUNDS = -1

class DemoModeDataSource {

    /**
     * Data sources for the [profiles] created in the demo-mode
     */
    val profiles: MutableStateFlow<MutableList<DemoModeProfile>> =
        MutableStateFlow(mutableListOf(demoProfile01, demoProfile02))

    private val syncedTasksList = listOf(
        syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Completed, index = 1),
        syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Completed, index = 2),
        syncedTask(
            demoProfile01.id,
            status = SyncedTaskData.TaskStatus.Ready,
            index = 3,
            isDirectAssignment = true
        ),
        syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Canceled, index = 4),
        syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Ready, index = 5),
        syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Ready, index = 6),
        syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Ready, index = 7),
        syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Ready, index = 8),
        syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Ready, index = 9),
        syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Ready, index = 10),
        syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Ready, index = 11),
        syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Ready, index = 12),
        syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Ready, index = 13),

        syncedTask(demoProfile02.id, status = SyncedTaskData.TaskStatus.Ready, index = 14),
        syncedTask(demoProfile02.id, status = SyncedTaskData.TaskStatus.Ready, index = 15),
        syncedTask(demoProfile02.id, status = SyncedTaskData.TaskStatus.Ready, index = 16),
        syncedTask(demoProfile02.id, status = SyncedTaskData.TaskStatus.Ready, index = 17),
        syncedTask(demoProfile02.id, status = SyncedTaskData.TaskStatus.Ready, index = 18),
        syncedTask(demoProfile02.id, status = SyncedTaskData.TaskStatus.Ready, index = 19),
        syncedTask(demoProfile02.id, status = SyncedTaskData.TaskStatus.Completed, index = 20),
        syncedTask(demoProfile02.id, status = SyncedTaskData.TaskStatus.Completed, index = 21),
        syncedTask(
            demoProfile02.id,
            status = SyncedTaskData.TaskStatus.Completed,
            index = 22,
            isDirectAssignment = true
        )
    )

    /**
     * Data sources for the [syncedTasks] created in the demo-mode
     */
    val syncedTasks: MutableStateFlow<MutableList<SyncedTaskData.SyncedTask>> =
        MutableStateFlow(syncedTasksList.toMutableList())

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
        MutableStateFlow(mutableListOf())

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
     * Data sources for the [requestCommunication] created in the demo-mode,
     * this is used as the source for communication between the user, pharmacy and the doctor
     */
    val communications: MutableStateFlow<MutableList<DemoModeProfileLinkedCommunication>> =
        MutableStateFlow(mutableListOf())

    val inAppMessages: MutableStateFlow<MutableList<InAppMessageEntity>> =
        MutableStateFlow(mutableListOf(inAppMessageEntity))

    val counter: MutableStateFlow<Long> =
        MutableStateFlow(0)

    val lastVersion: MutableStateFlow<String> =
        MutableStateFlow("demo.version")

    val lastUpdatedVersion: MutableStateFlow<String> =
        MutableStateFlow("demo.version")

    val showWelcomeMessage: MutableStateFlow<Boolean> =
        MutableStateFlow(false)

    val welcomeMessageTimeStamp: MutableStateFlow<Instant> =
        MutableStateFlow(Clock.System.now().minus(12.hours))

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

    companion object {
        val communicationPayload: String = """
     {
        "version":1 , 
        "supplyOptionsType":"onPremise" , 
        "info_text":"Beispieltext für die Kommunikation zwischen Patient und Apotheke" 
        "pickUpCodeHR":"1234567890" , 
        "pickUpCodeDMC":"0123456789" , 
        "url":"https://www.gematik.de/"
        }
        """.trimIndent()

        fun replyCommunications(
            profileId: String,
            taskId: String,
            communicationId: String,
            pharmacyId: String,
            orderId: String,
            consumed: Boolean = false
        ) = listOf(
            // T-01
            DemoModeProfileLinkedCommunication(
                profileId = profileId,
                taskId = taskId,
                communicationId = communicationId,
                sentOn = Clock.System.now().minus(3.days).minus(2.hours),
                sender = pharmacyId,
                consumed = consumed,
                profile = ErxCommunicationReply,
                // these values are kept empty while saving them
                orderId = orderId,
                payload = """
                        {
                        "version":1 , 
                        "supplyOptionsType":"onPremise" , 
                        "info_text":"Eine Beispielnachricht, wie eine Nachricht aus der Apotheke aussieht" , 
                        "pickUpCodeHR":"T01" , 
                        "pickUpCodeDMC":"DMC01" , 
                        "url":"https://github.com/gematik/E-Rezept-App-Android"
                        }
                """.trimIndent(),
                recipient = "Erika Mustermann"
            ),
            // T-02
            DemoModeProfileLinkedCommunication(
                profileId = profileId,
                taskId = taskId,
                communicationId = communicationId,
                sentOn = Clock.System.now().minus(4.days).minus(3.hours),
                sender = pharmacyId,
                consumed = consumed,
                profile = ErxCommunicationReply,
                // these values are kept empty while saving them
                orderId = orderId,
                payload = """
                        {
                        "version":1 , 
                        "supplyOptionsType":"onPremise" , 
                        "info_text":"" , 
                        "pickUpCodeHR":"", 
                        "pickUpCodeDMC":"" 
                        }
                """.trimIndent(),
                recipient = "Max Mustermann"
            ),
            // T-03
            DemoModeProfileLinkedCommunication(
                profileId = profileId,
                taskId = taskId,
                communicationId = communicationId,
                sentOn = Clock.System.now().minus(5.days),
                sender = pharmacyId,
                consumed = consumed,
                profile = ErxCommunicationReply,
                orderId = orderId,
                payload = """
                        {
                        "version":1 , 
                        "supplyOptionsType":"onPremise" , 
                        "pickUpCodeHR":"T03", 
                        "pickUpCodeDMC":"" 
                        }
                """.trimIndent(),
                recipient = "Mustermann"
            )
        )

        fun requestCommunication(
            profileId: String,
            taskId: String,
            communicationId: String,
            pharmacyId: String,
            consumed: Boolean = false
        ): DemoModeProfileLinkedCommunication {
            val orderId = UUID.randomUUID().toString()
            return DemoModeProfileLinkedCommunication(
                profileId = profileId,
                taskId = taskId,
                communicationId = communicationId,
                sentOn = Clock.System.now().minus(2.days).minus(1.hours),
                sender = pharmacyId,
                consumed = consumed,
                profile = ErxCommunicationDispReq,
                // these values are kept empty while saving them
                orderId = orderId,
                payload = "",
                recipient = "Max Mustermann"
            )
        }
    }
}
