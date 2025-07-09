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

package de.gematik.ti.erp.app.pharmacy.repository.datasource.local

import de.gematik.ti.erp.app.db.entities.v1.pharmacy.FavoritePharmacyEntityV1
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

class DefaultFavouritePharmacyLocalDataSource(private val realm: Realm) :
    FavouritePharmacyLocalDataSource {
    override suspend fun deleteFavoritePharmacy(favoritePharmacy: Pharmacy) {
        realm.tryWrite {
            queryFirst<FavoritePharmacyEntityV1>("telematikId = $0", favoritePharmacy.telematikId)?.let { delete(it) }
        }
    }

    override fun loadFavoritePharmacies(): Flow<List<OverviewPharmacy>> {
        return realm.query<FavoritePharmacyEntityV1>()
            .sort("lastUsed", Sort.DESCENDING)
            .asFlow()
            .map {
                it.list.map { favorite ->
                    favorite.toOverviewPharmacy()
                }
            }
    }

    override suspend fun markPharmacyAsFavourite(pharmacy: Pharmacy) {
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

    companion object {
        private fun Pharmacy.toFavoritePharmacyEntityV1() =
            FavoritePharmacyEntityV1().apply {
                this.address = this@toFavoritePharmacyEntityV1.singleLineAddress()
                this.pharmacyName = this@toFavoritePharmacyEntityV1.name
                this.telematikId = this@toFavoritePharmacyEntityV1.telematikId
                this.lastUsed = Clock.System.now().toRealmInstant()
            }

        private fun FavoritePharmacyEntityV1.toOverviewPharmacy() =
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
