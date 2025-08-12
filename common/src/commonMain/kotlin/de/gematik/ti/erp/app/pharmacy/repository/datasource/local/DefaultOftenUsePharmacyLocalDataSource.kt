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

import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.utils.tryWrite
import de.gematik.ti.erp.app.database.realm.v1.pharmacy.FavoritePharmacyEntityV1
import de.gematik.ti.erp.app.database.realm.v1.pharmacy.OftenUsedPharmacyEntityV1
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData.OverviewPharmacy
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Pharmacy
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class DefaultOftenUsePharmacyLocalDataSource(private val realm: Realm) :
    OftenUsedPharmacyLocalDataSource {

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

    override suspend fun markPharmacyAsOftenUsed(pharmacy: Pharmacy) {
        realm.tryWrite<Unit> {
            queryFirst<OftenUsedPharmacyEntityV1>("telematikId = $0", pharmacy.telematikId)?.apply {
                this.lastUsed = Clock.System.now().toRealmInstant()
                this.usageCount += 1
            } ?: copyToRealm(pharmacy.toOftenUsedPharmacyEntityV1())
        }
    }

    companion object {
        fun Pharmacy.toOftenUsedPharmacyEntityV1() =
            OftenUsedPharmacyEntityV1().apply {
                this.address = this@toOftenUsedPharmacyEntityV1.singleLineAddress()
                this.pharmacyName = this@toOftenUsedPharmacyEntityV1.name
                this.telematikId = this@toOftenUsedPharmacyEntityV1.telematikId
                this.lastUsed = Clock.System.now().toRealmInstant()
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
    }
}
