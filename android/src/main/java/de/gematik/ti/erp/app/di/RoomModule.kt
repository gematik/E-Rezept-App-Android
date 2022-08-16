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

package de.gematik.ti.erp.app.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import de.gematik.ti.erp.app.MessageConversionException
import de.gematik.ti.erp.app.db.AppDatabase
import de.gematik.ti.erp.app.db.MIGRATION_10_11
import de.gematik.ti.erp.app.db.MIGRATION_11_12
import de.gematik.ti.erp.app.db.MIGRATION_12_13
import de.gematik.ti.erp.app.db.MIGRATION_13_14
import de.gematik.ti.erp.app.db.MIGRATION_14_15
import de.gematik.ti.erp.app.db.MIGRATION_15_16
import de.gematik.ti.erp.app.db.MIGRATION_16_17
import de.gematik.ti.erp.app.db.MIGRATION_17_18
import de.gematik.ti.erp.app.db.MIGRATION_18_19
import de.gematik.ti.erp.app.db.MIGRATION_19_20
import de.gematik.ti.erp.app.db.MIGRATION_1_2
import de.gematik.ti.erp.app.db.MIGRATION_20_21
import de.gematik.ti.erp.app.db.MIGRATION_21_22
import de.gematik.ti.erp.app.db.MIGRATION_22_23
import de.gematik.ti.erp.app.db.MIGRATION_23_24
import de.gematik.ti.erp.app.db.MIGRATION_24_25
import de.gematik.ti.erp.app.db.MIGRATION_25_26
import de.gematik.ti.erp.app.db.MIGRATION_27_28
import de.gematik.ti.erp.app.db.MIGRATION_2_3
import de.gematik.ti.erp.app.db.MIGRATION_3_4
import de.gematik.ti.erp.app.db.MIGRATION_4_5
import de.gematik.ti.erp.app.db.MIGRATION_5_6
import de.gematik.ti.erp.app.db.MIGRATION_6_7
import de.gematik.ti.erp.app.db.MIGRATION_7_8
import de.gematik.ti.erp.app.db.MIGRATION_8_9
import de.gematik.ti.erp.app.db.MIGRATION_9_10
import de.gematik.ti.erp.app.db.entities.CommunicationProfile
import de.gematik.ti.erp.app.db.entities.ProfileColorNames
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod.Biometrics
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod.DeviceCredentials
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod.DeviceSecurity
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod.HealthCard
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod.None
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod.Password
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod.Unspecified
import de.gematik.ti.erp.app.db.entities.TaskStatus
import de.gematik.ti.erp.app.db.entities.v1.ProfileColorNamesV1
import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import de.gematik.ti.erp.app.db.entities.v1.SettingsAuthenticationMethodV1
import de.gematik.ti.erp.app.db.entities.v1.SettingsEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationProfileV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationDispenseEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationEntityV1

import de.gematik.ti.erp.app.db.entities.v1.task.ScannedTaskEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.SyncedTaskEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.TaskStatusV1
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.db.toRealmInstant
import de.gematik.ti.erp.app.prescription.model.CommunicationWaitStateDelta
import de.gematik.ti.erp.app.prescription.repository.extractResources
import de.gematik.ti.erp.app.prescription.repository.toInsuranceInformationEntityV1
import de.gematik.ti.erp.app.prescription.repository.toMedicationEntityV1
import de.gematik.ti.erp.app.prescription.repository.toMedicationRequestEntityV1
import de.gematik.ti.erp.app.prescription.repository.toOrganizationEntityV1
import de.gematik.ti.erp.app.prescription.repository.toPatientEntityV1
import de.gematik.ti.erp.app.prescription.repository.toPractitionerEntityV1
import io.realm.kotlin.Realm

import io.realm.kotlin.ext.toRealmList
import java.time.ZoneOffset
import java.util.UUID
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.time.LocalDate

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coverage
import org.hl7.fhir.r4.model.Medication
import org.hl7.fhir.r4.model.MedicationRequest
import org.hl7.fhir.r4.model.Organization
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Practitioner
import io.github.aakira.napier.Napier
import org.kodein.di.DI
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import java.time.Instant
import java.time.OffsetDateTime

const val REALM_MIGRATION_COMPLETED = "RealmMigrationCompleted"

private const val ENCRYPTED_PREFS_FILE_NAME = "ENCRYPTED_PREFS_FILE_NAME"
private const val ENCRYPTED_PREFS_PASSWORD_KEY = "ENCRYPTED_PREFS_PASSWORD_KEY"
private const val MASTER_KEY_ALIAS = "ROOM_DB_MASTER_KEY"

const val RoomDatabaseSecurePreferencesTag = "RoomDatabaseSecurePreferences"

val migrations = arrayOf(
    MIGRATION_1_2,
    MIGRATION_2_3,
    MIGRATION_3_4,
    MIGRATION_4_5,
    MIGRATION_5_6,
    MIGRATION_6_7,
    MIGRATION_7_8,
    MIGRATION_8_9,
    MIGRATION_9_10,
    MIGRATION_10_11,
    MIGRATION_11_12,
    MIGRATION_12_13,
    MIGRATION_13_14,
    MIGRATION_14_15,
    MIGRATION_15_16,
    MIGRATION_16_17,
    MIGRATION_17_18,
    MIGRATION_18_19,
    MIGRATION_19_20,
    MIGRATION_20_21,
    MIGRATION_21_22,
    MIGRATION_22_23,
    MIGRATION_23_24,
    MIGRATION_24_25,
    MIGRATION_25_26,
    MIGRATION_27_28
)

val roomModule = DI.Module("roomModule") {
    bindSingleton(RoomDatabaseSecurePreferencesTag) {
        val context = instance<Context>()

        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_FILE_NAME,
            MasterKey.Builder(context, MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    bindEagerSingleton {
        val context = instance<Context>()
        val realm = instance<Realm>()
        val appPrefs = instance<SharedPreferences>(ApplicationPreferencesTag)
        val securePrefs = instance<SharedPreferences>(RoomDatabaseSecurePreferencesTag)
        try {
            val passphrase: ByteArray =
                SQLiteDatabase.getBytes(getPassphrase(securePrefs).toCharArray())
            val factory = SupportFactory(passphrase)
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "db"
            )
                .addMigrations(*migrations)
                .openHelperFactory(factory)
                .allowMainThreadQueries()
                .build().also { db ->
                    if (!appPrefs.getBoolean(REALM_MIGRATION_COMPLETED, false)) {
                        migrateRoomToRealm(db, realm, appPrefs)
                        appPrefs.edit().putBoolean(REALM_MIGRATION_COMPLETED, true).commit()
                    }
                }
        } catch (expected: Throwable) {
            throw MessageConversionException(expected)
        }
    }
}

private fun migrateRoomToRealm(db: AppDatabase, realm: Realm, appPrefs: SharedPreferences) {
    db.migrationDao().getSettings()?.also { settingsSql ->
        realm.writeBlocking {
            findLatest(queryFirst<SettingsEntityV1>()!!)?.apply {
                this.authenticationMethod = when (settingsSql.authenticationMethod) {
                    HealthCard -> SettingsAuthenticationMethodV1.HealthCard
                    DeviceSecurity -> SettingsAuthenticationMethodV1.DeviceSecurity
                    Biometrics -> SettingsAuthenticationMethodV1.Biometrics
                    DeviceCredentials -> SettingsAuthenticationMethodV1.DeviceCredentials
                    Password -> SettingsAuthenticationMethodV1.Password
                    None -> SettingsAuthenticationMethodV1.None
                    Unspecified -> SettingsAuthenticationMethodV1.Unspecified
                }
                this.authenticationFails = settingsSql.authenticationFails
                this.zoomEnabled = settingsSql.zoomEnabled
                this.pharmacySearch?.name = settingsSql.pharmacySearch.name
                this.pharmacySearch?.locationEnabled = settingsSql.pharmacySearch.locationEnabled
                this.pharmacySearch?.filterDeliveryService =
                    settingsSql.pharmacySearch.filterDeliveryService
                this.pharmacySearch?.filterOnlineService = settingsSql.pharmacySearch.filterOnlineService
                this.pharmacySearch?.filterReady = settingsSql.pharmacySearch.filterReady
                this.pharmacySearch?.filterOpenNow = settingsSql.pharmacySearch.filterOpenNow
                this.userHasAcceptedInsecureDevice = settingsSql.userHasAcceptedInsecureDevice
                this.dataProtectionVersionAccepted =
                    LocalDate.parse(settingsSql.dataProtectionVersionAccepted).atStartOfDay()
                        .toInstant(ZoneOffset.UTC)
                        .toRealmInstant()
                this.password?.hash = settingsSql.password?.hash ?: byteArrayOf()
                this.password?.salt = settingsSql.password?.salt ?: byteArrayOf()

                if (!appPrefs.getBoolean("newUser", true)) {
                    this.onboardingLatestAppVersionName = this.latestAppVersionName
                    this.onboardingLatestAppVersionCode = this.latestAppVersionCode
                }
            }
        }
    }

    val profiles = db.migrationDao().getProfiles().map { profile ->
        ProfileEntityV1().apply {
            this.name = profile.name
            this.insurantName = profile.insurantName
            this.insuranceIdentifier = profile.insuranceIdentifier
            this.insuranceName = profile.insuranceName
            this.lastAuthenticated = profile.lastAuthenticated?.let {
                Instant.parse(profile.lastAuthenticated).toRealmInstant()
            }
            this.color = when (profile.color) {
                ProfileColorNames.SPRING_GRAY.name -> ProfileColorNamesV1.SPRING_GRAY
                ProfileColorNames.SUN_DEW.name -> ProfileColorNamesV1.SUN_DEW
                ProfileColorNames.PINK.name -> ProfileColorNamesV1.PINK
                ProfileColorNames.TREE.name -> ProfileColorNamesV1.TREE
                ProfileColorNames.BLUE_MOON.name -> ProfileColorNamesV1.BLUE_MOON
                else -> ProfileColorNamesV1.SPRING_GRAY
            }
        }
    }

    val communications = db.migrationDao().getCommunications().map { com ->
        CommunicationEntityV1().apply {
            this.communicationId = com.communicationId
            this.profile = when (com.profile) {
                CommunicationProfile.ErxCommunicationDispReq -> CommunicationProfileV1.ErxCommunicationDispReq
                CommunicationProfile.ErxCommunicationReply -> CommunicationProfileV1.ErxCommunicationReply
            }
            this.taskId = com.taskId
            this.sentOn = Instant.now().minus(CommunicationWaitStateDelta).toRealmInstant()
            this.sender = when (com.profile) {
                CommunicationProfile.ErxCommunicationDispReq -> com.kbvUserId
                CommunicationProfile.ErxCommunicationReply -> com.telematicsId
            }
            this.recipient = when (com.profile) {
                CommunicationProfile.ErxCommunicationDispReq -> com.telematicsId
                CommunicationProfile.ErxCommunicationReply -> com.kbvUserId
            }
            this.payload = com.payload
            this.consumed = com.consumed
        }
    }

    val medicationDispenses = db.migrationDao().getMedicationDispenses().map { medicationDispense ->
        MedicationDispenseEntityV1().apply {
            this.dispenseId = medicationDispense.taskId
            this.patientIdentifier = medicationDispense.patientIdentifier
            this.medication = MedicationEntityV1().apply {
                this.text = medicationDispense.text ?: ""
                this.form = medicationDispense.type
                this.normSizeCode = null // was not extracted
                this.uniqueIdentifier = medicationDispense.uniqueIdentifier
            }
            this.wasSubstituted = medicationDispense.wasSubstituted
            this.dosageInstruction = medicationDispense.dosageInstruction
            this.performer = medicationDispense.performer
            this.whenHandedOver = medicationDispense.whenHandedOver.let { Instant.parse(it) }?.toRealmInstant()!!
        }
    }

    db.migrationDao().getTasks().map { task ->
        if (task.rawKBVBundle == null) {
            val scannedTask = ScannedTaskEntityV1().apply {
                this.taskId = task.taskId
                this.accessCode = task.accessCode!!
                this.redeemedOn = task.redeemedOn?.let { OffsetDateTime.parse(it) }?.toInstant()?.toRealmInstant()
                this.scannedOn = task.scannedOn?.let { OffsetDateTime.parse(it) }?.toInstant()?.toRealmInstant()!!
            }
            profiles.find {
                it.name == task.profileName
            }?.scannedTasks?.add(scannedTask)
        }
    }

    db.migrationDao().getTasks().map { task ->
        if (task.rawKBVBundle != null) {
            try {
                val fhirParser = LazyFhirParser()
                val syncedTask = SyncedTaskEntityV1().apply {
                    val bundle = fhirParser.parseResource(task.rawKBVBundle.decodeToString()) as Bundle
                    val medicationRequest = bundle.extractResources<MedicationRequest>().first()
                    val medication = bundle.extractResources<Medication>().first()
                    val organization = bundle.extractResources<Organization>().first()
                    val practitioner = bundle.extractResources<Practitioner>().first()
                    val patient = bundle.extractResources<Patient>().first()
                    val insuranceInformation = bundle.extractResources<Coverage>().first()

                    this.taskId = task.taskId
                    this.accessCode = task.accessCode
                    this.lastModified = Instant.parse(task.lastModified).toRealmInstant()
                    this.status = when (task.status) {
                        TaskStatus.Ready -> TaskStatusV1.Ready
                        TaskStatus.InProgress -> TaskStatusV1.InProgress
                        TaskStatus.Completed -> TaskStatusV1.Completed
                        else -> TaskStatusV1.Other
                    }
                    this.expiresOn =
                        LocalDate.parse(task.expiresOn!!).atStartOfDay().toInstant(ZoneOffset.UTC).toRealmInstant()
                    this.acceptUntil = LocalDate.parse(task.acceptUntil!!).atStartOfDay().toInstant(ZoneOffset.UTC)
                        .toRealmInstant()
                    this.authoredOn = OffsetDateTime.parse(task.authoredOn!!).toInstant().toRealmInstant()
                    this.organization = organization.toOrganizationEntityV1()
                    this.practitioner = practitioner.toPractitionerEntityV1()
                    this.patient = patient.toPatientEntityV1()
                    this.insuranceInformation = insuranceInformation.toInsuranceInformationEntityV1()
                    this.status = when (task.status) {
                        TaskStatus.Ready -> TaskStatusV1.Ready
                        TaskStatus.InProgress -> TaskStatusV1.InProgress
                        TaskStatus.Completed -> TaskStatusV1.Completed
                        else -> TaskStatusV1.Other
                    }
                    this.medicationRequest =
                        medicationRequest.toMedicationRequestEntityV1(medication.toMedicationEntityV1())
                    this.medicationDispenses += medicationDispenses.filter {
                        it.dispenseId == task.taskId
                    }.toRealmList()
                    val communicationsFromTask = communications.filter {
                        it.taskId == task.taskId
                    }
                    this.communications += communicationsFromTask
                }
                profiles.find {
                    it.name == task.profileName
                }?.apply {
                    this.syncedTasks += syncedTask
                }
            } catch (expected: Exception) {
                Napier.e("Migration error", expected)
            }
        }
    }

    val profileIsUnused = profiles.size == 1 && profiles.first().name == "" && appPrefs.getBoolean("newUser", true)

    if (!profileIsUnused) {
        realm.writeBlocking {
            profiles.forEach {
                copyToRealm(it)
            }
            queryFirst<ProfileEntityV1>()?.let { profileToActivate ->
                profileToActivate.active = true
            }
        }
    }
}

private fun generatePassPhrase(): String {
    return UUID.randomUUID().toString()
}

private fun storePassPhrase(
    sharedPreferences: SharedPreferences,
    passPhrase: String
) {
    sharedPreferences.edit().putString(
        ENCRYPTED_PREFS_PASSWORD_KEY,
        passPhrase
    )
        .apply()
}

private fun getPassphrase(sharedPreferences: SharedPreferences): String {
    if (getPassword(sharedPreferences).isNullOrEmpty()) {
        val passPhrase = generatePassPhrase()
        storePassPhrase(sharedPreferences, passPhrase)
    }
    return getPassword(sharedPreferences)
        ?: throw IllegalStateException("passphrase should not be empty")
}

private fun getPassword(sharedPreferences: SharedPreferences): String? {
    return sharedPreferences.getString(
        ENCRYPTED_PREFS_PASSWORD_KEY,
        null
    )
}
