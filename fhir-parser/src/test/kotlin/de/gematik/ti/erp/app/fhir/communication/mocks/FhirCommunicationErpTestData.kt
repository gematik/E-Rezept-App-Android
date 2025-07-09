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

package de.gematik.ti.erp.app.fhir.communication.mocks

import de.gematik.ti.erp.app.fhir.common.model.erp.FhirCommunicationBundleErpModel
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirDispenseCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirReplyCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.FhirCommunicationVersions.COMMUNICATION_VERSION_1_2
import de.gematik.ti.erp.app.fhir.communication.FhirCommunicationVersions.COMMUNICATION_VERSION_1_3
import de.gematik.ti.erp.app.fhir.communication.FhirCommunicationVersions.COMMUNICATION_VERSION_1_4
import de.gematik.ti.erp.app.fhir.communication.model.erp.CommunicationParticipantErpModel
import de.gematik.ti.erp.app.fhir.communication.model.erp.DispenseCommunicationPayloadContentErpModel
import de.gematik.ti.erp.app.fhir.communication.model.erp.DispensePrescriptionTypeErpModel
import de.gematik.ti.erp.app.fhir.communication.model.erp.DispenseSupplyOptionsType
import de.gematik.ti.erp.app.fhir.communication.model.erp.ReplyCommunicationPayloadContentErpModel
import de.gematik.ti.erp.app.fhir.communication.model.erp.ReplyCommunicationSupplyOptionsErpModel
import de.gematik.ti.erp.app.utils.FhirTemporal
import kotlinx.datetime.Instant

/**
 * Test data for FHIR Communication models used in tests
 */
object FhirCommunicationErpTestData {

    // Expected models for V1.4 reply communication
    private val expectedReplyEntryV1_4 = FhirReplyCommunicationEntryErpModel(
        id = "01ebc980-ae10-41f0-5a9f-c8ad61141a66",
        profile = COMMUNICATION_VERSION_1_4,
        taskId = "160.000.226.545.733.51",
        sent = FhirTemporal.Instant(Instant.parse("2024-08-14T11:14:38.230Z")),
        received = FhirTemporal.Instant(Instant.parse("2024-08-14T11:14:46.000Z")),
        sender = CommunicationParticipantErpModel(
            identifier = "3-01.2.2023001.16.103",
            identifierSystem = "https://gematik.de/fhir/sid/telematik-id"
        ),
        recipient = CommunicationParticipantErpModel(
            identifier = "X110432693",
            identifierSystem = "http://fhir.de/sid/gkv/kvid-10"
        ),
        payload = ReplyCommunicationPayloadContentErpModel(
            text = "Eisern",
            supplyOptions = ReplyCommunicationSupplyOptionsErpModel(
                onPremise = true,
                delivery = false,
                shipment = false
            )
        ),
        orderId = null
    )

    private val expectedReplyEntryV1_4_second = FhirReplyCommunicationEntryErpModel(
        id = "01ebc980-c555-9bf8-66b2-0d434e302916",
        profile = COMMUNICATION_VERSION_1_4,
        taskId = "160.000.226.545.733.51",
        sent = FhirTemporal.Instant(Instant.parse("2024-08-14T11:21:08.651Z")),
        received = FhirTemporal.Instant(Instant.parse("2024-08-14T11:21:15.000Z")),
        sender = CommunicationParticipantErpModel(
            identifier = "3-01.2.2023001.16.103",
            identifierSystem = "https://gematik.de/fhir/sid/telematik-id"
        ),
        recipient = CommunicationParticipantErpModel(
            identifier = "X110432693",
            identifierSystem = "http://fhir.de/sid/gkv/kvid-10"
        ),
        payload = ReplyCommunicationPayloadContentErpModel(
            text = "Eisern",
            supplyOptions = ReplyCommunicationSupplyOptionsErpModel(
                onPremise = true,
                delivery = false,
                shipment = false
            )
        ),
        orderId = null
    )

    private val expectedReplyEntryV1_4_third = FhirReplyCommunicationEntryErpModel(
        id = "01ebc980-cb72-d730-762e-dd08075f568a",
        profile = COMMUNICATION_VERSION_1_4,
        taskId = "160.000.226.545.733.51",
        sent = FhirTemporal.Instant(Instant.parse("2024-08-14T11:22:51.230Z")),
        received = FhirTemporal.Instant(Instant.parse("2024-08-14T11:28:44.000Z")),
        sender = CommunicationParticipantErpModel(
            identifier = "3-01.2.2023001.16.103",
            identifierSystem = "https://gematik.de/fhir/sid/telematik-id"
        ),
        recipient = CommunicationParticipantErpModel(
            identifier = "X110432693",
            identifierSystem = "http://fhir.de/sid/gkv/kvid-10"
        ),
        payload = ReplyCommunicationPayloadContentErpModel(
            text = "Eisern",
            supplyOptions = ReplyCommunicationSupplyOptionsErpModel(
                onPremise = true,
                delivery = false,
                shipment = false
            )
        ),
        orderId = null
    )

    // Expected models for V1.3 reply communication
    private val expectedReplyEntryV1_3 = FhirReplyCommunicationEntryErpModel(
        id = "01ebc980-ae10-41f0-5a9f-c8ad61141a66",
        profile = COMMUNICATION_VERSION_1_3,
        taskId = "160.000.226.545.733.51",
        sent = FhirTemporal.Instant(Instant.parse("2024-08-14T11:14:38.230Z")),
        received = FhirTemporal.Instant(Instant.parse("2024-08-14T11:14:46.000Z")),
        sender = CommunicationParticipantErpModel(
            identifier = "3-01.2.2023001.16.103",
            identifierSystem = "https://gematik.de/fhir/sid/telematik-id"
        ),
        recipient = CommunicationParticipantErpModel(
            identifier = "X110432693",
            identifierSystem = "http://fhir.de/sid/gkv/kvid-10"
        ),
        payload = ReplyCommunicationPayloadContentErpModel(
            text = "Eisern",
            supplyOptions = ReplyCommunicationSupplyOptionsErpModel(
                onPremise = true,
                delivery = false,
                shipment = false
            )
        ),
        orderId = null
    )

    // Expected models for V1.2 reply communication
    private val expectedReplyEntryV1_2 = FhirReplyCommunicationEntryErpModel(
        id = "7977a4ab-97a9-4d95-afb3-6c4c1e2ac596",
        profile = COMMUNICATION_VERSION_1_2,
        taskId = "160.000.033.491.280.78",
        sent = FhirTemporal.Instant(Instant.parse("2020-04-29T11:46:30.128Z")),
        received = null,
        sender = CommunicationParticipantErpModel(
            identifier = "3-SMC-B-Testkarte-883110000123465",
            identifierSystem = "https://gematik.de/fhir/sid/telematik-id"
        ),
        recipient = CommunicationParticipantErpModel(
            identifier = "X234567890",
            identifierSystem = "http://fhir.de/sid/gkv/kvid-10"
        ),
        payload = ReplyCommunicationPayloadContentErpModel(
            text = "Eisern",
            supplyOptions = ReplyCommunicationSupplyOptionsErpModel(
                onPremise = true,
                delivery = true,
                shipment = false
            )
        ),
        orderId = null
    )

    // Expected models for V1.4 dispense communication
    private val expectedDispenseEntryV1_4 = FhirDispenseCommunicationEntryErpModel(
        id = "01ebd9e1-47d8-bab8-2566-31341cc59b11",
        profile = "1.4",
        taskId = "200.000.000.157.911.86",
        sender = CommunicationParticipantErpModel(
            identifier = "X110571977",
            identifierSystem = "http://fhir.de/sid/gkv/kvid-10"
        ),
        recipient = CommunicationParticipantErpModel(
            identifier = "3-10.2.0111108800.16.806",
            identifierSystem = "https://gematik.de/fhir/sid/telematik-id"
        ),
        payload = DispenseCommunicationPayloadContentErpModel(
            contentString = "{\"version\":1,\"supplyOptionsType\":\"onPremise\",\"name\":\"Paula" +
                " Privati\",\"address\":[\"Blumenweg\",\"\",\"26427\",\"Esens\"]" +
                ",\"hint\":\"\",\"phone\":\"\"}",
            supplyOptionsType = DispenseSupplyOptionsType.ON_PREMISE,
            name = "Paula Privati",
            address = listOf("Blumenweg", "", "26427", "Esens"),
            phone = ""
        ),
        prescriptionType = DispensePrescriptionTypeErpModel(
            code = "200",
            system = "https://gematik.de/fhir/erp/CodeSystem/GEM_ERP_CS_FlowType",
            display = "PKV (Apothekenpflichtige Arzneimittel)"
        ),
        sent = FhirTemporal.Instant(Instant.parse("2025-03-10T21:12:41.187Z")),
        orderId = "7526e7d4-deeb-432c-97bd-7f4ccfa7e901"
    )

    // Expected model for single V1.4 dispense communication
    private val expectedSingleDispenseEntryV1_4 = FhirDispenseCommunicationEntryErpModel(
        id = "erp-communication-05-request-RezeptZuweisen",
        profile = "1.4",
        taskId = "160.000.000.000.000.01",
        sender = null,
        recipient = CommunicationParticipantErpModel(
            identifier = "3-2-APO-XanthippeVeilchenblau01",
            identifierSystem = "https://gematik.de/fhir/sid/telematik-id"
        ),
        payload = DispenseCommunicationPayloadContentErpModel(
            contentString = "{ \"version\": 1, \"supplyOptionsType\": \"onPremise\", \"name\": \"Dr. Maximilian" +
                " von Muster\", \"address\": [ \"wohnhaft bei Emilia Fischer\", \"Bundesallee" +
                " 312\", \"123. OG\", \"12345 Berlin\" ], \"phone\": \"004916094858168\" }",
            supplyOptionsType = DispenseSupplyOptionsType.ON_PREMISE,
            name = "Dr. Maximilian von Muster",
            address = listOf("wohnhaft bei Emilia Fischer", "Bundesallee 312", "123. OG", "12345 Berlin"),
            phone = "004916094858168"
        ),
        prescriptionType = DispensePrescriptionTypeErpModel(
            code = "160",
            system = "https://gematik.de/fhir/erp/CodeSystem/GEM_ERP_CS_FlowType",
            display = "Muster 16 (Apothekenpflichtige Arzneimittel)"
        ),
        sent = null,
        orderId = null
    )

    // Expected model for single V1.2 dispense communication
    private val expectedSingleDispenseEntryV1_2 = FhirDispenseCommunicationEntryErpModel(
        id = "",
        profile = "1.2",
        taskId = "200.000.000.190.660.03",
        sender = null,
        recipient = CommunicationParticipantErpModel(
            identifier = "3-01.2.2023001.16.101",
            identifierSystem = "https://gematik.de/fhir/sid/telematik-id"
        ),
        payload = DispenseCommunicationPayloadContentErpModel(
            contentString = "{\"version\":1,\"supplyOptionsType\":\"onPremise\",\"name\":\"Paula" +
                " Privati\",\"address\":[\"Blumenweg\",\"\",\"26427\",\"Esens\"],\"hint\":\"\",\"phone\":\"\"}",
            supplyOptionsType = DispenseSupplyOptionsType.ON_PREMISE,
            name = "Paula Privati",
            address = listOf("Blumenweg", "", "26427", "Esens"),
            phone = ""
        ),
        prescriptionType = null,
        sent = null,
        orderId = "9437677e-729a-4dde-b174-e39802031423"
    )

    // Complete models for bundle tests
    val replyEntriesV1_4Bundle = FhirCommunicationBundleErpModel(
        total = 3,
        messages = listOf(
            expectedReplyEntryV1_4,
            expectedReplyEntryV1_4_second,
            expectedReplyEntryV1_4_third
        )
    )

    val dispenseEntryV1_4Bundle = FhirCommunicationBundleErpModel(
        total = 1,
        messages = listOf(expectedDispenseEntryV1_4)
    )

    val mixedBundleV1_4Model = FhirCommunicationBundleErpModel(
        total = 4,
        messages = listOf(
            expectedDispenseEntryV1_4,
            expectedReplyEntryV1_4,
            expectedReplyEntryV1_3,
            expectedReplyEntryV1_2
        )
    )
    val mixedBundleV1_2Model = FhirCommunicationBundleErpModel(
        total = 4,
        messages = listOf(
            expectedSingleDispenseEntryV1_2.copy(orderId = "9437677e-729a-4dde-b174-e39802031423"),
            expectedReplyEntryV1_4,
            expectedReplyEntryV1_3,
            expectedReplyEntryV1_2
        )
    )

    // Complete models for single communication tests
    val singleReplyV1_4Model = FhirCommunicationBundleErpModel(
        total = 1,
        messages = listOf(expectedReplyEntryV1_4)
    )

    val singleReplyV1_3Model = FhirCommunicationBundleErpModel(
        total = 1,
        messages = listOf(expectedReplyEntryV1_3)
    )

    val singleReplyV1_2Model = FhirCommunicationBundleErpModel(
        total = 1,
        messages = listOf(expectedReplyEntryV1_2)
    )

    val singleDispenseV1_4Model = FhirCommunicationBundleErpModel(
        total = 1,
        messages = listOf(expectedSingleDispenseEntryV1_4)
    )

    val singleDispenseV1_2Model = FhirCommunicationBundleErpModel(
        total = 1,
        messages = listOf(expectedSingleDispenseEntryV1_2)
    )
}
