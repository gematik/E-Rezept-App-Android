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

package de.gematik.ti.erp.app.database.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import de.gematik.ti.erp.app.database.room.v2.accesstoken.SearchAccessTokenDao
import de.gematik.ti.erp.app.database.room.v2.accesstoken.SearchAccessTokenEntity
import de.gematik.ti.erp.app.database.room.v2.invoice.InvoiceDao
import de.gematik.ti.erp.app.database.room.v2.invoice.InvoiceRoomEntity
import de.gematik.ti.erp.app.database.room.v2.pharmacy.PharmacyDao
import de.gematik.ti.erp.app.database.room.v2.pharmacy.PharmacyEntity
import de.gematik.ti.erp.app.database.room.v2.profile.ErpProfileEntity
import de.gematik.ti.erp.app.database.room.v2.profile.ProfileDao
import de.gematik.ti.erp.app.database.room.v2.settings.SettingsDao
import de.gematik.ti.erp.app.database.room.v2.settings.SettingsRoomEntity
import de.gematik.ti.erp.app.database.room.v2.shippinginfo.ShippingInfoDao
import de.gematik.ti.erp.app.database.room.v2.shippinginfo.ShippingInfoEntity
import de.gematik.ti.erp.app.database.room.v2.task.ErpAccidentInfoEntity
import de.gematik.ti.erp.app.database.room.v2.task.ErpChargeItemRoom
import de.gematik.ti.erp.app.database.room.v2.task.ErpMultiplePrescriptionEntity
import de.gematik.ti.erp.app.database.room.v2.task.ErpPatientEntity
import de.gematik.ti.erp.app.database.room.v2.task.ErpPractitionerEntity
import de.gematik.ti.erp.app.database.room.v2.task.ErpTaskDao
import de.gematik.ti.erp.app.database.room.v2.task.ErpTaskEntity
import de.gematik.ti.erp.app.database.room.v2.task.ErpTaskEntityMultiplePrescriptionEntity
import de.gematik.ti.erp.app.database.room.v2.task.ErpTaskMedicationDeviceRequestEntity
import de.gematik.ti.erp.app.database.room.v2.task.ErpTaskMultiplePrescriptionDao
import de.gematik.ti.erp.app.database.room.v2.task.ErpTaskWithRefsDao
import de.gematik.ti.erp.app.database.room.v2.task.communication.CommunicationDao
import de.gematik.ti.erp.app.database.room.v2.task.communication.ErpCommunicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpIngredientEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationDispenseEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpRatioEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.MedicationDispenseDao
import de.gematik.ti.erp.app.database.room.v2.task.organization.ErpOrganizationEntity
import de.gematik.ti.erp.app.database.room.v2.task.organization.OrganizationDao
import de.gematik.ti.erp.app.database.room.v2.task.util.InstantConverter
import de.gematik.ti.erp.app.database.room.v2.truststrore.TrustStoreDao
import de.gematik.ti.erp.app.database.room.v2.truststrore.TrustStoreEntity
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        ErpProfileEntity::class,
        PharmacyEntity::class,
        SettingsRoomEntity::class,
        InvoiceRoomEntity::class,
        ErpRatioEntity::class,
        ErpPatientEntity::class,
        ErpTaskEntity::class,
        ErpMedicationEntity::class,
        ErpOrganizationEntity::class,
        ErpAccidentInfoEntity::class,
        ErpChargeItemRoom::class,
        ErpCommunicationEntity::class,
        ErpIngredientEntity::class,
        ErpMedicationDispenseEntity::class,
        ErpMultiplePrescriptionEntity::class,
        ErpTaskMedicationDeviceRequestEntity::class,
        ErpPractitionerEntity::class,
        ErpTaskEntityMultiplePrescriptionEntity::class,
        TrustStoreEntity::class,
        SearchAccessTokenEntity::class,
        ShippingInfoEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    InstantConverter::class
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun pharmacyFavoriteDao(): PharmacyDao
    abstract fun settingsDao(): SettingsDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun taskDao(): ErpTaskDao
    abstract fun taskWithRefsDao(): ErpTaskWithRefsDao
    abstract fun communicationDao(): CommunicationDao
    abstract fun medicationDispenseDao(): MedicationDispenseDao
    abstract fun organizationDao(): OrganizationDao
    abstract fun trustStoreDao(): TrustStoreDao
    abstract fun searchAccessTokenDao(): SearchAccessTokenDao
    abstract fun shippingInfoDao(): ShippingInfoDao
    abstract fun erpTaskMultiplePrescriptionDao(): ErpTaskMultiplePrescriptionDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/**
 * Platform-agnostic helper that must use getRoomDatabase(builder) to construct the DB.
 */
expect fun buildAppDatabase(): AppDatabase

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
