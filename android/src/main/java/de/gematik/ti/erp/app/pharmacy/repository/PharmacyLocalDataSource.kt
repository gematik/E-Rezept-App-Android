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

package de.gematik.ti.erp.app.pharmacy.repository

import de.gematik.ti.erp.app.db.entities.v1.SettingsEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.OftenUsedPharmacyEntityV1
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.db.toInstant
import de.gematik.ti.erp.app.db.toRealmInstant
import de.gematik.ti.erp.app.db.tryWrite
import de.gematik.ti.erp.app.pharmacy.model.OftenUsedPharmacyData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class PharmacyLocalDataSource @Inject constructor(
    private val realm: Realm
) {
    suspend fun deleteOftenUsedPharmacy(oftenUsedPharmacy: OftenUsedPharmacyData.OftenUsedPharmacy) =
        realm.tryWrite {
            queryFirst<OftenUsedPharmacyEntityV1>("telematikId = $0", oftenUsedPharmacy.telematikId)?.let { delete(it) }
        }

    fun loadOftenUsedPharmacies(): Flow<List<OftenUsedPharmacyData.OftenUsedPharmacy>> =
        realm.query<SettingsEntityV1>()
            .first()
            .asFlow()
            .map { profile ->
                profile.obj?.let {
                    it.oftenUsedPharmacies.map { pharmacyEntity ->
                        pharmacyEntity.toOftenUsedPharmacy()
                    }
                } ?: emptyList()
            }

    suspend fun saveOrUpdateOftenUsedPharmacy(pharmacy: PharmacyUseCaseData.Pharmacy) {
        realm.tryWrite<Unit> {
            queryFirst<OftenUsedPharmacyEntityV1>("telematikId = $0", pharmacy.telematikId)?.apply {
                this.lastUsed = Instant.now().toRealmInstant()
                this.usageCount += 1
            } ?: this.queryFirst<SettingsEntityV1>()?.let { settings ->
                settings.oftenUsedPharmacies += copyToRealm(pharmacy.toOftenUsedPharmacyEntityV1())
            }
        }
    }
}

fun PharmacyUseCaseData.Pharmacy.toOftenUsedPharmacyEntityV1() =
    OftenUsedPharmacyEntityV1().apply {
        this.address = this@toOftenUsedPharmacyEntityV1.removeLineBreaksFromAddress()
        this.lastUsed = Instant.now().toRealmInstant()
        this.pharmacyName = this@toOftenUsedPharmacyEntityV1.name
        this.usageCount = 1
        this.telematikId = this@toOftenUsedPharmacyEntityV1.telematikId
    }

fun OftenUsedPharmacyEntityV1.toOftenUsedPharmacy() =
    OftenUsedPharmacyData.OftenUsedPharmacy(
        lastUsed = this.lastUsed.toInstant(),
        usageCount = this.usageCount,
        telematikId = this.telematikId,
        pharmacyName = this.pharmacyName,
        address = this.address
    )
