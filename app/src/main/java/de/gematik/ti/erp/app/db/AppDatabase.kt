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
import de.gematik.ti.erp.app.db.daos.AttestationDao
import de.gematik.ti.erp.app.db.daos.CommunicationDao
import de.gematik.ti.erp.app.db.daos.HealthCardUserDao
import de.gematik.ti.erp.app.db.daos.IdpAuthenticationDataDao
import de.gematik.ti.erp.app.db.daos.IdpConfigurationDao
import de.gematik.ti.erp.app.db.daos.SettingsDao
import de.gematik.ti.erp.app.db.daos.TaskDao
import de.gematik.ti.erp.app.db.daos.TruststoreDao
import de.gematik.ti.erp.app.db.entities.AuditEventSimple
import de.gematik.ti.erp.app.db.entities.Communication
import de.gematik.ti.erp.app.db.entities.HealthCardUser
import de.gematik.ti.erp.app.db.entities.IdpAuthenticationDataEntity
import de.gematik.ti.erp.app.db.entities.IdpConfiguration
import de.gematik.ti.erp.app.db.entities.LowDetailEventSimple
import de.gematik.ti.erp.app.db.entities.MedicationDispenseSimple
import de.gematik.ti.erp.app.db.entities.SafetynetAttestationEntity
import de.gematik.ti.erp.app.db.entities.Settings
import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.db.entities.TruststoreEntity
import javax.inject.Singleton

const val DB_VERSION = 10

@Singleton
@Database(
    entities = [
        Task::class,
        AuditEventSimple::class,
        IdpConfiguration::class,
        IdpAuthenticationDataEntity::class,
        HealthCardUser::class,
        Settings::class,
        TruststoreEntity::class,
        Communication::class,
        LowDetailEventSimple::class,
        MedicationDispenseSimple::class,
        SafetynetAttestationEntity::class
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
    abstract fun healthCardUserDao(): HealthCardUserDao
    abstract fun truststoreDao(): TruststoreDao
    abstract fun communicationsDao(): CommunicationDao
    abstract fun attestationDao(): AttestationDao
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
