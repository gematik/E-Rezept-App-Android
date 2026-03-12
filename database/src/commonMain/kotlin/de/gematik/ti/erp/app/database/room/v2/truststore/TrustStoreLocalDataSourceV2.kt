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
package de.gematik.ti.erp.app.database.room.v2.truststore

import de.gematik.ti.erp.app.database.api.TrustStoreLocalDataSource
import de.gematik.ti.erp.app.database.room.v2.truststrore.SINGLETON_ID
import de.gematik.ti.erp.app.database.room.v2.truststrore.TrustStoreDao
import de.gematik.ti.erp.app.database.room.v2.truststrore.TrustStoreEntity
import de.gematik.ti.erp.app.vau.model.TrustStoreErpModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TrustStoreLocalDataSourceV2(
    private val dao: TrustStoreDao
) : TrustStoreLocalDataSource {

    override suspend fun saveCertificateAndOcspLists(certListJson: String, ocspListJson: String) {
        val entity = TrustStoreEntity(
            id = SINGLETON_ID,
            certListJson = certListJson,
            ocspListJson = ocspListJson
        )
        dao.upsert(entity)
    }

    override fun loadUntrusted(): Flow<TrustStoreErpModel?> =
        dao.observe().map { entity ->
            entity?.let {
                TrustStoreErpModel(
                    certListJson = it.certListJson,
                    ocspListJson = it.ocspListJson
                )
            }
        }

    override suspend fun deleteAll() {
        dao.clear()
    }
}
