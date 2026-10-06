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

object RoomSchemaVersion {
    @RoomSchemaMigrations(
        RoomSchemaMigration(1, "Initial Room database schema"),
        RoomSchemaMigration(2, "Add ShippingInfoEntity, TrustStoreEntity, SearchAccessTokenEntity"),
        RoomSchemaMigration(3, "Add ErpTaskMedicationDeviceRequestEntity for DiGA support"),
        RoomSchemaMigration(
            4,
            "Fix patient table: make name/dob/insuranceIdentifier nullable, remove coverageType and insurance columns, add additionalAddressInformation"
        ),
        RoomSchemaMigration(5, "Add migration logic"),
        RoomSchemaMigration(6, "Add cascading foreign keys for profile deletion"),
        RoomSchemaMigration(7, "Reverse task foreign keys so task is the parent"),
        RoomSchemaMigration(8, "Add EuAccessCodeEntity, EuOrderEntity, EuTaskEventEntity for EU prescriptions"),
        RoomSchemaMigration(9, "Add teratogenicPrescription fields to ErpMedicationRequestEntity"),
        RoomSchemaMigration(10, "Add embedded medicationProfile to ErpMedicationEntity"),
        RoomSchemaMigration(11, "Force re-mapping of cached invoices and GKV medications"),
        RoomSchemaMigration(12, "Change payload column to structured CommunicationPayloadErpModel"),
        RoomSchemaMigration(13, "Normalize communications payload column nullability to match migrated Room data"),
        RoomSchemaMigration(14, "Add country column to ShippingInfoEntity")
    )
    const val ACTUAL = 14
}
