/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.orders.repository

import de.gematik.ti.erp.app.db.entities.v1.PharmacyCacheEntityV1
import de.gematik.ti.erp.app.db.queryFirst
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class PharmacyCacheLocalDataSource(
    private val realm: Realm
) {
    fun loadPharmacies() =
        realm
            .query<PharmacyCacheEntityV1>()
            .asFlow()
            .map { result ->
                result.list.map {
                    it.toCachedPharmacy()
                }
            }
            .distinctUntilChanged()

    suspend fun savePharmacy(telematikId: String, name: String) {
        realm.write<Unit> {
            realm.queryFirst<PharmacyCacheEntityV1>("telematikId = $0", telematikId)?.apply {
                this.name = name
            } ?: run {
                copyToRealm(
                    PharmacyCacheEntityV1().apply {
                        this.telematikId = telematikId
                        this.name = name
                    }
                )
            }
        }
    }
}

data class CachedPharmacy(
    val name: String,
    val telematikId: String
)

fun PharmacyCacheEntityV1.toCachedPharmacy() =
    CachedPharmacy(
        name = this.name,
        telematikId = this.telematikId
    )
