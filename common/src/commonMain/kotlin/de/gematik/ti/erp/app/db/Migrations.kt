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

package de.gematik.ti.erp.app.db

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.db.entities.v1.AddressEntityV1
import de.gematik.ti.erp.app.db.entities.v1.AuthenticationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.AuthenticationPasswordEntityV1
import de.gematik.ti.erp.app.db.entities.v1.AvatarFigureV1
import de.gematik.ti.erp.app.db.entities.v1.IdpAuthenticationDataEntityV1
import de.gematik.ti.erp.app.db.entities.v1.IdpConfigurationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.InAppMessageEntity
import de.gematik.ti.erp.app.db.entities.v1.InsuranceTypeV1
import de.gematik.ti.erp.app.db.entities.v1.InternalMessageEntity
import de.gematik.ti.erp.app.db.entities.v1.InternalMessageEntityV1
import de.gematik.ti.erp.app.db.entities.v1.PasswordEntityV1
import de.gematik.ti.erp.app.db.entities.v1.PharmacySearchEntityV1
import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import de.gematik.ti.erp.app.db.entities.v1.SettingsAuthenticationMethodV1
import de.gematik.ti.erp.app.db.entities.v1.SettingsEntityV1
import de.gematik.ti.erp.app.db.entities.v1.ShippingContactEntityV1
import de.gematik.ti.erp.app.db.entities.v1.SingleSignOnTokenScopeV1
import de.gematik.ti.erp.app.db.entities.v1.TruststoreEntityV1
import de.gematik.ti.erp.app.db.entities.v1.debugsettings.DebugSettingsEntityV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.ChargeableItemV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.InvoiceEntityV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.PKVInvoiceEntityV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.PriceComponentV1
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationDosageEntityV1
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationNotificationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationScheduleEntityV1
import de.gematik.ti.erp.app.db.entities.v1.pharmacy.FavoritePharmacyEntityV1
import de.gematik.ti.erp.app.db.entities.v1.pharmacy.OftenUsedPharmacyEntityV1
import de.gematik.ti.erp.app.db.entities.v1.pharmacy.PharmacyCacheEntityV1
import de.gematik.ti.erp.app.db.entities.v1.pharmacy.PharmacyRemoteDataSourceSelectionEntityV1
import de.gematik.ti.erp.app.db.entities.v1.pharmacy.SearchAccessTokenEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.AccidentTypeV1
import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.CoverageTypeV1
import de.gematik.ti.erp.app.db.entities.v1.task.DeviceRequestDispenseEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.DeviceRequestEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.IdentifierEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.IngredientEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.InsuranceInformationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationDispenseEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationRequestEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MultiplePrescriptionInfoEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.OrganizationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.PatientEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.PractitionerEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.QuantityEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.RatioEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.ScannedTaskEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.SyncedTaskEntityV1
import io.realm.kotlin.ext.copyFromRealm
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.nanoseconds

@Requirement(
    "O.Source_2#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "The list of the model classes for the typed-safe databases.",
    codeLines = 50
)
@Suppress("CyclomaticComplexMethod")
fun appSchemas(profileName: String): Set<AppRealmSchema> {
    return setOf(
        AppRealmSchema(
            version = SchemaVersion.ACTUAL,
            classes = setOf(
                SettingsEntityV1::class,
                AuthenticationEntityV1::class,
                AuthenticationPasswordEntityV1::class,
                PharmacySearchEntityV1::class,
                PasswordEntityV1::class, // TODO remove after migration 38
                TruststoreEntityV1::class,
                IdpConfigurationEntityV1::class,
                ProfileEntityV1::class,
                CommunicationEntityV1::class,
                MedicationEntityV1::class,
                IdentifierEntityV1::class,
                MedicationDispenseEntityV1::class,
                MedicationRequestEntityV1::class,
                OrganizationEntityV1::class,
                PatientEntityV1::class,
                PractitionerEntityV1::class,
                ScannedTaskEntityV1::class,
                SyncedTaskEntityV1::class,
                IdpAuthenticationDataEntityV1::class,
                AddressEntityV1::class,
                InsuranceInformationEntityV1::class,
                ShippingContactEntityV1::class,
                IngredientEntityV1::class,
                QuantityEntityV1::class,
                RatioEntityV1::class,
                PharmacyCacheEntityV1::class,
                OftenUsedPharmacyEntityV1::class,
                MultiplePrescriptionInfoEntityV1::class,
                FavoritePharmacyEntityV1::class,
                IngredientEntityV1::class,
                PKVInvoiceEntityV1::class,
                InvoiceEntityV1::class,
                ChargeableItemV1::class,
                PriceComponentV1::class,
                InAppMessageEntity::class,
                InternalMessageEntityV1::class,
                InternalMessageEntity::class,
                MedicationDosageEntityV1::class,
                MedicationNotificationEntityV1::class,
                MedicationScheduleEntityV1::class,
                SearchAccessTokenEntityV1::class,
                // added for test purposes
                PharmacyRemoteDataSourceSelectionEntityV1::class,
                DebugSettingsEntityV1::class,
                // support for digas
                DeviceRequestEntityV1::class,
                DeviceRequestDispenseEntityV1::class
            ),
            migrateOrInitialize = { migrationStartedFrom ->
                queryFirst<SettingsEntityV1>() ?: run {
                    copyToRealm(
                        SettingsEntityV1()
                    )
                }
                if (migrationStartedFrom < 3L) {
                    query<ProfileEntityV1>().find().forEach { profile ->
                        profile.syncedTasks.forEach { syncedTask ->
                            syncedTask.parent = profile

                            syncedTask.communications.forEach {
                                it.parent = syncedTask
                                it.orderId = ""
                            }
                        }
                        profile.scannedTasks.forEach { scannedTask ->
                            scannedTask.parent = profile
                        }
                    }
                }
                if (migrationStartedFrom < 10L) {
                    query<ProfileEntityV1>().find().forEach {
                        it._avatarFigure = AvatarFigureV1.PersonalizedImage.toString()
                    }
                }
                if (migrationStartedFrom < 12L) {
                    query<MedicationEntityV1>().find().forEach {
                        if (it._expirationDate?.isEmpty() == true) {
                            it._expirationDate = null
                        }
                    }
                }
                if (migrationStartedFrom < 15L) {
                    query<MedicationRequestEntityV1>().find().forEach {
                        it.accidentType = AccidentTypeV1.None
                    }
                }
                if (migrationStartedFrom < 17L) {
                    query<ProfileEntityV1>().find().forEach {
                        if (it.lastAuthenticated != null) {
                            it._insuranceType = InsuranceTypeV1.GKV.toString()
                        } else {
                            it._insuranceType = InsuranceTypeV1.None.toString()
                        }
                    }
                }
                if (migrationStartedFrom < 18L) {
                    query<ProfileEntityV1>().find().forEach {
                        it.invoices = realmListOf()
                    }
                    query<MedicationRequestEntityV1>().find().forEach {
                        if (it._authoredOn?.isEmpty() == true) {
                            it._authoredOn = null
                        }
                    }
                }

                if (migrationStartedFrom < 19L) {
                    query<MedicationDispenseEntityV1>().find().forEach {
                        if (it._handedOverOn?.isEmpty() == true) {
                            it._handedOverOn = null
                        }
                    }
                }

                if (migrationStartedFrom < 23L) {
                    query<SyncedTaskEntityV1>().find().forEach {
                        it.lastModified = Instant.parse("2023-06-01T00:00:00Z").toRealmInstant()
                        it.isIncomplete = false
                        it.failureToReport = ""
                    }
                }

                if (migrationStartedFrom < 27) {
                    query<ScannedTaskEntityV1>().find().groupBy {
                        it.scannedOn
                    }.forEach {
                        it.value.mapIndexed { index, scannedTaskEntityV1 ->
                            scannedTaskEntityV1.index = index + 1
                        }
                    }
                }
                // Logout all users with external authentication if they have not authenticated since 15.12.2023 (GID)
                if (migrationStartedFrom < 30) {
                    query<ProfileEntityV1>().find().forEach {
                        val epochSeconds: Long = Instant.parse("2023-12-15T00:00:00Z").epochSeconds
                        if (it.idpAuthenticationData?.singleSignOnTokenScope
                            == SingleSignOnTokenScopeV1.ExternalAuthentication
                        ) {
                            it.lastAuthenticated?.let { lastAuthenticated ->
                                if (lastAuthenticated < RealmInstant.from(epochSeconds, nanosecondAdjustment = 0)) {
                                    it.idpAuthenticationData = null
                                }
                            }
                        }
                    }
                }
                if (migrationStartedFrom < 34) {
                    query<ProfileEntityV1>().find().forEach { profileId ->
                        query<ScannedTaskEntityV1>("parent = $0", profileId).find().filter { it.name == null }
                            .groupBy {
                                it.scannedOn.toLocalDateTime().date
                            }.forEach {
                                it.value.mapIndexed { idx, scannedTaskEntityV1 ->
                                    scannedTaskEntityV1.name = "Medikament ${idx + 1}"
                                    // fix the ordering by adding minimal time to the scannedTask
                                    scannedTaskEntityV1.scannedOn = (
                                        scannedTaskEntityV1.scannedOn.toInstant()
                                            .plus(scannedTaskEntityV1.index.nanoseconds)
                                        ).toRealmInstant()
                                }
                            }
                    }
                }
                if (migrationStartedFrom < 35) {
                    query<InsuranceInformationEntityV1>().find().forEach { insuranceInformation ->
                        insuranceInformation._coverageType = CoverageTypeV1.UNKNOWN.name
                    }
                }
                if (migrationStartedFrom < 36) {
                    query<SyncedTaskEntityV1>().find().forEach { task ->
                        task.lastMedicationDispense = RealmInstant.MIN
                    }
                }
                if (migrationStartedFrom < 37) {
                    query<PKVInvoiceEntityV1>().find().forEach { task ->
                        task.consumed = true
                    }
                }
                if (migrationStartedFrom < 38) {
                    query<SettingsEntityV1>().find().forEach { settings ->
                        settings.authentication = AuthenticationEntityV1().apply {
                            when (settings.authenticationMethod) {
                                SettingsAuthenticationMethodV1.Password -> {
                                    this.password = AuthenticationPasswordEntityV1().apply {
                                        settings.password?.let { password ->
                                            this.setHash(password._hash)
                                            this.setSalt(password._salt)
                                        }
                                    }
                                }

                                SettingsAuthenticationMethodV1.DeviceSecurity -> {
                                    this.deviceSecurity = true
                                }

                                else -> {
                                    // do nothing since onboarding was not done yet
                                }
                            }
                            this.failedAuthenticationAttempts = settings.authenticationFails
                        }
                    }
                }
                if (migrationStartedFrom < 39) {
                    query<ProfileEntityV1>().find().forEach { profile ->
                        profile.isNewlyCreated = profile.name == profileName
                    }
                }

                if (migrationStartedFrom < 41) {
                    query<SyncedTaskEntityV1>().find().forEach { syncedTask ->
                        for (medicationDispense in syncedTask.medicationDispenses) {
                            medicationDispense.medication = medicationDispense.medication?.copyFromRealm().apply {
                                this?.identifier = IdentifierEntityV1().apply {
                                    this.pzn = medicationDispense.medication?.uniqueIdentifier
                                }
                            }
                        }

                        syncedTask.medicationRequest?.medication = syncedTask.medicationRequest?.medication?.copyFromRealm().apply {
                            this?.identifier = IdentifierEntityV1().apply {
                                this.pzn = syncedTask.medicationRequest?.medication?.uniqueIdentifier
                            }
                        }
                    }
                }

                // migration needed since the welcome message timestamp was set to the current time everytime time the app was started
                if (migrationStartedFrom < 42) {
                    query<InternalMessageEntity>().find().forEach { internalMessage ->
                        internalMessage.welcomeMessageTimeStamp = Clock.System.now().toRealmInstant()
                    }
                }
                if (migrationStartedFrom < 45) {
                    query<InternalMessageEntity>().find().forEach { internalMessage ->
                        if (internalMessage.showWelcomeMessage == null) {
                            internalMessage.showWelcomeMessage = false
                        }
                    }
                }
                if (migrationStartedFrom < 46) {
                    val lowestVersion = query<InternalMessageEntity>().find().flatMap { internalMessageEntity ->
                        internalMessageEntity.inAppMessageEntity.map { it.version }
                    }.minOrNull()
                    val welcomeMessageTimeStamp = query<InternalMessageEntity>().find().mapNotNull {
                        it.welcomeMessageTimeStamp
                    }.minOrNull()

                    if (lowestVersion != null && welcomeMessageTimeStamp != null) {
                        copyToRealm(
                            InternalMessageEntityV1()
                                .apply {
                                    // create placeholder welcome message which will be replaced in the updateInternalMessagesUseCase
                                    this.id = "0"
                                    this.time = welcomeMessageTimeStamp
                                    this.version = lowestVersion
                                }
                        )
                    }
                }
            }
        )
    )
}
