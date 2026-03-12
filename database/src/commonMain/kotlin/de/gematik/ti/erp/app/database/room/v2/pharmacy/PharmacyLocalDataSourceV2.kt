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

package de.gematik.ti.erp.app.database.room.v2.pharmacy

import de.gematik.ti.erp.app.database.api.PharmacyLocalDataSource
import de.gematik.ti.erp.app.pharmacy.model.ContactInformationErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyAddressErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyErpModel
import de.gematik.ti.erp.app.pharmacy.model.PositionErpModel
import de.gematik.ti.erp.app.pharmacy.model.TelematikId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class PharmacyLocalDataSourceV2(
    private val dao: PharmacyDao
) : PharmacyLocalDataSource {

    override fun loadPharmacies(): Flow<List<PharmacyErpModel>> =
        dao.observePharmacy()
            .map { entities ->
                val models = entities.map { it.toErpModel() }
                val favorites = models.filter { it.isFavorite }
                    .sortedByDescending { it.lastUsed }
                val oftenUsed = models.filter { it.isOftenUsed && !it.isFavorite }
                    .sortedByDescending { it.lastUsed }

                favorites + oftenUsed
            }

    override fun getPharmacy(telematikId: TelematikId): Flow<PharmacyErpModel?> {
        return loadPharmacies().map { models ->
            models.firstOrNull { it.telematikId == telematikId.value }
        }
    }

    override suspend fun deletePharmacy(telematikId: TelematikId) {
        dao.deleteById(telematikId.value)
    }

    override suspend fun deleteFavoritePharmacy(telematikId: TelematikId) {
        val existing = dao.getPharmacyById(telematikId.value) ?: return
        if (existing.isOftenUsed) {
            // demark favorite only
            dao.upsert(existing.copy(isFavourite = false))
        } else {
            // both flags would be false -> delete
            dao.deleteById(telematikId.value)
        }
    }

    override suspend fun deleteOftenUsedPharmacy(telematikId: TelematikId) {
        val existing = dao.getPharmacyById(telematikId.value) ?: return
        if (existing.isFavourite) {
            // demark often used only
            dao.upsert(existing.copy(isOftenUsed = false))
        } else {
            // both flags would be false -> delete
            dao.deleteById(telematikId.value)
        }
    }

    override suspend fun markPharmacyAsFavourite(pharmacy: PharmacyErpModel) {
        // Preserve existing fields (like usage count), set favourite=true and update lastUsed
        val existing = dao.getPharmacyById(pharmacy.telematikId)
        val mergedOftenUsed = (existing?.isOftenUsed == true) || pharmacy.isOftenUsed

        val base = existing ?: pharmacy.toEntity(isFavourite = true, isOftenUsed = mergedOftenUsed)
        val updated = base.copy(
            isFavourite = true,
            isOftenUsed = mergedOftenUsed,
            lastUsed = Clock.System.now()
        )
        dao.upsert(updated)
    }

    override suspend fun markPharmacyAsOftenUsed(pharmacy: PharmacyErpModel) {
        // Preserve existing fields; set oftenUsed=true and increment usage exactly once via DAO
        val existing = dao.getPharmacyById(pharmacy.telematikId)
        val mergedFavourite = (existing?.isFavourite == true) || pharmacy.isFavorite

        val base = existing ?: pharmacy.toEntity(isFavourite = mergedFavourite, isOftenUsed = true)
        val updated = base.copy(
            isFavourite = mergedFavourite,
            isOftenUsed = true,
            lastUsed = Clock.System.now()
        )
        dao.upsert(updated)
        // Let DAO handle the single increment of usage and update of lastUsed
        dao.markUsed(updated.id)
    }

    override fun isPharmacyInFavorites(pharmacy: PharmacyErpModel): Flow<Boolean> = dao.observeIsFavourite(id = pharmacy.telematikId)

    override fun isPharmacyOftenUsed(pharmacy: PharmacyErpModel): Flow<Boolean> = dao.observeIsOftenUsed(id = pharmacy.telematikId)

    private fun PharmacyEntity.toErpModel(): PharmacyErpModel =
        PharmacyErpModel(
            lastUsed = this.lastUsed ?: Instant.DISTANT_PAST,
            isFavorite = this.isFavourite,
            isOftenUsed = this.isOftenUsed,
            usageCount = this.countUsage,
            telematikId = this.id,
            name = this.name,
            address = PharmacyAddressErpModel(
                lineAddress = this.lineAddress,
                zip = this.zip,
                city = this.city
            ),
            position =
            if (this.latitude != 0.0 && this.longitude != 0.0) PositionErpModel(this.latitude, this.longitude) else null,
            contact = ContactInformationErpModel(
                phone = this.phone ?: "",
                mail = this.email ?: "",
                url = this.web ?: ""
            )
        )

    private fun PharmacyErpModel.toEntity(isFavourite: Boolean = false, isOftenUsed: Boolean = false): PharmacyEntity {
        return PharmacyEntity(
            id = this.telematikId, // use telematikId as stable primary key
            name = this.name,
            lineAddress = address?.lineAddress ?: "",
            city = address?.city ?: "",
            zip = address?.zip ?: "",
            latitude = position?.latitude ?: 0.0,
            longitude = position?.longitude ?: 0.0,
            phone = contact?.phone ?: "",
            fax = "", // TODO: Read fax from FHIR
            email = contact?.mail ?: "",
            web = contact?.url ?: "",
            imagePath = null,
            countUsage = this.usageCount,
            isFavourite = isFavourite,
            isOftenUsed = isOftenUsed,
            created = Clock.System.now(),
            lastUsed = this.lastUsed
        )
    }
}
