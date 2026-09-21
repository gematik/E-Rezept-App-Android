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

package de.gematik.ti.erp.app.messages.domain.usecase

import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import io.github.aakira.napier.Napier

/**
 * Resolves the display name of the pharmacy identified by [telematikId] for a given
 * communication ([communicationId]).
 *
 * Resolution order:
 * 1. Saved local pharmacy (favourites/often-used, already migrated to Room) via
 *    [PharmacyRepository.findLocalPharmacyByTelematikId].
 * 2. Backend lookup via [PharmacyRepository.searchPharmacyByTelematikId].
 *
 * Once resolved, the name is persisted back onto the communication record via
 * [CommunicationRepository.updatePharmacyName] so that subsequent loads read it directly
 * without needing another lookup.
 *
 * Returns `null` if the pharmacy name could not be resolved from either source.
 */
internal suspend fun resolvePharmacyName(
    pharmacyRepository: PharmacyRepository,
    communicationRepository: CommunicationRepository,
    communicationId: String,
    telematikId: String
): String? {
    if (telematikId.isBlank()) return null

    return try {
        val resolvedName = pharmacyRepository.findLocalPharmacyByTelematikId(telematikId)?.name
            ?: pharmacyRepository.searchPharmacyByTelematikId(telematikId)
                .getOrNull()
                ?.entries
                ?.firstOrNull()
                ?.name

        resolvedName?.let { name ->
            communicationRepository.updatePharmacyName(communicationId, name)
        }

        resolvedName
    } catch (e: Throwable) {
        Napier.e { "error on resolving pharmacy name for $telematikId: ${e.message}" }
        null
    }
}
