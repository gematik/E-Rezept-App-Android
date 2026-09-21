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

package de.gematik.ti.erp.app.database.datastore.debug.versions.eu

import androidx.datastore.core.DataStore
import de.gematik.ti.erp.app.database.api.debug.EuVersionLocalDataSource
import de.gematik.ti.erp.app.debug.model.EuVersion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val EU_VERSION_DATA_STORE = "EuVersionDataStore"

class EuVersionLocalDataStore(
    private val dataStore: DataStore<EuVersionEntitySchema>
) : EuVersionLocalDataSource {

    override val euVersion: Flow<EuVersion> = dataStore.data
        .map { schema ->
            runCatching { EuVersion.valueOf(schema.entity.euVersion) }
                .getOrDefault(EuVersion.V_1_1)
        }

    override suspend fun saveEuVersion(euVersion: EuVersion) {
        dataStore.updateData { schema ->
            schema.copy(entity = schema.entity.copy(euVersion = euVersion.name))
        }
    }
}
