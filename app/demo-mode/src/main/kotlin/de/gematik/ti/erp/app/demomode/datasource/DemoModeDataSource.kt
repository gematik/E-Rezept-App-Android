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

package de.gematik.ti.erp.app.demomode.datasource

import de.gematik.ti.erp.app.communication.model.CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq
import de.gematik.ti.erp.app.communication.model.CommunicationErpModel.CommunicationProfile.ErxCommunicationReply
import de.gematik.ti.erp.app.database.realm.v1.InternalMessageEntityV1
import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource.Companion.requestCommunication
import de.gematik.ti.erp.app.demomode.datasource.data.DemoAuditEventInfo
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.DIRECT_ASSIGNMENT_TASK_PRESET
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.SYNCED_TASK_PRESET
import de.gematik.ti.erp.app.demomode.datasource.data.DemoPharmacyInfo.demoFavouritePharmacy
import de.gematik.ti.erp.app.demomode.datasource.data.DemoPrescriptionInfo.DemoScannedPrescription.demoScannedTask01
import de.gematik.ti.erp.app.demomode.datasource.data.DemoPrescriptionInfo.DemoScannedPrescription.demoScannedTask02
import de.gematik.ti.erp.app.demomode.datasource.data.DemoPrescriptionInfo.DemoSyncedPrescription.syncedTask
import de.gematik.ti.erp.app.demomode.datasource.data.DemoProfileInfo.demoProfile01
import de.gematik.ti.erp.app.demomode.datasource.data.DemoProfileInfo.demoProfile02
import de.gematik.ti.erp.app.demomode.datasource.data.FunnyAppNameProvider
import de.gematik.ti.erp.app.demomode.datasource.data.internalMessageEntityV1
import de.gematik.ti.erp.app.demomode.model.DemoModeProfile
import de.gematik.ti.erp.app.demomode.model.DemoModeProfileLinkedCommunication
import de.gematik.ti.erp.app.eurezept.domain.model.Country
import de.gematik.ti.erp.app.eurezept.model.EuAccessCodeErpModel
import de.gematik.ti.erp.app.eurezept.model.EuOrderErpModel
import de.gematik.ti.erp.app.fhir.audit.model.FhirAuditEventErpModel
import de.gematik.ti.erp.app.idp.api.models.PairingData
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntry
import de.gematik.ti.erp.app.invoice.model.ChargeableItemDescriptionErpModel
import de.gematik.ti.erp.app.invoice.model.ChargeableItemErpModel
import de.gematik.ti.erp.app.invoice.model.InvoiceErpModel
import de.gematik.ti.erp.app.invoice.model.PKVInvoiceErpModel
import de.gematik.ti.erp.app.invoice.model.PriceComponentErpModel
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

const val INDEX_OUT_OF_BOUNDS = -1

class DemoModeDataSource(
    appNameProvider: FunnyAppNameProvider
) {

    /**
     * Data sources for the [profiles] created in the demo-mode
     */
    val profiles: MutableStateFlow<MutableList<DemoModeProfile>> =
        MutableStateFlow(mutableListOf(demoProfile01, demoProfile02))

    private val syncedTasksList = listOf(
        syncedTask(
            profileIdentifier = demoProfile01.id,
            status = TaskStatusEnum.Ready,
            medicationNamesIndex = 0,
            isEuRedeemable = true,
            isEuRedeemableByPatientAuthorization = true
        ),
        syncedTask(
            profileIdentifier = demoProfile01.id,
            status = TaskStatusEnum.Ready,
            medicationNamesIndex = 30,
            isEuRedeemable = true,
            isEuRedeemableByPatientAuthorization = true
        ),

        syncedTask(demoProfile01.id, status = TaskStatusEnum.Completed, medicationNamesIndex = 1),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.Completed, medicationNamesIndex = 2),

        syncedTask(
            demoProfile01.id,
            status = TaskStatusEnum.InProgress,
            isDirectAssignment = true,
            medicationNamesIndex = 3
        ),

        syncedTask(demoProfile01.id, status = TaskStatusEnum.Canceled, medicationNamesIndex = 4),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.Ready, medicationNamesIndex = 5),
        // dvg syncedTask(demoProfile01.id, status = SyncedTaskData.TaskStatus.Ready, medicationNamesIndex = 5, isTeratogenicPrescription = true),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.Ready, medicationNamesIndex = 6),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.Ready, medicationNamesIndex = 7),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.Ready, medicationNamesIndex = 8),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.Ready, medicationNamesIndex = 9),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.Ready, medicationNamesIndex = 10),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.Ready, medicationNamesIndex = 11),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.Ready, medicationNamesIndex = 12),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.Ready, medicationNamesIndex = 13),
        // dvg syncedTask(demoProfile02.id, status = SyncedTaskData.TaskStatus.Ready, medicationNamesIndex = 14, isTeratogenicPrescription = true),
        syncedTask(demoProfile02.id, status = TaskStatusEnum.Ready, medicationNamesIndex = 14),
        syncedTask(demoProfile02.id, status = TaskStatusEnum.Ready, medicationNamesIndex = 15),
        syncedTask(demoProfile02.id, status = TaskStatusEnum.Ready, medicationNamesIndex = 16),
        syncedTask(demoProfile02.id, status = TaskStatusEnum.Ready, medicationNamesIndex = 17),
        syncedTask(demoProfile02.id, status = TaskStatusEnum.Ready, medicationNamesIndex = 18),
        syncedTask(demoProfile02.id, status = TaskStatusEnum.Ready, medicationNamesIndex = 19),
        syncedTask(demoProfile02.id, status = TaskStatusEnum.Completed, medicationNamesIndex = 20),
        syncedTask(demoProfile02.id, status = TaskStatusEnum.Completed, medicationNamesIndex = 21),

        syncedTask(
            demoProfile01.id,
            status = TaskStatusEnum.Completed,
            isDirectAssignment = true,
            medicationNamesIndex = 22
        ),
        syncedTask(
            demoProfile01.id,
            isDeviceRequest = true,
            deviceRequestStatusIndex = 0,
            status = TaskStatusEnum.Ready,
            medicationNamesIndex = 23,
            appName = appNameProvider.next()
        ),
        syncedTask(
            demoProfile01.id,
            status = TaskStatusEnum.Ready,
            isDeviceRequest = true,
            deviceRequestStatusIndex = 1,
            medicationNamesIndex = 24,
            appName = appNameProvider.next()
        ),

        syncedTask(
            demoProfile02.id,
            status = TaskStatusEnum.InProgress,
            isDeviceRequest = true,
            deviceRequestStatusIndex = 1,
            medicationNamesIndex = 25,
            appName = appNameProvider.next()
        ),
        syncedTask(
            demoProfile02.id,
            status = TaskStatusEnum.Completed,
            isDeviceRequest = true,
            deviceRequestStatusIndex = 2,
            medicationNamesIndex = 26,
            appName = appNameProvider.next()
        ),
        syncedTask(
            demoProfile02.id,
            status = TaskStatusEnum.Completed,
            isDeviceRequest = true,
            deviceRequestStatusIndex = 2,
            medicationNamesIndex = 27,
            appName = appNameProvider.next()
        ),
        syncedTask(
            demoProfile01.id,
            status = TaskStatusEnum.Completed,
            isDeviceRequest = true,
            deviceRequestStatusIndex = 4,
            medicationNamesIndex = 28,
            appName = appNameProvider.next()
        ),
        syncedTask(
            demoProfile02.id,
            status = TaskStatusEnum.Completed,
            isDeviceRequest = true,
            deviceRequestStatusIndex = 5,
            medicationNamesIndex = 29,
            appName = appNameProvider.next()
        ),
        syncedTask(
            demoProfile01.id,
            status = TaskStatusEnum.Ready,
            isDeviceRequest = true,
            deviceRequestStatusIndex = 1,
            medicationNamesIndex = 1,
            appName = appNameProvider.next()
        ),

        // MultiReply demo scenario: three prescriptions redeemed together in the same order,
        // each answered with its own unique reply text (see [multiReplyDemoCommunications]).
        syncedTask(demoProfile01.id, status = TaskStatusEnum.InProgress, medicationNamesIndex = 31),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.InProgress, medicationNamesIndex = 32),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.InProgress, medicationNamesIndex = 33),

        // MultiReply demo scenario (broadcast variant): three prescriptions redeemed together in the same
        // order, all answered with the exact same reply text/transaction-id. This must be merged into a
        // single message listing all three task-ids (see [multiReplyCommunications]).
        syncedTask(demoProfile01.id, status = TaskStatusEnum.InProgress, medicationNamesIndex = 34),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.InProgress, medicationNamesIndex = 35),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.InProgress, medicationNamesIndex = 36),

        // MultiReply demo scenario using legacy V1 reply payloads (parsed via [CommunicationPayloadParser]):
        // one order where each task's pharmacy reply exercises a different combination of the legacy
        // pickUpCodeHR/pickUpCodeDMC/url/info_text fields, including the pickUpCodeHR+url-without-DMC
        // combination that used to be silently dropped (see [multiReplyLegacyV1Communications]), and one
        // task whose reply has none of these fields set, to show the "no message" empty state.
        syncedTask(demoProfile01.id, status = TaskStatusEnum.InProgress, medicationNamesIndex = 37),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.InProgress, medicationNamesIndex = 38),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.InProgress, medicationNamesIndex = 39),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.InProgress, medicationNamesIndex = 40),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.InProgress, medicationNamesIndex = 41),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.InProgress, medicationNamesIndex = 42),
        syncedTask(demoProfile01.id, status = TaskStatusEnum.InProgress, medicationNamesIndex = 43)
    )

    /**
     * Data sources for the [syncedTasks] created in the demo-mode
     */
    val syncedTasks: MutableStateFlow<MutableList<TaskErpModel.Synced>> =
        MutableStateFlow(syncedTasksList.toMutableList())

    /**
     * Data sources for the [scannedTasks] created in the demo-mode
     */
    val scannedTasks: MutableStateFlow<MutableList<TaskErpModel.Scanned>> =
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
    val auditEvents: MutableStateFlow<MutableList<FhirAuditEventErpModel>> =
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

    private val initialDemoCommunications: List<DemoModeProfileLinkedCommunication> by lazy {
        val taskId = "$DIRECT_ASSIGNMENT_TASK_PRESET.3"
        val orderId = "demo-order-v3-01"
        val pharmacyId = "pharmacy-demo-gematik"
        listOf(
            requestCommunication(
                profileId = demoProfile01.id,
                taskId = taskId,
                communicationId = "demo-comm-req-v3",
                pharmacyId = pharmacyId
            ).copy(orderId = orderId, pharmacyName = "Gematik Demo-Apotheke")
        ) + replyCommunications(
            profileId = demoProfile01.id,
            taskId = taskId,
            communicationId = "demo-comm-v3",
            pharmacyId = pharmacyId,
            orderId = orderId
        ) + multiReplyCommunications(
            profileId = demoProfile01.id,
            taskIds = listOf("$SYNCED_TASK_PRESET.31", "$SYNCED_TASK_PRESET.32", "$SYNCED_TASK_PRESET.33"),
            orderId = "demo-order-multireply-01",
            pharmacyId = pharmacyId
        ) + multiReplyCommunications(
            profileId = demoProfile01.id,
            taskIds = listOf("$SYNCED_TASK_PRESET.34", "$SYNCED_TASK_PRESET.35", "$SYNCED_TASK_PRESET.36"),
            orderId = "demo-order-multireply-02",
            pharmacyId = pharmacyId,
            useSameReplyForAllTasks = true
        ) + multiReplyLegacyV1Communications(
            profileId = demoProfile01.id,
            taskIds = listOf(
                "$SYNCED_TASK_PRESET.37",
                "$SYNCED_TASK_PRESET.38",
                "$SYNCED_TASK_PRESET.39",
                "$SYNCED_TASK_PRESET.40",
                "$SYNCED_TASK_PRESET.41",
                "$SYNCED_TASK_PRESET.42",
                "$SYNCED_TASK_PRESET.43"
            ),
            orderId = "demo-order-multireply-v1-01",
            pharmacyId = pharmacyId
        )
    }

    /**
     * Data sources for the [requestCommunication] created in the demo-mode,
     * this is used as the source for communication between the user, pharmacy and the doctor
     */
    val communications: MutableStateFlow<MutableList<DemoModeProfileLinkedCommunication>> =
        MutableStateFlow(initialDemoCommunications.toMutableList())

    val invoices: MutableStateFlow<MutableList<PKVInvoiceErpModel>> by lazy {
        val taskId = "$DIRECT_ASSIGNMENT_TASK_PRESET.3"
        val prescription = syncedTasksList.filterIsInstance<TaskErpModel.Synced.Prescription>().find { it.taskId == taskId }
        val invoice = PKVInvoiceErpModel(
            profileId = demoProfile01.id,
            taskId = taskId,
            accessCode = "DEMO_INVOICE_ACCESS_CODE",
            timestamp = Clock.System.now().minus(2.hours),
            pharmacyOrganization = prescription?.organization,
            practitionerOrganization = prescription?.organization,
            practitioner = prescription?.practitioner,
            patient = prescription?.patient,
            medicationRequest = prescription?.medicationRequest,
            whenHandedOver = null,
            consumed = false,
            invoice = InvoiceErpModel(
                totalAdditionalFee = 0.0,
                totalBruttoAmount = 14.95,
                currency = "EUR",
                chargeableItems = listOf(
                    ChargeableItemErpModel(
                        description = ChargeableItemDescriptionErpModel.PZN("12345678"),
                        text = prescription?.name ?: "Pantoprazol 20mg",
                        factor = 1.0,
                        price = PriceComponentErpModel(14.95, 2.39)
                    )
                )
            )
        )
        MutableStateFlow(mutableListOf(invoice))
    }

    // TODO: Wrong to expose database values directly
    val internalMessages: MutableStateFlow<MutableList<InternalMessageEntityV1>> =
        MutableStateFlow(mutableListOf(internalMessageEntityV1))

    val unreadInternalMessagesCount: MutableStateFlow<Long> =
        MutableStateFlow(0)

    val lastUpdatedVersion: MutableStateFlow<String> =
        MutableStateFlow("1.29.0")

    /**
     * Data source for the a [profileCommunicationLog] communication log that a particular profile has downloaded the information
     */
    val profileCommunicationLog: MutableStateFlow<MutableMap<String, Boolean>> =
        MutableStateFlow(mutableMapOf("no-profile-id" to false))

    /**
     * Data source for the connected device [pairedDevices] that will be shown to the user
     */
    val pairedDevices: MutableStateFlow<MutableList<Pair<PairingResponseEntry, PairingData>>> =
        MutableStateFlow(
            mutableListOf(
                PairingResponseEntry(
                    name = "Pixel 20",
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

    val euOrders: MutableStateFlow<MutableList<EuOrderErpModel>> = MutableStateFlow(mutableListOf())
    val euAccessCodes: MutableStateFlow<MutableList<EuAccessCodeErpModel>> = MutableStateFlow(mutableListOf())
    val orders: MutableStateFlow<MutableList<EuOrderErpModel>> = MutableStateFlow(mutableListOf())
    val events: MutableStateFlow<MutableList<FhirAuditEventErpModel>> = MutableStateFlow(mutableListOf())

    /**
     * Data sources for EU countries prescription
     */
    private val euCountriesList = listOf(
        Country("Österreich", "at", "🇦🇹"),
        Country("Belgien", "be", "🇧🇪"),
        Country("Tschechien", "cz", "🇨🇿"),
        Country("Dänemark", "dk", "🇩🇰"),
        Country("Estland", "ee", "🇪🇪"),
        Country("Finnland", "fi", "🇫🇮"),
        Country("Frankreich", "fr", "🇫🇷"),
        Country("Kroatien", "hr", "🇭🇷"),
        Country("Ungarn", "hu", "🇭🇺"),
        Country("Italien", "it", "🇮🇹"),
        Country("Luxemburg", "lu", "🇱🇺"),
        Country("Niederlande", "nl", "🇳🇱"),
        Country("Polen", "pl", "🇵🇱"),
        Country("Portugal", "pt", "🇵🇹"),
        Country("Schweden", "se", "🇸🇪"),
        Country("Spanien", "es", "🇪🇸")
    ).sortedBy { it.name }

    val euCountries: StateFlow<List<Country>> = MutableStateFlow(euCountriesList).asStateFlow()

    fun generateCode(): String {
        val uuid = UUID.randomUUID()
        val value = uuid.mostSignificantBits xor uuid.leastSignificantBits

        // Convert to positive number
        val positive = value.absoluteValue

        // Base36 = digits + uppercase letters
        return positive.toString(36)
            .uppercase()
            .take(7)
    }

    companion object {
        val communicationPayload: String = """
     {
        "version":1 , 
        "supplyOptionsType":"onPremise" , 
        "info_text":"Beispieltext für die Kommunikation zwischen Patient und Apotheke" , 
        "pickUpCodeHR":"1234567890" , 
        "pickUpCodeDMC":"0123456789" , 
        "url":"https://github.com/gematik/E-Rezept-App-Android"
        }
        """.trimIndent()

        fun replyCommunications(
            profileId: String,
            taskId: String,
            communicationId: String,
            pharmacyId: String,
            orderId: String,
            consumed: Boolean = false
        ): List<DemoModeProfileLinkedCommunication> {
            val baseTime = Clock.System.now()
            return listOf(
                // 1. V3 ReplyText
                DemoModeProfileLinkedCommunication(
                    profileId = profileId,
                    taskId = taskId,
                    communicationId = "$communicationId-v3-text",
                    sentOn = baseTime.minus(3.days),
                    sender = pharmacyId,
                    consumed = consumed,
                    profile = ErxCommunicationReply,
                    orderId = orderId,
                    payload = """
                        {
                            "version": 3,
                            "communicationType": "text",
                            "transactionID": "tx-v3-text",
                            "text": "Ihre verordneten Medikamente liegen in unserer Apotheke für Sie bereit."
                        }
                    """.trimIndent(),
                    recipient = "Erika Mustermann",
                    pharmacyName = "Gematik Demo-Apotheke"
                ),
                // 2. V3 PickUpCodeHR
                DemoModeProfileLinkedCommunication(
                    profileId = profileId,
                    taskId = taskId,
                    communicationId = "$communicationId-v3-pickup-hr",
                    sentOn = baseTime.minus(2.days),
                    sender = pharmacyId,
                    consumed = consumed,
                    profile = ErxCommunicationReply,
                    orderId = orderId,
                    payload = """
                        {
                            "version": 3,
                            "communicationType": "pickUpCodeHR",
                            "transactionID": "tx-v3-hr",
                            "pickupCodeHR": "HR-8421",
                            "text": "Ihr Abholcode lautet HR-8421. Bitte an der Kasse nennen."
                        }
                    """.trimIndent(),
                    recipient = "Erika Mustermann",
                    pharmacyName = "Gematik Demo-Apotheke"
                ),
                // 3. V3 PickUpCodeDMC
                DemoModeProfileLinkedCommunication(
                    profileId = profileId,
                    taskId = taskId,
                    communicationId = "$communicationId-v3-pickup-dmc",
                    sentOn = baseTime.minus(1.days).minus(4.hours),
                    sender = pharmacyId,
                    consumed = consumed,
                    profile = ErxCommunicationReply,
                    orderId = orderId,
                    payload = """
                        {
                            "version": 3,
                            "communicationType": "pickUpCodeDMC",
                            "transactionID": "tx-v3-dmc",
                            "pickupCodeDMC": "DMC-0987654321",
                            "text": "Bitte scannen Sie diesen Barcode an unserer 24/7-Abholstation."
                        }
                    """.trimIndent(),
                    recipient = "Erika Mustermann",
                    pharmacyName = "Gematik Demo-Apotheke"
                ),
                // 4. V3 ReplyLink
                DemoModeProfileLinkedCommunication(
                    profileId = profileId,
                    taskId = taskId,
                    communicationId = "$communicationId-v3-link",
                    sentOn = baseTime.minus(1.days).minus(2.hours),
                    sender = pharmacyId,
                    consumed = consumed,
                    profile = ErxCommunicationReply,
                    orderId = orderId,
                    payload = """
                        {
                            "version": 3,
                            "communicationType": "link",
                            "transactionID": "tx-v3-link",
                            "url": "https://github.com/gematik/E-Rezept-App-Android",
                            "text": "Weitere Details zu Ihrer Bestellung finden Sie in unserem Web-Portal."
                        }
                    """.trimIndent(),
                    recipient = "Erika Mustermann",
                    pharmacyName = "Gematik Demo-Apotheke"
                ),
                // 5. V3 DeliveryStatus
                DemoModeProfileLinkedCommunication(
                    profileId = profileId,
                    taskId = taskId,
                    communicationId = "$communicationId-v3-delivery",
                    sentOn = baseTime.minus(5.hours),
                    sender = pharmacyId,
                    consumed = consumed,
                    profile = ErxCommunicationReply,
                    orderId = orderId,
                    payload = """
                        {
                            "version": 3,
                            "communicationType": "deliveryStatus",
                            "transactionID": "tx-v3-delivery",
                            "deliveryStatus": "inTransport",
                            "text": "Ihre Botendienst-Lieferung befindet sich jetzt auf dem Weg zu Ihnen."
                        }
                    """.trimIndent(),
                    recipient = "Erika Mustermann",
                    pharmacyName = "Gematik Demo-Apotheke"
                ),
                // 6. V3 ReservationStatus
                DemoModeProfileLinkedCommunication(
                    profileId = profileId,
                    taskId = taskId,
                    communicationId = "$communicationId-v3-reservation",
                    sentOn = baseTime.minus(3.hours),
                    sender = pharmacyId,
                    consumed = consumed,
                    profile = ErxCommunicationReply,
                    orderId = orderId,
                    payload = """
                        {
                            "version": 3,
                            "communicationType": "reservationStatus",
                            "transactionID": "tx-v3-reservation",
                            "readyForCollection": "immediately"
                        }
                    """.trimIndent(),
                    recipient = "Erika Mustermann",
                    pharmacyName = "Gematik Demo-Apotheke"
                ),
                // 7. V3 PaymentInfo
                DemoModeProfileLinkedCommunication(
                    profileId = profileId,
                    taskId = taskId,
                    communicationId = "$communicationId-v3-payment",
                    sentOn = baseTime.minus(1.hours),
                    sender = pharmacyId,
                    consumed = consumed,
                    profile = ErxCommunicationReply,
                    orderId = orderId,
                    payload = """
                        {
                            "version": 3,
                            "communicationType": "paymentInfo",
                            "transactionID": "tx-v3-payment",
                            "totalAmount": 14.95,
                            "paymentMethods": [
                                { "method": "Online", "url": "https://github.com/gematik/E-Rezept-App-Android" }
                            ],
                            "text": "Rechnungsbetrag: 14,95 € - Bitte begleichen Sie den Betrag bei Abholung."
                        }
                    """.trimIndent(),
                    recipient = "Erika Mustermann",
                    pharmacyName = "Gematik Demo-Apotheke"
                ),
                // 8. Legacy V1 Reply
                DemoModeProfileLinkedCommunication(
                    profileId = profileId,
                    taskId = taskId,
                    communicationId = "$communicationId-v1-legacy",
                    sentOn = baseTime.minus(4.days),
                    sender = pharmacyId,
                    consumed = consumed,
                    profile = ErxCommunicationReply,
                    orderId = orderId,
                    payload = """
                        {
                            "version": 1,
                            "supplyOptionsType": "onPremise",
                            "info_text": "Eine Beispielnachricht aus der Apotheke im Legacy V1-Format.",
                            "pickUpCodeHR": "V1-8899",
                            "pickUpCodeDMC": "V1-DMC-4455",
                            "url": "https://github.com/gematik/E-Rezept-App-Android"
                        }
                    """.trimIndent(),
                    recipient = "Erika Mustermann",
                    pharmacyName = "Gematik Demo-Apotheke"
                )
            )
        }

        /**
         * Demonstrates "MultiReply" orders (see Test_Communications KBV 1_3_2 -> MultiReply postman flow):
         * multiple prescriptions are redeemed together in one order and the pharmacy answers each of the
         * [taskIds] individually. Regression coverage for [CommunicationMapper.groupByPayloadAndTaskId]:
         * replies with distinct payloads are shown as one message per task, but replies that carry the exact
         * same payload across different tasks of the same order are merged into a single message that lists
         * all the involved task-ids.
         *
         * When [useSameReplyForAllTasks] is true, every task receives the exact same reply text and
         * transaction-id (e.g. a broadcast status update) - this must result in a single merged message
         * listing all task-ids, not one message per task.
         */
        fun multiReplyCommunications(
            profileId: String,
            taskIds: List<String>,
            orderId: String,
            pharmacyId: String,
            consumed: Boolean = false,
            useSameReplyForAllTasks: Boolean = false
        ): List<DemoModeProfileLinkedCommunication> {
            val baseTime = Clock.System.now()
            val uniqueTexts = listOf(
                "Ihr erstes Medikament liegt zur Abholung bereit.",
                "Ihr zweites Medikament wird gerade für Sie vorbereitet.",
                "Ihr drittes Medikament ist bereits unterwegs zu Ihnen."
            )
            val sharedText = "Alle Ihre Medikamente sind zur Abholung bereit."
            return taskIds.flatMapIndexed { index, taskId ->
                val communicationId = "demo-comm-multireply-$orderId-$index"
                val text = if (useSameReplyForAllTasks) sharedText else uniqueTexts.getOrElse(index) { sharedText }
                val transactionId = if (useSameReplyForAllTasks) "tx-multireply-$orderId" else "tx-multireply-$orderId-$index"
                listOf(
                    requestCommunication(
                        profileId = profileId,
                        taskId = taskId,
                        communicationId = "$communicationId-req",
                        pharmacyId = pharmacyId,
                        sentOn = baseTime.minus(1.hours)
                    ).copy(orderId = orderId, pharmacyName = "Gematik Demo-Apotheke"),
                    DemoModeProfileLinkedCommunication(
                        profileId = profileId,
                        taskId = taskId,
                        communicationId = "$communicationId-reply",
                        sentOn = baseTime.minus((30 - index * 5).minutes),
                        sender = pharmacyId,
                        consumed = consumed,
                        profile = ErxCommunicationReply,
                        orderId = orderId,
                        payload = """
                            {
                                "version": 3,
                                "communicationType": "text",
                                "transactionID": "$transactionId",
                                "text": "$text"
                            }
                        """.trimIndent(),
                        recipient = "Erika Mustermann",
                        pharmacyName = "Gematik Demo-Apotheke"
                    )
                )
            }
        }

        /**
         * Demonstrates a MultiReply order where every reply is a legacy V1 payload (`"version": 1`), routed
         * through [CommunicationPayloadParser.extract] just like a real backend response, so the parser's
         * V1-combination handling is exercised in demo mode too. Covers, one task each for:
         * pickUpCodeDMC only, pickUpCodeHR only, url only, info_text only, pickUpCodeHR+url without a DMC
         * code (the exact combination that used to silently drop the link, see
         * [CommunicationPayloadParser.mapV1ToV3ReplyModel]), all four fields combined, and finally a reply
         * with none of them set to show the "no message" empty state.
         */
        fun multiReplyLegacyV1Communications(
            profileId: String,
            taskIds: List<String>,
            orderId: String,
            pharmacyId: String,
            consumed: Boolean = false
        ): List<DemoModeProfileLinkedCommunication> {
            val baseTime = Clock.System.now()

            data class LegacyV1Reply(val idSuffix: String, val payload: String)

            val replies = listOf(
                LegacyV1Reply(
                    idSuffix = "v1-dmc-only",
                    payload = """
                        {
                            "version": 1,
                            "supplyOptionsType": "onPremise",
                            "pickUpCodeDMC": "V1-DMC-ONLY-1234"
                        }
                    """.trimIndent()
                ),
                LegacyV1Reply(
                    idSuffix = "v1-hr-only",
                    payload = """
                        {
                            "version": 1,
                            "supplyOptionsType": "onPremise",
                            "pickUpCodeHR": "V1-HR-ONLY-5678"
                        }
                    """.trimIndent()
                ),
                LegacyV1Reply(
                    idSuffix = "v1-link-only",
                    payload = """
                        {
                            "version": 1,
                            "supplyOptionsType": "onPremise",
                            "url": "https://github.com/gematik/E-Rezept-App-Android"
                        }
                    """.trimIndent()
                ),
                LegacyV1Reply(
                    idSuffix = "v1-text-only",
                    payload = """
                        {
                            "version": 1,
                            "supplyOptionsType": "onPremise",
                            "info_text": "Ihre Bestellung wird gerade für Sie vorbereitet (Legacy V1-Format)."
                        }
                    """.trimIndent()
                ),
                // Regression case: pickUpCodeHR + url without a DMC code used to make the parser silently
                // drop the url, see [CommunicationPayloadParser.mapV1ToV3ReplyModel].
                LegacyV1Reply(
                    idSuffix = "v1-hr-link-combo",
                    payload = """
                        {
                            "version": 1,
                            "supplyOptionsType": "onPremise",
                            "info_text": "Ihr Abholcode sowie ein Link zu weiteren Informationen.",
                            "pickUpCodeHR": "V1-HRLINK-9911",
                            "url": "https://github.com/gematik/E-Rezept-App-Android"
                        }
                    """.trimIndent()
                ),
                LegacyV1Reply(
                    idSuffix = "v1-full-combo",
                    payload = """
                        {
                            "version": 1,
                            "supplyOptionsType": "onPremise",
                            "info_text": "Alle Informationen zu Ihrer Bestellung in einer Nachricht (Legacy V1-Format).",
                            "pickUpCodeHR": "V1-FULL-4432",
                            "pickUpCodeDMC": "V1-FULL-DMC-8877",
                            "url": "https://github.com/gematik/E-Rezept-App-Android"
                        }
                    """.trimIndent()
                ),
                // No pickup code, no url, no text -> shows the "no message" empty state.
                LegacyV1Reply(
                    idSuffix = "v1-empty",
                    payload = """
                        {
                            "version": 1,
                            "supplyOptionsType": "onPremise"
                        }
                    """.trimIndent()
                )
            )

            return taskIds.zip(replies).flatMapIndexed { index, (taskId, legacyReply) ->
                val communicationId = "demo-comm-multireply-legacy-$orderId-${legacyReply.idSuffix}"
                listOf(
                    requestCommunication(
                        profileId = profileId,
                        taskId = taskId,
                        communicationId = "$communicationId-req",
                        pharmacyId = pharmacyId,
                        sentOn = baseTime.minus(2.hours)
                    ).copy(orderId = orderId, pharmacyName = "Gematik Demo-Apotheke"),
                    DemoModeProfileLinkedCommunication(
                        profileId = profileId,
                        taskId = taskId,
                        communicationId = "$communicationId-reply",
                        sentOn = baseTime.minus((60 - index * 5).minutes),
                        sender = pharmacyId,
                        consumed = consumed,
                        profile = ErxCommunicationReply,
                        orderId = orderId,
                        payload = legacyReply.payload,
                        recipient = "Erika Mustermann",
                        pharmacyName = "Gematik Demo-Apotheke"
                    )
                )
            }
        }

        fun requestCommunication(
            profileId: String,
            taskId: String,
            communicationId: String,
            pharmacyId: String,
            consumed: Boolean = false,
            sentOn: Instant = Clock.System.now().minus(5.days)
        ): DemoModeProfileLinkedCommunication {
            val orderId = UUID.randomUUID().toString()
            return DemoModeProfileLinkedCommunication(
                profileId = profileId,
                taskId = taskId,
                communicationId = communicationId,
                sentOn = sentOn,
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
