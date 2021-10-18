/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.gematik.ti.erp.app.db.converter.CertificateConverter
import de.gematik.ti.erp.app.db.converter.DateConverter
import de.gematik.ti.erp.app.db.converter.TruststoreConverter
import de.gematik.ti.erp.app.db.daos.ActiveProfileDao
import de.gematik.ti.erp.app.db.daos.AttestationDao
import de.gematik.ti.erp.app.db.daos.CommunicationDao
import de.gematik.ti.erp.app.db.daos.IdpAuthenticationDataDao
import de.gematik.ti.erp.app.db.daos.IdpConfigurationDao
import de.gematik.ti.erp.app.db.daos.SettingsDao
import de.gematik.ti.erp.app.db.daos.TaskDao
import de.gematik.ti.erp.app.db.daos.TruststoreDao
import de.gematik.ti.erp.app.db.entities.ActiveProfile
import de.gematik.ti.erp.app.db.entities.AuditEventSimple
import de.gematik.ti.erp.app.db.entities.Communication
import de.gematik.ti.erp.app.db.entities.IdpAuthenticationDataEntity
import de.gematik.ti.erp.app.db.entities.IdpConfiguration
import de.gematik.ti.erp.app.db.entities.LowDetailEventSimple
import de.gematik.ti.erp.app.db.entities.MedicationDispenseSimple
import de.gematik.ti.erp.app.db.entities.Profile
import de.gematik.ti.erp.app.db.entities.SafetynetAttestationEntity
import de.gematik.ti.erp.app.db.entities.Settings
import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.db.entities.TruststoreEntity
import javax.inject.Singleton

const val DB_VERSION = 11

@Singleton
@Database(
    entities = [
        Task::class,
        AuditEventSimple::class,
        IdpConfiguration::class,
        IdpAuthenticationDataEntity::class,
        Profile::class,
        Settings::class,
        TruststoreEntity::class,
        Communication::class,
        LowDetailEventSimple::class,
        MedicationDispenseSimple::class,
        SafetynetAttestationEntity::class,
        ActiveProfile::class
    ],
    version = DB_VERSION,
    exportSchema = true
)
@TypeConverters(
    DateConverter::class,
    TruststoreConverter::class,
    CertificateConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun idpInfoDao(): IdpConfigurationDao
    abstract fun idpAuthDataDao(): IdpAuthenticationDataDao
    abstract fun settingsDao(): SettingsDao
    abstract fun profileDao(): de.gematik.ti.erp.app.db.daos.ProfileDao
    abstract fun truststoreDao(): TruststoreDao
    abstract fun communicationsDao(): CommunicationDao
    abstract fun attestationDao(): AttestationDao
    abstract fun activeProfileDao(): ActiveProfileDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE tasks ADD COLUMN status TEXT")
        database.execSQL("CREATE TABLE IF NOT EXISTS `medicationDispense` (`taskId` TEXT NOT NULL, `patientIdentifier` TEXT NOT NULL, `uniqueIdentifier` TEXT NOT NULL, `wasSubstituted` INTEGER NOT NULL, `dosageInstruction` TEXT NOT NULL, `performer` TEXT NOT NULL, `whenHandedOver` TEXT NOT NULL, PRIMARY KEY(`taskId`))")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS idpConfiguration")
        database.execSQL("CREATE TABLE IF NOT EXISTS `idpConfiguration` (`authorizationEndpoint` TEXT NOT NULL, `ssoEndpoint` TEXT NOT NULL, `tokenEndpoint` TEXT NOT NULL, `pairingEndpoint` TEXT NOT NULL, `authenticationEndpoint` TEXT NOT NULL, `pukIdpEncEndpoint` TEXT NOT NULL, `pukIdpSigEndpoint` TEXT NOT NULL, `certificate` BLOB NOT NULL, `expirationTimestamp` INTEGER NOT NULL, `issueTimestamp` INTEGER NOT NULL, `id` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        database.execSQL("CREATE TABLE IF NOT EXISTS `idpAuthenticationDataEntity` (`singleSignOnToken` TEXT, `singleSignOnTokenScope` TEXT, `healthCardCertificate` BLOB, `aliasOfSecureElementEntry` BLOB, `id` INTEGER NOT NULL, PRIMARY KEY(`id`))")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE medicationDispense ADD COLUMN text TEXT")
        database.execSQL("ALTER TABLE medicationDispense ADD COLUMN type INT")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE `medicationDispense_new` (`taskId` TEXT NOT NULL, `patientIdentifier` TEXT NOT NULL, `uniqueIdentifier` TEXT NOT NULL, `wasSubstituted` INTEGER NOT NULL, `dosageInstruction` TEXT NOT NULL, `performer` TEXT NOT NULL, `whenHandedOver` TEXT NOT NULL, `text` TEXT, `type` TEXT, PRIMARY KEY(`taskId`))"
        )
        database.execSQL(
            "INSERT INTO `medicationDispense_new` (`taskId`, `patientIdentifier`, `uniqueIdentifier`, `wasSubstituted`, `dosageInstruction`, `performer`, `whenHandedOver`, `text`) SELECT `taskId`, `patientIdentifier`, `uniqueIdentifier`, `wasSubstituted`, `dosageInstruction`, `performer`, `whenHandedOver`, `text` FROM `medicationDispense`"
        )
        database.execSQL("DROP TABLE `medicationDispense`")
        database.execSQL("ALTER TABLE `medicationDispense_new` RENAME TO `medicationDispense`")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE settings ADD COLUMN password_salt BLOB")
        database.execSQL("ALTER TABLE settings ADD COLUMN password_hash BLOB")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE settings ADD COLUMN zoomEnabled INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE settings ADD COLUMN authenticationFails INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE settings ADD COLUMN userHasAcceptedInsecureDevice INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE settings ADD COLUMN `pharmacySearch_name` TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE settings ADD COLUMN `pharmacySearch_locationEnabled` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE settings ADD COLUMN `pharmacySearch_filterReady` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE settings ADD COLUMN `pharmacySearch_filterDeliveryService` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE settings ADD COLUMN `pharmacySearch_filterOnlineService` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE settings ADD COLUMN `pharmacySearch_filterOpenNow` INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `safetynetattestations` (`id` INTEGER NOT NULL, `jws` TEXT NOT NULL, ourNonce BLOB NOT NULL, PRIMARY KEY(`id`))")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE `healthCardUsers`")
        database.execSQL("CREATE TABLE IF NOT EXISTS `profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `insuranceNumber` TEXT)")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_profiles_name` ON `profiles` (`name`)")

        database.execSQL("CREATE TABLE IF NOT EXISTS `activeProfile` (`id` INTEGER NOT NULL, `profileName` TEXT NOT NULL, PRIMARY KEY(`id`))")

        // we could insert a dummy user here - since we might need one to not violate foreign key constraint - and then overwrite it's name and things except id later
        database.execSQL(
            "INSERT INTO `profiles` (`id`, `name`, `insuranceNumber`) VALUES(0, '', NULL)"
        )
        database.execSQL("INSERT INTO `activeProfile` (`id`, `profileName`) VALUES (0, '')")
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `tasks_new` (`taskId` TEXT NOT NULL, `profileName` TEXT NOT NULL DEFAULT '', `accessCode` TEXT NOT NULL, `lastModified` TEXT, `organization` TEXT, `medicationText` TEXT, `expiresOn` TEXT, `acceptUntil` TEXT, `authoredOn` TEXT, `status` TEXT, `scannedOn` TEXT, `scanSessionEnd` TEXT, `nrInScanSession` INTEGER, `scanSessionName` TEXT, `redeemedOn` TEXT, `rawKBVBundle` BLOB, PRIMARY KEY(`taskId`), FOREIGN KEY(`profileName`) REFERENCES `profiles`(`name`) ON UPDATE CASCADE ON DELETE CASCADE)",
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_profileName` ON `tasks_new` (`profileName`)")
        database.execSQL(
            "INSERT INTO `tasks_new` (`taskId`, `accessCode`, `lastModified`, `organization`, `medicationText`, `expiresOn`, `acceptUntil`, `authoredOn`, `status`, `scannedOn`, `scanSessionEnd`, `nrInScanSession`, `scanSessionName`, `redeemedOn`, `rawKBVBundle`) select `taskId`, `accessCode`, `lastModified`, `organization`, `medicationText`, `expiresOn`, `acceptUntil`, `authoredOn`, `status`, `scannedOn`, `scanSessionEnd`, `nrInScanSession`, `scanSessionName`, `redeemedOn`, `rawKBVBundle` FROM `tasks`"
        )
        database.execSQL(
            "DROP table tasks"
        )
        database.execSQL(
            "ALTER TABLE tasks_new RENAME TO tasks"
        )

        // migration of communications
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `communications_new` (`communicationId` TEXT NOT NULL, `profile` TEXT NOT NULL, `profileName` TEXT NOT NULL DEFAULT '', `time` TEXT NOT NULL, `taskId` TEXT NOT NULL, `telematicsId` TEXT NOT NULL, `kbvUserId` TEXT NOT NULL, `payload` TEXT, `consumed` INTEGER NOT NULL, PRIMARY KEY(`communicationId`), FOREIGN KEY(`taskId`) REFERENCES `tasks`(`taskId`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`profileName`) REFERENCES `profiles`(`name`) ON UPDATE CASCADE ON DELETE CASCADE )"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_communications_profileName` ON `communications_new` (`profileName`)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_communications_taskId` ON `communications_new` (`taskId`)"
        )

        database.execSQL(
            "INSERT INTO `communications_new` (`communicationId`, `profile`, `time`, `taskId`, `telematicsId`, `kbvUserId`, `payload`, `consumed`) select `communicationId`, `profile`, `time`, `taskId`, `telematicsId`, `kbvUserId`, `payload`, `consumed` FROM communications"
        )
        database.execSQL(
            "DROP TABLE communications"
        )
        database.execSQL(
            "ALTER TABLE communications_new RENAME TO communications"
        )

        // migration of idpAuthenticationDataEntity (adds foreign key profileName, adds CAN
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `idpAuthenticationDataEntity_new` (`profileName` TEXT NOT NULL DEFAULT '', `singleSignOnToken` TEXT, `singleSignOnTokenScope` TEXT, `cardAccessNumber` TEXT, `healthCardCertificate` BLOB, `aliasOfSecureElementEntry` BLOB, `id` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`profileName`) REFERENCES `profiles`(`name`) ON UPDATE CASCADE ON DELETE CASCADE)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_idpAuthenticationDataEntity_profileName` ON `idpAuthenticationDataEntity_new` (`profileName`)"
        )
        database.execSQL(
            "INSERT INTO `idpAuthenticationDataEntity_new` (`id`,`singleSignOnToken`, `singleSignOnTokenScope`, `healthCardCertificate`, `aliasOfSecureElementEntry`) select `id`,`singleSignOnToken`, `singleSignOnTokenScope`, `healthCardCertificate`, `aliasOfSecureElementEntry` from `idpAuthenticationDataEntity`"
        )
        database.execSQL(
            "DROP TABLE idpAuthenticationDataEntity"
        )
        database.execSQL(
            "ALTER TABLE idpAuthenticationDataEntity_new RENAME TO idpAuthenticationDataEntity"
        )
    }
}
