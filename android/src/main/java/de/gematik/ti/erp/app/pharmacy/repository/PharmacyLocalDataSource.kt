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

import de.gematik.ti.erp.app.db.entities.v1.pharmacy.FavoritePharmacyEntityV1
import de.gematik.ti.erp.app.db.entities.v1.pharmacy.OftenUsedPharmacyEntityV1
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.db.toInstant
import de.gematik.ti.erp.app.db.toRealmInstant
import de.gematik.ti.erp.app.db.tryWrite
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class PharmacyLocalDataSource @Inject constructor(
    private val realm: Realm
) {
    suspend fun deleteOverviewPharmacy(overviewPharmacy: OverviewPharmacyData.OverviewPharmacy) {
        realm.tryWrite {
            queryFirst<OftenUsedPharmacyEntityV1>("telematikId = $0", overviewPharmacy.telematikId)?.let {
                delete(it)
            }
            queryFirst<FavoritePharmacyEntityV1>("telematikId = $0", overviewPharmacy.telematikId)?.let {
                delete(it)
            }
        }
    }

    fun loadOftenUsedPharmacies(): Flow<List<OverviewPharmacyData.OverviewPharmacy>> =
        realm.query<OftenUsedPharmacyEntityV1>().sort("lastUsed", Sort.DESCENDING).asFlow().map {
            it.list.map { pharmacy ->
                pharmacy.toOverviewPharmacy()
            }
        }

    suspend fun saveOrUpdateOftenUsedPharmacy(pharmacy: PharmacyUseCaseData.Pharmacy) {
        realm.tryWrite<Unit> {
            queryFirst<OftenUsedPharmacyEntityV1>("telematikId = $0", pharmacy.telematikId)?.apply {
                this.lastUsed = Instant.now().toRealmInstant()
                this.usageCount += 1
            } ?: copyToRealm(pharmacy.toOftenUsedPharmacyEntityV1())
        }
    }

    fun PharmacyUseCaseData.Pharmacy.toOftenUsedPharmacyEntityV1() =
        OftenUsedPharmacyEntityV1().apply {
            this.address = this@toOftenUsedPharmacyEntityV1.singleLineAddress()
            this.pharmacyName = this@toOftenUsedPharmacyEntityV1.name
            this.telematikId = this@toOftenUsedPharmacyEntityV1.telematikId
        }

    fun OftenUsedPharmacyEntityV1.toOverviewPharmacy() =
        OverviewPharmacyData.OverviewPharmacy(
            lastUsed = this.lastUsed.toInstant(),
            usageCount = this.usageCount,
            isFavorite = false,
            telematikId = this.telematikId,
            pharmacyName = this.pharmacyName,
            address = this.address
        )

    suspend fun deleteFavoritePharmacy(favoritePharmacy: PharmacyUseCaseData.Pharmacy) {
        realm.tryWrite {
            queryFirst<FavoritePharmacyEntityV1>("telematikId = $0", favoritePharmacy.telematikId)?.let { delete(it) }
        }
    }

    fun loadFavoritePharmacies(): Flow<List<OverviewPharmacyData.OverviewPharmacy>> =
        realm.query<FavoritePharmacyEntityV1>().sort("lastUsed", Sort.DESCENDING).asFlow().map {
            it.list.map { favorite ->
                favorite.toOverviewPharmacy()
            }
        }

    suspend fun saveOrUpdateFavoritePharmacy(pharmacy: PharmacyUseCaseData.Pharmacy) {
        realm.tryWrite<Unit> {
            queryFirst<FavoritePharmacyEntityV1>("telematikId = $0", pharmacy.telematikId)?.apply {
                this.lastUsed = Instant.now().toRealmInstant()
            } ?: copyToRealm(pharmacy.toFavoritePharmacyEntityV1())
        }
    }

    fun isPharmacyInFavorites(pharmacy: PharmacyUseCaseData.Pharmacy): Flow<Boolean> =
        realm.query<FavoritePharmacyEntityV1>("telematikId = $0", pharmacy.telematikId)
            .asFlow()
            .map {
                it.list.isNotEmpty()
            }

    fun PharmacyUseCaseData.Pharmacy.toFavoritePharmacyEntityV1() =
        FavoritePharmacyEntityV1().apply {
            this.address = this@toFavoritePharmacyEntityV1.singleLineAddress()
            this.pharmacyName = this@toFavoritePharmacyEntityV1.name
            this.telematikId = this@toFavoritePharmacyEntityV1.telematikId
        }

    fun FavoritePharmacyEntityV1.toOverviewPharmacy() =
        OverviewPharmacyData.OverviewPharmacy(
            lastUsed = this.lastUsed.toInstant(),
            telematikId = this.telematikId,
            pharmacyName = this.pharmacyName,
            address = this.address,
            isFavorite = true,
            usageCount = 0
        )
}
