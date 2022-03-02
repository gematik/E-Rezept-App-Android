/*
 * Copyright (c) 2022 gematik GmbH
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

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.gematik.ti.erp.app.db.converter.CertificateConverter
import de.gematik.ti.erp.app.db.converter.DateConverter
import de.gematik.ti.erp.app.db.converter.ProfileColorsConverter
import de.gematik.ti.erp.app.db.converter.TruststoreConverter
import de.gematik.ti.erp.app.db.daos.ActiveProfileDao
import de.gematik.ti.erp.app.db.daos.AttestationDao
import de.gematik.ti.erp.app.db.daos.CommunicationDao
import de.gematik.ti.erp.app.db.daos.IdpAuthenticationDataDao
import de.gematik.ti.erp.app.db.daos.IdpConfigurationDao
import de.gematik.ti.erp.app.db.daos.ProfileDao
import de.gematik.ti.erp.app.db.daos.SettingsDao
import de.gematik.ti.erp.app.db.daos.ShippingContactDao
import de.gematik.ti.erp.app.db.daos.TaskDao
import de.gematik.ti.erp.app.db.daos.TruststoreDao
import de.gematik.ti.erp.app.db.entities.ActiveProfile
import de.gematik.ti.erp.app.db.entities.AuditEventSimple
import de.gematik.ti.erp.app.db.entities.Communication
import de.gematik.ti.erp.app.db.entities.IdpAuthenticationDataEntity
import de.gematik.ti.erp.app.db.entities.IdpConfiguration
import de.gematik.ti.erp.app.db.entities.LowDetailEventSimple
import de.gematik.ti.erp.app.db.entities.MedicationDispenseSimple
import de.gematik.ti.erp.app.db.entities.ProfileColorNames
import de.gematik.ti.erp.app.db.entities.ProfileEntity
import de.gematik.ti.erp.app.db.entities.SafetynetAttestationEntity
import de.gematik.ti.erp.app.db.entities.Settings
import de.gematik.ti.erp.app.db.entities.ShippingContactEntity
import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.db.entities.TaskStatus
import de.gematik.ti.erp.app.db.entities.TruststoreEntity
import de.gematik.ti.erp.app.settings.usecase.DEFAULT_PROFILE_NAME
import javax.inject.Singleton

const val DB_VERSION = 27

@Singleton
@Database(
    entities = [
        Task::class,
        AuditEventSimple::class,
        IdpConfiguration::class,
        IdpAuthenticationDataEntity::class,
        ProfileEntity::class,
        Settings::class,
        TruststoreEntity::class,
        Communication::class,
        LowDetailEventSimple::class,
        MedicationDispenseSimple::class,
        SafetynetAttestationEntity::class,
        ActiveProfile::class,
        ShippingContactEntity::class
    ],
    version = DB_VERSION,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 26, to = 27)]

)
@TypeConverters(
    DateConverter::class,
    TruststoreConverter::class,
    CertificateConverter::class,
    ProfileColorsConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun idpInfoDao(): IdpConfigurationDao
    abstract fun idpAuthDataDao(): IdpAuthenticationDataDao
    abstract fun settingsDao(): SettingsDao
    abstract fun profileDao(): ProfileDao
    abstract fun truststoreDao(): TruststoreDao
    abstract fun communicationsDao(): CommunicationDao
    abstract fun attestationDao(): AttestationDao
    abstract fun activeProfileDao(): ActiveProfileDao
    abstract fun shippingContactDao(): ShippingContactDao
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
        database.execSQL("INSERT INTO `activeProfile` (`id`, `profileName`) VALUES (0, '$DEFAULT_PROFILE_NAME')")
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
            """
                INSERT INTO `communications_new` (
                    `communicationId`,
                    `profile`,
                    `time`,
                    `taskId`,
                    `telematicsId`,
                    `kbvUserId`,
                    `payload`,
                    `consumed`
                ) SELECT
                    `communicationId`,
                    `profile`,
                    `time`,
                    `taskId`,
                    `telematicsId`,
                    `kbvUserId`,
                    `payload`,
                    `consumed`
                FROM communications WHERE taskId IN (SELECT taskId FROM tasks);
            """.trimIndent()
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

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE profiles ADD COLUMN `color` TEXT NOT NULL DEFAULT 'SPRING_GRAY'")
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS idpConfiguration")
        database.execSQL("CREATE TABLE IF NOT EXISTS `idpConfiguration` (`authorizationEndpoint` TEXT NOT NULL, `ssoEndpoint` TEXT NOT NULL, `tokenEndpoint` TEXT NOT NULL, `pairingEndpoint` TEXT NOT NULL, `authenticationEndpoint` TEXT NOT NULL, `pukIdpEncEndpoint` TEXT NOT NULL, `pukIdpSigEndpoint` TEXT NOT NULL, `certificate` BLOB NOT NULL, `expirationTimestamp` INTEGER NOT NULL, `issueTimestamp` INTEGER NOT NULL,  `externalAuthorizationIDsEndpoint` TEXT ,`thirdPartyAuthorizationEndpoint` TEXT ,`id` INTEGER NOT NULL, PRIMARY KEY(`id`))")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE profiles ADD COLUMN `lastAuthenticated` INTEGER")
        database.execSQL("ALTER TABLE idpAuthenticationDataEntity ADD COLUMN `singleSignOnTokenValidOn` INTEGER")
        database.execSQL("ALTER TABLE idpAuthenticationDataEntity ADD COLUMN `singleSignOnTokenExpiresOn` INTEGER")
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS auditEvents")
        database.execSQL("CREATE TABLE IF NOT EXISTS `auditEvents` (`id` TEXT NOT NULL, `locale` TEXT NOT NULL, `text` TEXT, `timestamp` TEXT NOT NULL, `taskId` TEXT NOT NULL, PRIMARY KEY(`id`, `locale`))")
    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("UPDATE tasks SET status='${TaskStatus.Other}' WHERE status IS NOT NULL")
    }
}

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS auditEvents")
        database.execSQL("CREATE TABLE IF NOT EXISTS `auditEvents` (`id` TEXT NOT NULL, `locale` TEXT NOT NULL, `profileName` TEXT NOT NULL, `text` TEXT, `timestamp` TEXT NOT NULL, `taskId` TEXT NOT NULL, PRIMARY KEY(`id`, `locale`), FOREIGN KEY(`profileName`) REFERENCES `profiles`(`name`) ON UPDATE CASCADE ON DELETE CASCADE )")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_auditEvents_profileName` ON `auditEvents` (`profileName`)")

        database.execSQL("ALTER TABLE profiles ADD COLUMN `lastAuditEventSynced` Text")
    }
}

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS auditEvents")
        database.execSQL("CREATE TABLE IF NOT EXISTS `auditEvents` (`id` TEXT NOT NULL, `locale` TEXT NOT NULL, `profileName` TEXT NOT NULL, `text` TEXT NOT NULL, `timestamp` TEXT NOT NULL, `taskId` TEXT NOT NULL, PRIMARY KEY(`id`, `locale`), FOREIGN KEY(`profileName`) REFERENCES `profiles`(`name`) ON UPDATE CASCADE ON DELETE CASCADE )")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_auditEvents_profileName` ON `auditEvents` (`profileName`)")
    }
}

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE profiles ADD COLUMN `lastTaskSynced` INTEGER")
    }
}

val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "DROP TABLE idpAuthenticationDataEntity"
        )

        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `idpAuthenticationDataEntity` (`profileName` TEXT NOT NULL, `singleSignOnToken` TEXT, `singleSignOnTokenScope` TEXT, `cardAccessNumber` TEXT, `healthCardCertificate` BLOB, `aliasOfSecureElementEntry` BLOB, `singleSignOnTokenValidOn` INTEGER, `singleSignOnTokenExpiresOn` INTEGER, PRIMARY KEY(`profileName`), FOREIGN KEY(`profileName`) REFERENCES `profiles`(`name`) ON UPDATE CASCADE ON DELETE CASCADE)"
        )
    }
}

val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `tasks_new` (`taskId` TEXT NOT NULL, `profileName` TEXT NOT NULL, `accessCode` TEXT, `lastModified` TEXT, `organization` TEXT, `medicationText` TEXT, `expiresOn` TEXT, `acceptUntil` TEXT, `authoredOn` TEXT, `status` TEXT, `scannedOn` TEXT, `scanSessionEnd` TEXT, `nrInScanSession` INTEGER, `scanSessionName` TEXT, `redeemedOn` TEXT, `rawKBVBundle` BLOB, PRIMARY KEY(`taskId`), FOREIGN KEY(`profileName`) REFERENCES `profiles`(`name`) ON UPDATE CASCADE ON DELETE CASCADE)",
        )
        database.execSQL(
            "INSERT INTO `tasks_new` (`taskId`, `profileName`, `accessCode`, `lastModified`, `organization`, `medicationText`, `expiresOn`, `acceptUntil`, `authoredOn`, `status`, `scannedOn`, `scanSessionEnd`, `nrInScanSession`, `scanSessionName`, `redeemedOn`, `rawKBVBundle`) select `taskId`, `profileName`, `accessCode`, `lastModified`, `organization`, `medicationText`, `expiresOn`, `acceptUntil`, `authoredOn`, `status`, `scannedOn`, `scanSessionEnd`, `nrInScanSession`, `scanSessionName`, `redeemedOn`, `rawKBVBundle` FROM `tasks`"
        )
        database.execSQL(
            "DROP table tasks"
        )
        database.execSQL(
            "ALTER TABLE tasks_new RENAME TO tasks"
        )

        database.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_profileName` ON `tasks` (`profileName`)")
    }
}

val MIGRATION_21_22 = object : Migration(21, 22) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "DROP TABLE idpAuthenticationDataEntity"
        )
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `idpAuthenticationDataEntity` (`profileName` TEXT NOT NULL, `singleSignOnToken` TEXT, `singleSignOnTokenScope` TEXT, `cardAccessNumber` TEXT, `healthCardCertificate` BLOB, `aliasOfSecureElementEntry` BLOB, `singleSignOnTokenValidOn` TEXT, `singleSignOnTokenExpiresOn` TEXT, PRIMARY KEY(`profileName`), FOREIGN KEY(`profileName`) REFERENCES `profiles`(`name`) ON UPDATE CASCADE ON DELETE CASCADE)"
        )
        database.execSQL(
            "DROP TABLE profiles"
        )
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `insuranceNumber` TEXT, `color` TEXT NOT NULL DEFAULT '${ProfileColorNames.SPRING_GRAY}', `lastAuthenticated` TEXT DEFAULT NULL, `lastAuditEventSynced` TEXT DEFAULT NULL, `lastTaskSynced` TEXT DEFAULT NULL)"
        )
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_profiles_name` ON `profiles` (`name`)")
        database.execSQL(
            "INSERT INTO `profiles` (`id`, `name`, `insuranceNumber`) VALUES(0, '', NULL)"
        )
    }
}

val MIGRATION_22_23 = object : Migration(22, 23) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE profiles ADD COLUMN `insurantName` TEXT")
        database.execSQL("ALTER TABLE profiles ADD COLUMN `insuranceName` TEXT")
        database.execSQL("ALTER TABLE profiles RENAME COLUMN insuranceNumber TO insuranceIdentifier")
    }
}

val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS idpConfiguration")
        database.execSQL("CREATE TABLE IF NOT EXISTS `idpConfiguration` (`authorizationEndpoint` TEXT NOT NULL, `ssoEndpoint` TEXT NOT NULL, `tokenEndpoint` TEXT NOT NULL, `pairingEndpoint` TEXT NOT NULL, `authenticationEndpoint` TEXT NOT NULL, `pukIdpEncEndpoint` TEXT NOT NULL, `pukIdpSigEndpoint` TEXT NOT NULL, `certificate` BLOB NOT NULL, `expirationTimestamp` TEXT NOT NULL, `issueTimestamp` TEXT NOT NULL,  `externalAuthorizationIDsEndpoint` TEXT ,`thirdPartyAuthorizationEndpoint` TEXT ,`id` INTEGER NOT NULL, PRIMARY KEY(`id`))")
    }
}

val MIGRATION_24_25 = object : Migration(24, 25) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE settings ADD COLUMN `dataProtectionVersionAccepted` TEXT NOT NULL DEFAULT '2021-10-15'")
    }
}

val MIGRATION_25_26 = object : Migration(25, 26) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "DROP TABLE profiles"
        )
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `insurantName` TEXT, `insuranceName` TEXT, `insuranceIdentifier` TEXT, `color` TEXT NOT NULL DEFAULT '${ProfileColorNames.SPRING_GRAY}', `lastAuthenticated` TEXT DEFAULT NULL, `lastAuditEventSynced` TEXT DEFAULT NULL, `lastTaskSynced` TEXT DEFAULT NULL)"
        )
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_profiles_name` ON `profiles` (`name`)")
        database.execSQL(
            "INSERT INTO `profiles` (`id`, `name`, `insuranceIdentifier`) VALUES(0, '$DEFAULT_PROFILE_NAME', NULL)"
        )
        database.execSQL("INSERT OR REPLACE INTO `activeProfile` (`id`, `profileName`) VALUES (0, '$DEFAULT_PROFILE_NAME')")
    }
}
