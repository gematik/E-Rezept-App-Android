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
package de.gematik.ti.erp.app.database.realm.v1.pharmacy

import de.gematik.ti.erp.app.database.api.PharmacyLocalDataSource
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.utils.tryWrite
import de.gematik.ti.erp.app.pharmacy.model.PharmacyAddressErpModel.Companion.toAddressErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyErpModel
import de.gematik.ti.erp.app.pharmacy.model.TelematikId
import io.github.aakira.napier.Napier
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

internal class PharmacyLocalDataSourceV1(private val realm: Realm) : PharmacyLocalDataSource {

    override fun loadPharmacies(): Flow<List<PharmacyErpModel>> = combine(
        loadFavoritePharmacies(),
        loadOftenUsedPharmacies()
    ) { favoritePharmacies, oftenUsedPharmacies ->
        // Favorite takes precedence on duplicates
        val favorites = favoritePharmacies.sortedByDescending { it.lastUsed }
        val oftenUsed = oftenUsedPharmacies
            .filter { oftenUsed -> favoritePharmacies.none { it.telematikId == oftenUsed.telematikId } }
            .sortedByDescending { it.lastUsed }

        favorites + oftenUsed
    }

    override fun getPharmacy(telematikId: TelematikId): Flow<PharmacyErpModel?> {
        return loadPharmacies().map { models ->
            models.firstOrNull { it.telematikId == telematikId.value }
        }
    }

    private fun loadFavoritePharmacies(): Flow<List<PharmacyErpModel>> = realm.query<FavoritePharmacyEntityV1>()
        .sort("lastUsed", Sort.DESCENDING)
        .asFlow()
        .map {
            it.list.map { favorite ->
                favorite.toErpModel()
            }
        }

    private fun loadOftenUsedPharmacies(): Flow<List<PharmacyErpModel>> =
        realm.query<OftenUsedPharmacyEntityV1>().sort("lastUsed", Sort.DESCENDING).asFlow().map {
            it.list.map { pharmacy ->
                pharmacy.toOverviewPharmacy()
            }
        }

    override suspend fun deletePharmacy(telematikId: TelematikId) {
        realm.tryWrite {
            queryFirst<FavoritePharmacyEntityV1>("telematikId = $0", telematikId.value)?.let { delete(it) }
            queryFirst<OftenUsedPharmacyEntityV1>("telematikId = $0", telematikId.value)?.let { delete(it) }
        }
    }

    override suspend fun deleteFavoritePharmacy(telematikId: TelematikId) {
        realm.tryWrite {
            queryFirst<FavoritePharmacyEntityV1>("telematikId = $0", telematikId.value)?.let { delete(it) }
            // If it exists also as often used, keep that entry; nothing else to do
        }
    }

    override suspend fun deleteOftenUsedPharmacy(telematikId: TelematikId) {
        realm.tryWrite {
            queryFirst<OftenUsedPharmacyEntityV1>("telematikId = $0", telematikId.value)?.let { delete(it) }
            // If it exists also as favorite, keep that entry; nothing else to do
        }
    }

    override suspend fun markPharmacyAsFavourite(pharmacy: PharmacyErpModel) {
        realm.tryWrite<Unit> {
            queryFirst<FavoritePharmacyEntityV1>("telematikId = $0", pharmacy.telematikId)?.apply {
                this.lastUsed = Clock.System.now().toRealmInstant()
            } ?: copyToRealm(pharmacy.toFavoritePharmacyEntityV1())
        }
    }

    override suspend fun markPharmacyAsOftenUsed(pharmacy: PharmacyErpModel) {
        realm.tryWrite<Unit> {
            queryFirst<OftenUsedPharmacyEntityV1>("telematikId = $0", pharmacy.telematikId)?.apply {
                this.lastUsed = Clock.System.now().toRealmInstant()
                this.usageCount += 1
            } ?: copyToRealm(pharmacy.toOftenUsedPharmacyEntityV1())
        }
    }

    override fun isPharmacyInFavorites(pharmacy: PharmacyErpModel): Flow<Boolean> =
        realm.query<FavoritePharmacyEntityV1>("telematikId = $0", pharmacy.telematikId)
            .asFlow()
            .map {
                it.list.isNotEmpty()
            }

    override fun isPharmacyOftenUsed(pharmacy: PharmacyErpModel): Flow<Boolean> =
        realm.query<OftenUsedPharmacyEntityV1>("telematikId = $0", pharmacy.telematikId)
            .asFlow()
            .map {
                it.list.isNotEmpty()
            }

    companion object {

        private fun PharmacyErpModel.toFavoritePharmacyEntityV1(): FavoritePharmacyEntityV1 {
            Napier.d("Pharmacy ${this.telematikId} is marked as Favorite")
            return FavoritePharmacyEntityV1().apply {
                this.address = this@toFavoritePharmacyEntityV1.singleLineAddress()
                this.pharmacyName = this@toFavoritePharmacyEntityV1.name
                this.telematikId = this@toFavoritePharmacyEntityV1.telematikId
                this.lastUsed = Clock.System.now().toRealmInstant()
            }
        }

        private fun FavoritePharmacyEntityV1.toErpModel() =
            PharmacyErpModel(
                lastUsed = this.lastUsed.toInstant(),
                telematikId = this.telematikId,
                name = this.pharmacyName,
                address = this.address.toAddressErpModel(),
                isFavorite = true,
                isOftenUsed = false,
                usageCount = 0,
                contact = null
            )

        fun PharmacyErpModel.toOftenUsedPharmacyEntityV1() =
            OftenUsedPharmacyEntityV1().apply {
                this.address = this@toOftenUsedPharmacyEntityV1.singleLineAddress()
                this.pharmacyName = this@toOftenUsedPharmacyEntityV1.name
                this.telematikId = this@toOftenUsedPharmacyEntityV1.telematikId
                this.lastUsed = Clock.System.now().toRealmInstant()
            }

        fun OftenUsedPharmacyEntityV1.toOverviewPharmacy() =
            PharmacyErpModel(
                lastUsed = this.lastUsed.toInstant(),
                usageCount = this.usageCount,
                isFavorite = false,
                isOftenUsed = true,
                telematikId = this.telematikId,
                name = this.pharmacyName,
                address = this.address.toAddressErpModel(),
                contact = null
            )
    }
}
