/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.fhir.communication.parser

import de.gematik.ti.erp.app.fhir.BundleParser
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirCommunicationBundleErpModel
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundle.Companion.getBundleEntries
import de.gematik.ti.erp.app.fhir.communication.FhirCommunicationVersions
import de.gematik.ti.erp.app.fhir.communication.model.original.CommunicationProfileType
import de.gematik.ti.erp.app.fhir.communication.model.original.FhirCommunicationResourceType.FhirCommunication
import de.gematik.ti.erp.app.fhir.communication.model.original.FhirCommunicationResourceType.FhirCommunication.Companion.getCommunication
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonElement

class CommunicationParser : BundleParser {

    override fun extract(bundle: JsonElement): FhirCommunicationBundleErpModel? {
        return runCatching {
            val entries = try {
                bundle.getBundleEntries()
            } catch (e: Exception) {
                Napier.e("Failed to parse input as a bundle", e)
                return@runCatching null
            }

            if (entries.isEmpty()) {
                Napier.w("Communication bundle contains no entries")
                return@runCatching FhirCommunicationBundleErpModel(total = 0, messages = emptyList())
            }

            val messages = entries.mapNotNull { it.resource.getCommunication() }
                .mapNotNull { mapCommunicationToErpModel(it) }

            FhirCommunicationBundleErpModel(total = messages.size, messages = messages)
        }.getOrNull()
    }

    private fun mapCommunicationToErpModel(communication: FhirCommunication): FhirCommunicationEntryErpModel? {
        val profileType = communication.getProfileType()
        val profileVersion = communication.getProfileVersion()

        return when (profileType) {
            CommunicationProfileType.REPLY -> {
                if (isSupportedReplyVersion(profileVersion)) {
                    communication.toReplyErpModel()
                } else {
                    Napier.w("Unsupported Reply Communication version: $profileVersion")
                    null
                }
            }

            CommunicationProfileType.DISPENSE -> {
                if (isSupportedDispenseVersion(profileVersion)) {
                    communication.toDispenseErpModel()
                } else {
                    Napier.w("Unsupported Dispense Communication version: $profileVersion")
                    null
                }
            }

            else -> {
                Napier.w("Unknown communication profile type: ${communication.meta?.profiles}")
                null
            }
        }
    }

    private fun isSupportedReplyVersion(version: String): Boolean {
        return FhirCommunicationVersions.SupportedCommunicationReplyVersions.entries.any { it.version == version }
    }

    private fun isSupportedDispenseVersion(version: String): Boolean {
        return FhirCommunicationVersions.SupportedCommunicationDispenseVersions.entries.any { it.version == version }
    }
}
