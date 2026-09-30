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
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.database.room.v2.accesstoken.SearchAccessTokenDao
import de.gematik.ti.erp.app.database.room.v2.accesstoken.SearchAccessTokenEntity
import de.gematik.ti.erp.app.database.room.v2.euredeem.EuAccessCodeEntity
import de.gematik.ti.erp.app.database.room.v2.euredeem.EuOrderDao
import de.gematik.ti.erp.app.database.room.v2.euredeem.EuOrderEntity
import de.gematik.ti.erp.app.database.room.v2.euredeem.EuTaskEventEntity
import de.gematik.ti.erp.app.database.room.v2.idp.IdpConfigurationDao
import de.gematik.ti.erp.app.database.room.v2.idp.IdpConfigurationEntity
import de.gematik.ti.erp.app.database.room.v2.internalmessage.InternalMessageDao
import de.gematik.ti.erp.app.database.room.v2.internalmessage.InternalMessageRoomEntity
import de.gematik.ti.erp.app.database.room.v2.invoice.InvoiceDao
import de.gematik.ti.erp.app.database.room.v2.invoice.InvoiceRoomEntity
import de.gematik.ti.erp.app.database.room.v2.medicationplan.MedicationPlanDao
import de.gematik.ti.erp.app.database.room.v2.medicationplan.MedicationScheduleEntity
import de.gematik.ti.erp.app.database.room.v2.medicationplan.MedicationScheduleNotificationEntity
import de.gematik.ti.erp.app.database.room.v2.pharmacy.PharmacyDao
import de.gematik.ti.erp.app.database.room.v2.pharmacy.PharmacyEntity
import de.gematik.ti.erp.app.database.room.v2.profile.ProfileDao
import de.gematik.ti.erp.app.database.room.v2.profile.ProfileEntity
import de.gematik.ti.erp.app.database.room.v2.shippinginfo.ShippingInfoDao
import de.gematik.ti.erp.app.database.room.v2.shippinginfo.ShippingInfoEntity
import de.gematik.ti.erp.app.database.room.v2.task.accident.ErpAccidentInfoEntity
import de.gematik.ti.erp.app.database.room.v2.task.chargeitem.ErpChargeItemRoom
import de.gematik.ti.erp.app.database.room.v2.task.communication.CommunicationDao
import de.gematik.ti.erp.app.database.room.v2.task.communication.ErpCommunicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.diga.ErpTaskMedicationDeviceRequestEntity
import de.gematik.ti.erp.app.database.room.v2.task.insuranceinformation.ErpInsuranceInformationEntity
import de.gematik.ti.erp.app.database.room.v2.task.insuranceinformation.InsuranceInformationDao
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpIngredientEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationDispenseEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationWithRefsDao
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpRatioEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.IngredientDao
import de.gematik.ti.erp.app.database.room.v2.task.medication.MedicationDispenseDao
import de.gematik.ti.erp.app.database.room.v2.task.medicationrequest.ErpMedicationRequestEntity
import de.gematik.ti.erp.app.database.room.v2.task.medicationrequest.MedicationRequestDao
import de.gematik.ti.erp.app.database.room.v2.task.multipleprescription.ErpMultiplePrescriptionEntity
import de.gematik.ti.erp.app.database.room.v2.task.multipleprescription.ErpTaskMultiplePrescriptionDao
import de.gematik.ti.erp.app.database.room.v2.task.organization.ErpOrganizationEntity
import de.gematik.ti.erp.app.database.room.v2.task.organization.OrganizationDao
import de.gematik.ti.erp.app.database.room.v2.task.patient.ErpPatientEntity
import de.gematik.ti.erp.app.database.room.v2.task.practitioner.ErpPractitionerEntity
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskDao
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskEntity
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskWithRefsDao
import de.gematik.ti.erp.app.database.room.v2.task.util.InstantConverter
import de.gematik.ti.erp.app.database.room.v2.task.util.IntSetConverter
import de.gematik.ti.erp.app.database.room.v2.task.util.LocalTimeConverter
import de.gematik.ti.erp.app.database.room.v2.truststore.TrustStoreDao
import de.gematik.ti.erp.app.database.room.v2.truststore.TrustStoreEntity
import de.gematik.ti.erp.app.database.room.v2.userAuthentication.UserAuthenticationDao
import de.gematik.ti.erp.app.database.room.v2.userAuthentication.UserAuthenticationEntity
import kotlinx.coroutines.Dispatchers

@Requirement(
    "O.Source_2#14",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "The list of the model classes for the typed-safe databases.",
    codeLines = 22
)
@Database(
    entities = [
        ProfileEntity::class,
        PharmacyEntity::class,
        InvoiceRoomEntity::class,
        ErpRatioEntity::class,
        ErpPatientEntity::class,
        ErpTaskEntity::class,
        ErpMedicationEntity::class,
        ErpOrganizationEntity::class,
        ErpAccidentInfoEntity::class,
        ErpInsuranceInformationEntity::class,
        ErpMedicationRequestEntity::class,
        ErpChargeItemRoom::class,
        ErpCommunicationEntity::class,
        ErpIngredientEntity::class,
        ErpMedicationDispenseEntity::class,
        ErpMultiplePrescriptionEntity::class,
        ErpTaskMedicationDeviceRequestEntity::class,
        ErpPractitionerEntity::class,
        TrustStoreEntity::class,
        SearchAccessTokenEntity::class,
        ShippingInfoEntity::class,
        UserAuthenticationEntity::class,
        IdpConfigurationEntity::class,
        MedicationScheduleEntity::class,
        MedicationScheduleNotificationEntity::class,
        InternalMessageRoomEntity::class,
        EuAccessCodeEntity::class,
        EuOrderEntity::class,
        EuTaskEventEntity::class
    ],
    version = RoomSchemaVersion.ACTUAL,
    exportSchema = true
)
@TypeConverters(
    InstantConverter::class,
    IntSetConverter::class,
    LocalTimeConverter::class
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun pharmacyFavoriteDao(): PharmacyDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun taskDao(): ErpTaskDao
    abstract fun taskWithRefsDao(): ErpTaskWithRefsDao
    abstract fun communicationDao(): CommunicationDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun medicationDispenseDao(): MedicationDispenseDao
    abstract fun organizationDao(): OrganizationDao
    abstract fun trustStoreDao(): TrustStoreDao
    abstract fun searchAccessTokenDao(): SearchAccessTokenDao
    abstract fun shippingInfoDao(): ShippingInfoDao
    abstract fun insuranceInformationDao(): InsuranceInformationDao
    abstract fun medicationRequestDao(): MedicationRequestDao
    abstract fun medicationWithRefsDao(): ErpMedicationWithRefsDao
    abstract fun euOrderDao(): EuOrderDao
    abstract fun internalMessageDao(): InternalMessageDao
    abstract fun medicationPlanDao(): MedicationPlanDao
    abstract fun erpTaskMultiplePrescriptionDao(): ErpTaskMultiplePrescriptionDao
    abstract fun idpConfigurationDao(): IdpConfigurationDao
    abstract fun userAuthenticationDao(): UserAuthenticationDao
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
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .build()
}
