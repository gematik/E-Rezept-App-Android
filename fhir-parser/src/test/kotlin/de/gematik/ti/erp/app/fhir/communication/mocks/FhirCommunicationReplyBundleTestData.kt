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

@file:Suppress("ktlint:max-line-length")

package de.gematik.ti.erp.app.fhir.communication.mocks

import de.gematik.ti.erp.app.fhir.FhirCommunicationBundleErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirDispenseCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirReplyCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.model.support.CommunicationParticipantErpModel
import de.gematik.ti.erp.app.fhir.communication.model.support.DispenseCommunicationPayloadContentErpModel
import de.gematik.ti.erp.app.fhir.communication.model.support.DispensePrescriptionTypeErpModel
import de.gematik.ti.erp.app.fhir.communication.model.support.DispenseSupplyOptionsType
import de.gematik.ti.erp.app.fhir.communication.model.support.ReplyCommunicationPayloadContentErpModel
import de.gematik.ti.erp.app.fhir.communication.model.support.ReplyCommunicationSupplyOptionsErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporalSerializationType
import kotlinx.datetime.Instant

object FhirCommunicationReplyBundleTestData {

    val replyBundle: FhirCommunicationBundleErpModel =
        FhirCommunicationBundleErpModel(
            total = 39,
            messages = buildList {
                // --- Helpers to reduce repetition ---
                fun instant(s: String) = FhirTemporal.Instant(value = Instant.parse(s), type = FhirTemporalSerializationType.FhirTemporalInstant)
                fun senderTI(id: String) = CommunicationParticipantErpModel(
                    identifier = id,
                    identifierSystem = "https://gematik.de/fhir/sid/telematik-id"
                )

                fun senderKVNR(id: String) = CommunicationParticipantErpModel(
                    identifier = id,
                    identifierSystem = "http://fhir.de/sid/gkv/kvid-10"
                )

                fun reply(
                    id: String,
                    profile: String,
                    senderTi: String,
                    recipientKvnr: String,
                    sent: String,
                    received: String?,
                    textJson: String,
                    onPremise: Boolean,
                    shipment: Boolean,
                    delivery: Boolean,
                    isDiga: Boolean
                ) = FhirReplyCommunicationEntryErpModel(
                    id = id,
                    profile = profile,
                    taskId = null,
                    sender = senderTI(senderTi),
                    recipient = senderKVNR(recipientKvnr),
                    orderId = null,
                    sent = instant(sent),
                    received = received?.let(::instant),
                    payload = ReplyCommunicationPayloadContentErpModel(
                        text = textJson,
                        supplyOptions = ReplyCommunicationSupplyOptionsErpModel(
                            onPremise = onPremise,
                            shipment = shipment,
                            delivery = delivery
                        )
                    ),
                    isDiga = isDiga
                )

                fun dispense(
                    id: String,
                    profile: String,
                    senderKvnr: String,
                    recipientTi: String,
                    sent: String,
                    contentString: String,
                    supplyOptionsType: DispenseSupplyOptionsType,
                    flowCode: String,
                    flowDisplay: String,
                    isDiga: Boolean
                ) = FhirDispenseCommunicationEntryErpModel(
                    id = id,
                    profile = profile,
                    taskId = null,
                    sender = senderKVNR(senderKvnr),
                    recipient = senderTI(recipientTi),
                    orderId = null,
                    sent = instant(sent),
                    payload = DispenseCommunicationPayloadContentErpModel(
                        contentString = contentString,
                        supplyOptionsType = supplyOptionsType,
                        name = null,
                        address = null,
                        phone = null
                    ),
                    prescriptionType = DispensePrescriptionTypeErpModel(
                        code = flowCode,
                        system = "https://gematik.de/fhir/erp/CodeSystem/GEM_ERP_CS_FlowType",
                        display = flowDisplay
                    ),
                    isDiga = isDiga
                )

                // ---------- 1.5 replies (onPremise) ----------
                add(
                    reply(
                        id = "01ebe55a-0410-a488-3f5d-e5e0bed59890",
                        profile = "1.5",
                        senderTi = "3-SMC-B-Testkarte--883110000163973",
                        recipientKvnr = "X110614233",
                        sent = "2025-08-03T20:52:22.949Z",
                        received = "2025-08-03T20:52:23Z",
                        textJson = """{"version":1,"supplyOptionsType":"onPremise","info_text":"Nachricht Nr. 4 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = true, shipment = false, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe55a-040e-8978-a838-8c3b3ff86b6d",
                        profile = "1.5",
                        senderTi = "3-SMC-B-Testkarte--883110000163973",
                        recipientKvnr = "X110614233",
                        sent = "2025-08-03T20:52:22.811Z",
                        received = "2025-08-03T20:52:23Z",
                        textJson = """{"version":1,"supplyOptionsType":"onPremise","info_text":"Nachricht Nr. 3 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = true, shipment = false, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe55a-040b-e998-2cca-5f2d2b60397b",
                        profile = "1.5",
                        senderTi = "3-SMC-B-Testkarte--883110000163973",
                        recipientKvnr = "X110614233",
                        sent = "2025-08-03T20:52:22.639Z",
                        received = "2025-08-03T20:52:23Z",
                        textJson = """{"version":1,"supplyOptionsType":"onPremise","info_text":"Nachricht Nr. 2 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = true, shipment = false, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe55a-0409-49b8-063e-23fa83e746a6",
                        profile = "1.5",
                        senderTi = "3-SMC-B-Testkarte--883110000163973",
                        recipientKvnr = "X110614233",
                        sent = "2025-08-03T20:52:22.467Z",
                        received = "2025-08-03T20:52:23Z",
                        textJson = """{"version":1,"supplyOptionsType":"onPremise","info_text":"Nachricht Nr. 1 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = true, shipment = false, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe55a-0406-82c8-2487-fc3b980e85b1",
                        profile = "1.5",
                        senderTi = "3-SMC-B-Testkarte--883110000163973",
                        recipientKvnr = "X110614233",
                        sent = "2025-08-03T20:52:22.285Z",
                        received = "2025-08-03T20:52:23Z",
                        textJson = """{"version":1,"supplyOptionsType":"onPremise","info_text":"Nachricht Nr. 0 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = true, shipment = false, delivery = false,
                        isDiga = false
                    )
                )

                // ---------- 1.5 pair (reply shipment + dispense shipment) ----------
                add(
                    reply(
                        id = "01ebe505-234e-1100-f9bf-3a40f120a93b",
                        profile = "1.5",
                        senderTi = "3-SMC-B-Testkarte--883110000163973",
                        recipientKvnr = "X110614233",
                        sent = "2025-07-30T15:36:34.848Z",
                        received = "2025-07-30T15:36:35Z",
                        textJson = """{"version":1,"supplyOptionsType":"shipment","info_text":"Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = false, shipment = true, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    dispense(
                        id = "01ebe505-2341-b108-7d7c-1392712e3399",
                        profile = "1.5",
                        senderKvnr = "X110614233",
                        recipientTi = "3-SMC-B-Testkarte--883110000163973",
                        sent = "2025-07-30T15:36:34.037Z",
                        contentString = """{"version":1,"supplyOptionsType":"shipment","hint":"Nachricht Nr. {0} zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        supplyOptionsType = DispenseSupplyOptionsType.SHIPMENT,
                        flowCode = "169",
                        flowDisplay = "Muster 16 (Direkte Zuweisung)",
                        isDiga = false
                    )
                )

                // ---------- 1.5 reply onPremise ----------
                add(
                    reply(
                        id = "01ebe4ef-7aa6-51a8-dd9f-7aaec3ca8129",
                        profile = "1.5",
                        senderTi = "3-SMC-B-Testkarte--883110000163973",
                        recipientKvnr = "X110614233",
                        sent = "2025-07-29T13:46:10.969Z",
                        received = "2025-07-29T13:46:11Z",
                        textJson = """{"version":1,"supplyOptionsType":"onPremise","info_text":"Nachricht Nr. 4 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = true, shipment = false, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe4ef-7aa4-03d0-10a2-7aeac2787f42",
                        profile = "1.5",
                        senderTi = "3-SMC-B-Testkarte--883110000163973",
                        recipientKvnr = "X110614233",
                        sent = "2025-07-29T13:46:10.818Z",
                        received = "2025-07-29T13:46:11Z",
                        textJson = """{"version":1,"supplyOptionsType":"onPremise","info_text":"Nachricht Nr. 3 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = true, shipment = false, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe4ef-7aa1-b5f8-3606-d5a43d4dcb98",
                        profile = "1.5",
                        senderTi = "3-SMC-B-Testkarte--883110000163973",
                        recipientKvnr = "X110614233",
                        sent = "2025-07-29T13:46:10.667Z",
                        received = "2025-07-29T13:46:11Z",
                        textJson = """{"version":1,"supplyOptionsType":"onPremise","info_text":"Nachricht Nr. 2 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = true, shipment = false, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe4ef-7a9f-9ed0-d0e9-47f08eff1a4a",
                        profile = "1.5",
                        senderTi = "3-SMC-B-Testkarte--883110000163973",
                        recipientKvnr = "X110614233",
                        sent = "2025-07-29T13:46:10.530Z",
                        received = "2025-07-29T13:46:11Z",
                        textJson = """{"version":1,"supplyOptionsType":"onPremise","info_text":"Nachricht Nr. 1 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = true, shipment = false, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe4ef-7a9d-4158-bcf9-419379763fdb",
                        profile = "1.5",
                        senderTi = "3-SMC-B-Testkarte--883110000163973",
                        recipientKvnr = "X110614233",
                        sent = "2025-07-29T13:46:10.375Z",
                        received = "2025-07-29T13:46:11Z",
                        textJson = """{"version":1,"supplyOptionsType":"onPremise","info_text":"Nachricht Nr. 0 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = true, shipment = false, delivery = false,
                        isDiga = false
                    )
                )

                // ---------- 1.5 pair (reply onPremise + dispense onPremise) ----------
                add(
                    reply(
                        id = "01ebe403-f63b-ff30-050d-49ca157f4d7d",
                        profile = "1.5",
                        senderTi = "3-SMC-B-Testkarte--883110000163973",
                        recipientKvnr = "X110614233",
                        sent = "2025-07-17T20:47:12.094Z",
                        received = "2025-07-17T20:47:12Z",
                        textJson = """{"version":1,"supplyOptionsType":"onPremise","info_text":"Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = true, shipment = false, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    dispense(
                        id = "01ebe403-f62f-1298-4c50-a10fa68ca1df",
                        profile = "1.5",
                        senderKvnr = "X110614233",
                        recipientTi = "3-SMC-B-Testkarte--883110000163973",
                        sent = "2025-07-17T20:47:11.247Z",
                        contentString = """{"version":1,"supplyOptionsType":"onPremise","hint":"Nachricht Nr. {0} zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        supplyOptionsType = DispenseSupplyOptionsType.ON_PREMISE,
                        flowCode = "200",
                        flowDisplay = "PKV (Apothekenpflichtige Arzneimittel)",
                        isDiga = false
                    )
                )

                // ---------- 1.4 replies (delivery) ----------
                val v14Sender = "3-SMC-B-Testkarte--883110000163973"
                val v14Recipient = "X110614233"
                add(
                    reply(
                        id = "01ebe3ef-dc34-ba00-5bc6-93c713065d1a",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-07-16T20:48:16.064Z",
                        received = "2025-07-16T20:48:16Z",
                        textJson = """{"version":1,"supplyOptionsType":"delivery","info_text":"Nachricht Nr. 4 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = false, shipment = false, delivery = true,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe3ef-dc32-9ef0-c710-7f1f50ea0c30",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-07-16T20:48:15.926Z",
                        received = "2025-07-16T20:48:16Z",
                        textJson = """{"version":1,"supplyOptionsType":"delivery","info_text":"Nachricht Nr. 3 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = false, shipment = false, delivery = true,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe3ef-dc30-2620-766d-ec3a257ccc12",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-07-16T20:48:15.764Z",
                        received = "2025-07-16T20:48:16Z",
                        textJson = """{"version":1,"supplyOptionsType":"delivery","info_text":"Nachricht Nr. 2 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = false, shipment = false, delivery = true,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe3ef-dc2d-dc30-d452-cfa90af38b4f",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-07-16T20:48:15.614Z",
                        received = "2025-07-16T20:48:16Z",
                        textJson = """{"version":1,"supplyOptionsType":"delivery","info_text":"Nachricht Nr. 1 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = false, shipment = false, delivery = true,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe3ef-dc2b-ad98-0e4a-af6574da8dc5",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-07-16T20:48:15.471Z",
                        received = "2025-07-16T20:48:16Z",
                        textJson = """{"version":1,"supplyOptionsType":"delivery","info_text":"Nachricht Nr. 0 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = false, shipment = false, delivery = true,
                        isDiga = false
                    )
                )

                // ---------- 1.4 pair (onPremise) ----------
                add(
                    reply(
                        id = "01ebe326-f861-6558-520c-4c3dcba45219",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-07-06T21:08:00.327Z",
                        received = "2025-07-06T21:08:00Z",
                        textJson = """{"version":1,"supplyOptionsType":"onPremise","info_text":"Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = true, shipment = false, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    dispense(
                        id = "01ebe326-f853-a1e8-e009-118572565901",
                        profile = "1.4",
                        senderKvnr = v14Recipient,
                        recipientTi = v14Sender,
                        sent = "2025-07-06T21:07:59.425Z",
                        contentString = """{"version":1,"supplyOptionsType":"onPremise","hint":"Nachricht Nr. {0} zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        supplyOptionsType = DispenseSupplyOptionsType.ON_PREMISE,
                        flowCode = "200",
                        flowDisplay = "PKV (Apothekenpflichtige Arzneimittel)",
                        isDiga = false
                    )
                )

                // ---------- 1.4 pair (shipment) ----------
                add(
                    reply(
                        id = "01ebe1f8-ecff-d7c8-203b-f92cb8ac95c3",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-06-21T20:46:49.261Z",
                        received = "2025-06-21T20:46:49Z",
                        textJson = """{"version":1,"supplyOptionsType":"shipment","info_text":"Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = false, shipment = true, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    dispense(
                        id = "01ebe1f8-ecf5-4c90-72aa-7682dd0cc5c1",
                        profile = "1.4",
                        senderKvnr = v14Recipient,
                        recipientTi = v14Sender,
                        sent = "2025-06-21T20:46:48.570Z",
                        contentString = """{"version":1,"supplyOptionsType":"shipment","hint":"Nachricht Nr. {0} zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        supplyOptionsType = DispenseSupplyOptionsType.SHIPMENT,
                        flowCode = "160",
                        flowDisplay = "Muster 16 (Apothekenpflichtige Arzneimittel)",
                        isDiga = false
                    )
                )

                // ---------- 1.4 replies (onPremise) ----------
                add(
                    reply(
                        id = "01ebe143-dbb5-5688-6d3b-81ca51c513e6",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-06-12T20:45:30.085Z",
                        received = "2025-06-12T20:45:30Z",
                        textJson = """{"version":1,"supplyOptionsType":"onPremise","info_text":"Nachricht Nr. 4 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = true, shipment = false, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe143-dbb3-81c8-60a6-39e108a9f188",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-06-12T20:45:29.965Z",
                        received = "2025-06-12T20:45:30Z",
                        textJson = """{"version":1,"supplyOptionsType":"onPremise","info_text":"Nachricht Nr. 3 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = true, shipment = false, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe143-dbb1-6aa0-7840-df26c92a9f17",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-06-12T20:45:29.828Z",
                        received = "2025-06-12T20:45:30Z",
                        textJson = """{"version":1,"supplyOptionsType":"onPremise","info_text":"Nachricht Nr. 2 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = true, shipment = false, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe143-dbaf-cc90-1720-b63f671457d8",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-06-12T20:45:29.722Z",
                        received = "2025-06-12T20:45:30Z",
                        textJson = """{"version":1,"supplyOptionsType":"onPremise","info_text":"Nachricht Nr. 1 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = true, shipment = false, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe143-dbae-4dc0-bb7d-35d0cda8408e",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-06-12T20:45:29.624Z",
                        received = "2025-06-12T20:45:30Z",
                        textJson = """{"version":1,"supplyOptionsType":"onPremise","info_text":"Nachricht Nr. 0 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = true, shipment = false, delivery = false,
                        isDiga = false
                    )
                )

                // ---------- 1.4 replies (shipment) ----------
                add(
                    reply(
                        id = "01ebe107-82a2-c0a8-3f11-0e712dd4425b",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-06-09T20:45:37.657Z",
                        received = "2025-06-09T20:45:37Z",
                        textJson = """{"version":1,"supplyOptionsType":"shipment","info_text":"Nachricht Nr. 4 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = false, shipment = true, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe107-82a0-c8c0-5e22-0c99dfa42034",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-06-09T20:45:37.528Z",
                        received = "2025-06-09T20:45:37Z",
                        textJson = """{"version":1,"supplyOptionsType":"shipment","info_text":"Nachricht Nr. 3 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = false, shipment = true, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe107-829f-26c8-b463-140509a768f7",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-06-09T20:45:37.421Z",
                        received = "2025-06-09T20:45:37Z",
                        textJson = """{"version":1,"supplyOptionsType":"shipment","info_text":"Nachricht Nr. 2 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = false, shipment = true, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe107-829d-7530-8d36-808b8fd48336",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-06-09T20:45:37.310Z",
                        received = "2025-06-09T20:45:37Z",
                        textJson = """{"version":1,"supplyOptionsType":"shipment","info_text":"Nachricht Nr. 1 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = false, shipment = true, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebe107-829b-f660-768e-be11b5a84fed",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-06-09T20:45:37.212Z",
                        received = "2025-06-09T20:45:37Z",
                        textJson = """{"version":1,"supplyOptionsType":"shipment","info_text":"Nachricht Nr. 0 zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = false, shipment = true, delivery = false,
                        isDiga = false
                    )
                )

                // ---------- 1.4 pair (delivery) ----------
                add(
                    dispense(
                        id = "01ebe03e-5214-5b00-b427-e582121fe481",
                        profile = "1.4",
                        senderKvnr = v14Recipient,
                        recipientTi = v14Sender,
                        sent = "2025-05-30T20:43:54.592Z",
                        contentString = """{"version":1,"supplyOptionsType":"delivery","hint":"Nachricht Nr. {0} zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        supplyOptionsType = DispenseSupplyOptionsType.DELIVERY,
                        flowCode = "169",
                        flowDisplay = "Muster 16 (Direkte Zuweisung)",
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebdf75-2e3b-1a08-f71c-edf9f1941a25",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-05-20T20:45:44.725Z",
                        received = "2025-05-20T20:45:44Z",
                        textJson = """{"version":1,"supplyOptionsType":"delivery","info_text":"Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = false, shipment = false, delivery = true,
                        isDiga = false
                    )
                )
                add(
                    dispense(
                        id = "01ebdf75-2e31-32e0-e90a-e4e51c278c77",
                        profile = "1.4",
                        senderKvnr = v14Recipient,
                        recipientTi = v14Sender,
                        sent = "2025-05-20T20:45:44.076Z",
                        contentString = """{"version":1,"supplyOptionsType":"delivery","hint":"Nachricht Nr. {0} zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        supplyOptionsType = DispenseSupplyOptionsType.DELIVERY,
                        flowCode = "160",
                        flowDisplay = "Muster 16 (Apothekenpflichtige Arzneimittel)",
                        isDiga = false
                    )
                )

                // ---------- 1.4 pair (delivery) ----------
                add(
                    dispense(
                        id = "01ebde6f-a1f6-a888-da1a-88d5a0e30fda",
                        profile = "1.4",
                        senderKvnr = v14Recipient,
                        recipientTi = v14Sender,
                        sent = "2025-05-07T20:43:24.965Z",
                        contentString = """{"version":1,"supplyOptionsType":"delivery","hint":"Nachricht Nr. {0} zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        supplyOptionsType = DispenseSupplyOptionsType.DELIVERY,
                        flowCode = "160",
                        flowDisplay = "Muster 16 (Apothekenpflichtige Arzneimittel)",
                        isDiga = false
                    )
                )
                add(
                    reply(
                        id = "01ebddce-bd53-5a50-401f-132ced869880",
                        profile = "1.4",
                        senderTi = v14Sender,
                        recipientKvnr = v14Recipient,
                        sent = "2025-04-29T20:46:14.290Z",
                        received = "2025-04-29T20:46:14Z",
                        textJson = """{"version":1,"supplyOptionsType":"onPremise","info_text":"Hey patient, how are you? does the medicine takes an effect??"}""",
                        onPremise = true, shipment = false, delivery = false,
                        isDiga = false
                    )
                )
                add(
                    dispense(
                        id = "01ebddce-bd47-f458-8fa7-9326fdedf49f",
                        profile = "1.4",
                        senderKvnr = v14Recipient,
                        recipientTi = v14Sender,
                        sent = "2025-04-29T20:46:13.543Z",
                        contentString = """{"version":1,"supplyOptionsType":"onPremise","hint":"Nachricht Nr. {0} zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??"}""",
                        supplyOptionsType = DispenseSupplyOptionsType.ON_PREMISE,
                        flowCode = "169",
                        flowDisplay = "Muster 16 (Direkte Zuweisung)",
                        isDiga = false
                    )
                )

                // NOTE: The list above enumerates all entries from your sample in order.
                // If you later want to add/remove messages, keep `total` in sync with messages.size.
            }
        )
}
