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

package de.gematik.ti.erp.app.changelogs

import de.gematik.ti.erp.app.db.entities.v1.InAppMessageEntity
import io.realm.kotlin.types.RealmList
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

class DefaultInAppMessageRepository(
    private val inAppDataSource: InAppDataSource
) : InAppMessageRepository {
    override val inAppMessages: Flow<List<InAppMessageEntity>> =
        inAppDataSource.changeLogs

    override val counter: Flow<Long> =
        inAppDataSource.counter

    override val lastVersion: Flow<String?> =
        inAppDataSource.lastVersion

    override val lastUpdatedVersion: Flow<String?> =
        inAppDataSource.lastUpdatedVersion

    override val showWelcomeMessage: Flow<Boolean> =
        inAppDataSource.showWelcomeMessage

    override val welcomeMessageTimeStamp: Flow<Instant>
        get() = inAppDataSource.welcomeMessageTimeStamp

    override suspend fun setInternalMessageAsRead() =
        inAppDataSource.setInternalMessageAsRead()

    override suspend fun setShowWelcomeMessage() =
        inAppDataSource.setShowWelcomeMessage()

    override suspend fun updateChangeLogs(newChangeLogs: RealmList<InAppMessageEntity>, lastVersion: String, inAppLastVersion: String) =
        inAppDataSource.updateChangeLogs(newChangeLogs, lastVersion, inAppLastVersion)
}
