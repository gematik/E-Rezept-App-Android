/*
 * Copyright (c) 2024 gematik GmbH
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
import de.gematik.ti.erp.app.db.entities.v1.task.ScannedTaskEntityV1
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.db.toInstant
import de.gematik.ti.erp.app.db.toRealmInstant
import de.gematik.ti.erp.app.db.tryWrite
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData.OverviewPharmacy
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Pharmacy
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class DefaultPharmacyLocalDataSource(private val realm: Realm) : PharmacyLocalDataSource {
    override suspend fun deleteOverviewPharmacy(overviewPharmacy: OverviewPharmacy) {
        realm.tryWrite {
            queryFirst<OftenUsedPharmacyEntityV1>("telematikId = $0", overviewPharmacy.telematikId)?.let {
                delete(it)
            }
            queryFirst<FavoritePharmacyEntityV1>("telematikId = $0", overviewPharmacy.telematikId)?.let {
                delete(it)
            }
        }
    }

    override fun loadOftenUsedPharmacies(): Flow<List<OverviewPharmacy>> =
        realm.query<OftenUsedPharmacyEntityV1>().sort("lastUsed", Sort.DESCENDING).asFlow().map {
            it.list.map { pharmacy ->
                pharmacy.toOverviewPharmacy()
            }
        }

    override suspend fun saveOrUpdateOftenUsedPharmacy(pharmacy: Pharmacy) {
        realm.tryWrite<Unit> {
            queryFirst<OftenUsedPharmacyEntityV1>("telematikId = $0", pharmacy.telematikId)?.apply {
                this.lastUsed = Clock.System.now().toRealmInstant()
                this.usageCount += 1
            } ?: copyToRealm(pharmacy.toOftenUsedPharmacyEntityV1())
        }
    }

    override suspend fun deleteFavoritePharmacy(favoritePharmacy: Pharmacy) {
        realm.tryWrite {
            queryFirst<FavoritePharmacyEntityV1>("telematikId = $0", favoritePharmacy.telematikId)?.let { delete(it) }
        }
    }

    override fun loadFavoritePharmacies(): Flow<List<OverviewPharmacy>> =
        realm.query<FavoritePharmacyEntityV1>().sort("lastUsed", Sort.DESCENDING).asFlow().map {
            it.list.map { favorite ->
                favorite.toOverviewPharmacy()
            }
        }

    override suspend fun saveOrUpdateFavoritePharmacy(pharmacy: Pharmacy) {
        realm.tryWrite<Unit> {
            queryFirst<FavoritePharmacyEntityV1>("telematikId = $0", pharmacy.telematikId)?.apply {
                this.lastUsed = Clock.System.now().toRealmInstant()
            } ?: copyToRealm(pharmacy.toFavoritePharmacyEntityV1())
        }
    }

    override fun isPharmacyInFavorites(pharmacy: Pharmacy): Flow<Boolean> =
        realm.query<FavoritePharmacyEntityV1>("telematikId = $0", pharmacy.telematikId)
            .asFlow()
            .map {
                it.list.isNotEmpty()
            }

    override suspend fun markAsRedeemed(taskId: String) {
        realm.tryWrite<Unit> {
            queryFirst<ScannedTaskEntityV1>("taskId = $0", taskId)?.apply {
                this.redeemedOn = Clock.System.now().toRealmInstant()
            }
        }
    }

    companion object {
        fun Pharmacy.toOftenUsedPharmacyEntityV1() =
            OftenUsedPharmacyEntityV1().apply {
                this.address = this@toOftenUsedPharmacyEntityV1.singleLineAddress()
                this.pharmacyName = this@toOftenUsedPharmacyEntityV1.name
                this.telematikId = this@toOftenUsedPharmacyEntityV1.telematikId
            }

        fun OftenUsedPharmacyEntityV1.toOverviewPharmacy() =
            OverviewPharmacy(
                lastUsed = this.lastUsed.toInstant(),
                usageCount = this.usageCount,
                isFavorite = false,
                telematikId = this.telematikId,
                pharmacyName = this.pharmacyName,
                address = this.address
            )

        fun Pharmacy.toFavoritePharmacyEntityV1() =
            FavoritePharmacyEntityV1().apply {
                this.address = this@toFavoritePharmacyEntityV1.singleLineAddress()
                this.pharmacyName = this@toFavoritePharmacyEntityV1.name
                this.telematikId = this@toFavoritePharmacyEntityV1.telematikId
            }

        fun FavoritePharmacyEntityV1.toOverviewPharmacy() =
            OverviewPharmacy(
                lastUsed = this.lastUsed.toInstant(),
                telematikId = this.telematikId,
                pharmacyName = this.pharmacyName,
                address = this.address,
                isFavorite = true,
                usageCount = 0
            )
    }
}
