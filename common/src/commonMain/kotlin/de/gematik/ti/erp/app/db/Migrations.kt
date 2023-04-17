/*
 * Copyright (c) 2023 gematik GmbH
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

import de.gematik.ti.erp.app.db.entities.v1.AddressEntityV1
import de.gematik.ti.erp.app.db.entities.v1.AuditEventEntityV1
import de.gematik.ti.erp.app.db.entities.v1.AvatarFigureV1
import de.gematik.ti.erp.app.db.entities.v1.IdpAuthenticationDataEntityV1
import de.gematik.ti.erp.app.db.entities.v1.IdpConfigurationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.InsuranceTypeV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.PKVInvoiceEntityV1
import de.gematik.ti.erp.app.db.entities.v1.PasswordEntityV1
import de.gematik.ti.erp.app.db.entities.v1.pharmacy.PharmacyCacheEntityV1
import de.gematik.ti.erp.app.db.entities.v1.PharmacySearchEntityV1
import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import de.gematik.ti.erp.app.db.entities.v1.SettingsEntityV1
import de.gematik.ti.erp.app.db.entities.v1.ShippingContactEntityV1
import de.gematik.ti.erp.app.db.entities.v1.TruststoreEntityV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.ChargeableItemV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.InvoiceEntityV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.PriceComponentV1
import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.pharmacy.FavoritePharmacyEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.IngredientEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.InsuranceInformationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationDispenseEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationRequestEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MultiplePrescriptionInfoEntityV1
import de.gematik.ti.erp.app.db.entities.v1.pharmacy.OftenUsedPharmacyEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.AccidentTypeV1
import de.gematik.ti.erp.app.db.entities.v1.task.OrganizationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.PatientEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.PractitionerEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.QuantityEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.RatioEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.ScannedTaskEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.SyncedTaskEntityV1
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf

const val ACTUAL_SCHEMA_VERSION = 19L

val appSchemas = setOf(
    AppRealmSchema(
        version = ACTUAL_SCHEMA_VERSION,
        classes = setOf(
            SettingsEntityV1::class,
            PharmacySearchEntityV1::class,
            PasswordEntityV1::class,
            TruststoreEntityV1::class,
            IdpConfigurationEntityV1::class,
            ProfileEntityV1::class,
            CommunicationEntityV1::class,
            MedicationEntityV1::class,
            MedicationDispenseEntityV1::class,
            MedicationRequestEntityV1::class,
            OrganizationEntityV1::class,
            PatientEntityV1::class,
            PractitionerEntityV1::class,
            ScannedTaskEntityV1::class,
            SyncedTaskEntityV1::class,
            AuditEventEntityV1::class,
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
            PriceComponentV1::class
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
        }
    )
)
