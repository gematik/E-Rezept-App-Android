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

package de.gematik.ti.erp.app.db.entities.v1

import de.gematik.ti.erp.app.db.TestDB
import de.gematik.ti.erp.app.db.entities.deleteAll
import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationEntityV1
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
import de.gematik.ti.erp.app.db.entities.v1.task.TaskStatusV1
import de.gematik.ti.erp.app.db.queryFirst
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncedTaskEntityV1Test : TestDB() {
    @Test
    fun `cascading delete`() {
        Realm.open(
            RealmConfiguration.Builder(
                schema = setOf(
                    SettingsEntityV1::class,
                    PharmacySearchEntityV1::class,
                    PasswordEntityV1::class,
                    TruststoreEntityV1::class,
                    SafetynetAttestationEntityV1::class,
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
                    MultiplePrescriptionInfoEntityV1::class
                )
            )
                .schemaVersion(0)
                .directory(tempDBPath)
                .build()
        ).also { realm ->
            realm.writeBlocking {
                copyToRealm(
                    SyncedTaskEntityV1().apply {
                        this.taskId = "123"
                        this.accessCode = "123"
                        this.lastModified = RealmInstant.MIN
                        this.expiresOn = RealmInstant.MIN
                        this.acceptUntil = RealmInstant.MIN
                        this.authoredOn = RealmInstant.MIN
                        this.organization = OrganizationEntityV1().apply {
                            this.address = AddressEntityV1()
                        }
                        this.practitioner = PractitionerEntityV1()
                        this.patient = PatientEntityV1().apply {
                            this.address = AddressEntityV1()
                        }
                        this.insuranceInformation = InsuranceInformationEntityV1()
                        this.status = TaskStatusV1.Ready
                        this.medicationRequest = MedicationRequestEntityV1().apply {
                            this.medication = MedicationEntityV1().apply {
                                this.amount = RatioEntityV1().apply {
                                    this.numerator = QuantityEntityV1().apply {
                                        this.value = "1"
                                        this.unit = "Tab"
                                    }
                                    this.denominator = QuantityEntityV1().apply {
                                        this.value = "1"
                                        this.unit = "X"
                                    }
                                }
                                this.ingredients = realmListOf(
                                    IngredientEntityV1().apply {
                                        this.strength = RatioEntityV1().apply {
                                            this.numerator = QuantityEntityV1().apply {
                                                this.value = "1"
                                                this.unit = "Tab"
                                            }
                                            this.denominator = QuantityEntityV1().apply {
                                                this.value = "1"
                                                this.unit = "X"
                                            }
                                        }
                                    }
                                )
                            }
                            this.multiplePrescriptionInfo = MultiplePrescriptionInfoEntityV1().apply {
                                this.indicator = true
                                this.numbering = RatioEntityV1().apply {
                                    this.denominator = QuantityEntityV1().apply {
                                        this.value = "1"
                                    }
                                }
                            }
                        }
                        this.medicationDispenses = realmListOf(
                            MedicationDispenseEntityV1().apply {
                                this.medication = MedicationEntityV1().apply {
                                    this.amount = RatioEntityV1().apply {
                                        this.numerator = QuantityEntityV1().apply {
                                            this.value = "1"
                                            this.unit = "Tab"
                                        }
                                        this.denominator = QuantityEntityV1().apply {
                                            this.value = "1"
                                            this.unit = "X"
                                        }
                                    }
                                    this.ingredients = realmListOf(
                                        IngredientEntityV1().apply {
                                            this.strength = RatioEntityV1().apply {
                                                this.numerator = QuantityEntityV1().apply {
                                                    this.value = "1"
                                                    this.unit = "Tab"
                                                }
                                                this.denominator = QuantityEntityV1().apply {
                                                    this.value = "1"
                                                    this.unit = "X"
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        )
                        this.communications = realmListOf(
                            CommunicationEntityV1()
                        )
                    }
                )
            }

            assertEquals(1, realm.query<SyncedTaskEntityV1>().count().find())
            assertEquals(1, realm.query<OrganizationEntityV1>().count().find())
            assertEquals(1, realm.query<PractitionerEntityV1>().count().find())
            assertEquals(1, realm.query<PatientEntityV1>().count().find())
            assertEquals(1, realm.query<InsuranceInformationEntityV1>().count().find())
            assertEquals(1, realm.query<MedicationRequestEntityV1>().count().find())
            assertEquals(1, realm.query<MedicationDispenseEntityV1>().count().find())
            assertEquals(1, realm.query<CommunicationEntityV1>().count().find())
            assertEquals(2, realm.query<AddressEntityV1>().count().find())
            assertEquals(2, realm.query<MedicationEntityV1>().count().find())
            assertEquals(2, realm.query<IngredientEntityV1>().count().find())
            assertEquals(1, realm.query<MultiplePrescriptionInfoEntityV1>().count().find())
            assertEquals(5, realm.query<RatioEntityV1>().count().find())
            assertEquals(9, realm.query<QuantityEntityV1>().count().find())

            realm.writeBlocking {
                val syncedTasks = queryFirst<SyncedTaskEntityV1>()!!
                deleteAll(syncedTasks)
            }

            assertEquals(0, realm.query<SyncedTaskEntityV1>().count().find())
            assertEquals(0, realm.query<OrganizationEntityV1>().count().find())
            assertEquals(0, realm.query<PractitionerEntityV1>().count().find())
            assertEquals(0, realm.query<PatientEntityV1>().count().find())
            assertEquals(0, realm.query<InsuranceInformationEntityV1>().count().find())
            assertEquals(0, realm.query<MedicationRequestEntityV1>().count().find())
            assertEquals(0, realm.query<MedicationDispenseEntityV1>().count().find())
            assertEquals(0, realm.query<CommunicationEntityV1>().count().find())
            assertEquals(0, realm.query<AddressEntityV1>().count().find())
            assertEquals(0, realm.query<MedicationEntityV1>().count().find())
            assertEquals(0, realm.query<IngredientEntityV1>().count().find())
            assertEquals(0, realm.query<RatioEntityV1>().count().find())
            assertEquals(0, realm.query<QuantityEntityV1>().count().find())
            assertEquals(0, realm.query<MultiplePrescriptionInfoEntityV1>().count().find())
        }
    }
}
