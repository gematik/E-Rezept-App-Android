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

package de.gematik.ti.erp.app.base.usecase

import de.gematik.ti.erp.app.changelogs.InAppMessageRepository
import de.gematik.ti.erp.app.db.entities.v1.InAppMessageEntity
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.messages.domain.model.InAppMessage
import de.gematik.ti.erp.app.messages.domain.repository.InAppLocalMessageRepository
import de.gematik.ti.erp.app.messages.mappers.toEntity
import de.gematik.ti.erp.app.utils.extensions.toFormattedFloat
import io.realm.kotlin.ext.toRealmList
import kotlinx.coroutines.flow.first

class UpdateInAppMessageUseCase(
    private val inAppMessageRepository: InAppMessageRepository,
    private val localMessageRepository: InAppLocalMessageRepository,
    private val buildConfigInformation: BuildConfigInformation
) {
    suspend operator fun invoke() {
        val versionWithRC = buildConfigInformation.versionName()
        val currentVersion = versionWithRC.toFormattedFloat() ?: 0.0f
        val lastUpdatedVersion = inAppMessageRepository.lastUpdatedVersion.first()?.toFormattedFloat() ?: 0.0f
        if ((lastUpdatedVersion + 1) < currentVersion) {
            val internalMessages = localMessageRepository.getInternalMessages().first()
            val newInternalMessage = internalMessages.filterMessagesEqualOrGreaterByVersion(lastUpdatedVersion)
            if (newInternalMessage.isNotEmpty()) {
                val inAppLastVersion = newInternalMessage.last().version
                val newInAppMessages: List<InAppMessageEntity> = newInternalMessage.map {
                    it.toEntity()
                }
                inAppMessageRepository.updateChangeLogs(newInAppMessages.toRealmList(), versionWithRC, inAppLastVersion)
            }
        }
    }
}

private fun List<InAppMessage>.filterMessagesEqualOrGreaterByVersion(currentVersion: Float): List<InAppMessage> {
    return this.filter { message ->
        (message.version.toFormattedFloat() ?: 0.0f) > currentVersion
    }
}
