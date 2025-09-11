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

package de.gematik.ti.erp.app.data

// fhir-communication
val ReplyCommTestBundleV1_4 by lazy { getResourceAsString("/fhir/communications_parser/communications_reply_bundle_version_1_4.json") }
val DispenseCommTestBundleV1_4 by lazy { getResourceAsString("/fhir/communications_parser/communications_dispense_bundle_version_1_4.json") }
val MixedBundleV1_4Json by lazy { getResourceAsString("/fhir/communications_parser/communications_mix_bundle_version_1_4.json") }
val MixedBundleV1_2Json by lazy { getResourceAsString("/fhir/communications_parser/communications_mix_bundle_version_1_2.json") }
val SingleReplyCommV1_4 by lazy { getResourceAsString("/fhir/communications_parser/communications_reply_version_1_4.json") }
val SingleReplyCommV1_3 by lazy { getResourceAsString("/fhir/communications_parser/communications_reply_version_1_3.json") }
val SingleReplyCommV1_2 by lazy { getResourceAsString("/fhir/communications_parser/communications_reply_version_1_2.json") }
val SingleDispenseCommV1_4 by lazy { getResourceAsString("/fhir/communications_parser/communications_dispense_version_1_4.json") }
val SingleDispenseCommV1_2 by lazy { getResourceAsString("/fhir/communications_parser/communications_dispense_version_1_2.json") }
val communication_diga_dispense_1_4 by lazy { getResourceAsString("/fhir/communications_parser/communication_diga_dispense_1_4.json") }

// 1.5 versions
val communication_dispense_with_payload_1_5 by lazy { getResourceAsString("/fhir/communications_parser/communication_dispense_with_payload_version_1_5.json") }
val communication_dispense_1_5 by lazy { getResourceAsString("/fhir/communications_parser/communication_dispense_version_1_5.json") }
val communication_diga_dispense_1_5 by lazy { getResourceAsString("/fhir/communications_parser/communication_diga_dispense_1_5.json") }
val communication_reply_1_5 by lazy { getResourceAsString("/fhir/communications_parser/communication_reply_version_1_5.json") }
val communication_reply_bundle_1_5 by lazy { getResourceAsString("/fhir/communications_parser/communication_reply_bundle_version_1_5.json") }
