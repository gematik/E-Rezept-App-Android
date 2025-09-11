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

package de.gematik.ti.erp.app.database.realm.v1.migrations

object SchemaVersion {
    @SchemaMigrations(
        SchemaMigration(43, "Add SearchAccessTokenEntityV1"),
        SchemaMigration(44, "Add DebugSettingsEntityV1"),
        SchemaMigration(46, "Add DeviceRequestEntityV1"),
        SchemaMigration(47, "Add identifierNumber to InsuranceInformationEntityV1"),
        SchemaMigration(48, "Add for InternalMessageEntity.counter removal"),
        SchemaMigration(49, "Add DeviceRequestEntityV1"),
        SchemaMigration(50, "Change DeviceRequestDispenseEntityV1"),
        SchemaMigration(51, "Change DeviceRequestDispenseEntityV1 to add modifiedDate"),
        SchemaMigration(52, "Change DeviceRequestDispenseEntityV1 to add sentOnDate"),
        SchemaMigration(53, "Change ProfileEntityV1 to add organizationIdentifier"),
        SchemaMigration(54, "Add AuthenticationTimeOut to AuthenticationEntityV1"),
        SchemaMigration(55, "Add isEuRedeemable to SyncedTaskEntityV1"),
        SchemaMigration(56, "MedicationReminder Refactoring"),
        SchemaMigration(
            57,
            "Rename isEuRedeemable to isEuRedeemableByProperties in SyncedTaskEntityV1 and " +
                "Add isEuRedeemableByPatientAuthorization to SyncedTaskEntityV1"
        ),
        SchemaMigration(
            58,
            "Added fax and telematikId to OrganizationEntityV1" +
                "Added additionalAddressInformation to AddressEntityV1"
        ),
        SchemaMigration(59, "Added zanr and telematikId to PractitionerEntityV1"),
        SchemaMigration(60, "Added medicationProfileType and medicationProfileVersion to MedicationEntityV1")

    )
    val ACTUAL = 61L
}
