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

package de.gematik.ti.erp.app.pharmacy.repository.datasource.local

import de.gematik.ti.erp.app.db.entities.v1.pharmacy.PharmacyRemoteDataSourceSelectionEntityV1
import de.gematik.ti.erp.app.db.updateOrCreate
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query

class PharmacyRemoteSelectorLocalDataSource(
    private val realm: Realm,
    private val isRelease: Boolean
) {

    fun getPharmacyVzdService(): PharmacyVzdService = if (isRelease) {
        PharmacyVzdService.APOVZD
    } else {
        realm.query<PharmacyRemoteDataSourceSelectionEntityV1>().first().find()
            ?.pharmacyServiceEnum ?: PharmacyVzdService.APOVZD
    }

    suspend fun updatePharmacyService(service: PharmacyVzdService) {
        realm.updateOrCreate(
            queryBlock = { query<PharmacyRemoteDataSourceSelectionEntityV1>().first().find() }
        ) {
            it.pharmacyService = service.name
        }
    }
}
