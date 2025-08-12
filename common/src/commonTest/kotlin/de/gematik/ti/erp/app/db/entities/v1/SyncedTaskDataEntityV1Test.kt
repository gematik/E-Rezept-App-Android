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

package de.gematik.ti.erp.app.db.entities.v1

import de.gematik.ti.erp.app.database.realm.utils.deleteAll
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.v1.AddressEntityV1
import de.gematik.ti.erp.app.database.realm.v1.AuthenticationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.AuthenticationPasswordEntityV1
import de.gematik.ti.erp.app.database.realm.v1.IdpAuthenticationDataEntityV1
import de.gematik.ti.erp.app.database.realm.v1.IdpConfigurationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.PasswordEntityV1
import de.gematik.ti.erp.app.database.realm.v1.PharmacySearchEntityV1
import de.gematik.ti.erp.app.database.realm.v1.ProfileEntityV1
import de.gematik.ti.erp.app.database.realm.v1.SettingsEntityV1
import de.gematik.ti.erp.app.database.realm.v1.ShippingContactEntityV1
import de.gematik.ti.erp.app.database.realm.v1.TruststoreEntityV1
import de.gematik.ti.erp.app.database.realm.v1.invoice.ChargeableItemV1
import de.gematik.ti.erp.app.database.realm.v1.invoice.InvoiceEntityV1
import de.gematik.ti.erp.app.database.realm.v1.invoice.PKVInvoiceEntityV1
import de.gematik.ti.erp.app.database.realm.v1.invoice.PriceComponentV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.CommunicationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.DeviceRequestDispenseEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.DeviceRequestEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.IdentifierEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.IngredientEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.InsuranceInformationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MedicationDispenseEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MedicationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MedicationRequestEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MultiplePrescriptionInfoEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.OrganizationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.PatientEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.PractitionerEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.QuantityEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.RatioEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.ScannedTaskEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.SyncedTaskEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.TaskStatusV1
import de.gematik.ti.erp.app.db.TestDB
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import org.junit.Test
import kotlin.test.assertEquals

class SyncedTaskDataEntityV1Test : TestDB() {
    @Test
    fun `cascading delete`() {
        Realm.open(
            RealmConfiguration.Builder(
                schema = setOf(
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
                    IdpAuthenticationDataEntityV1::class,
                    AddressEntityV1::class,
                    InsuranceInformationEntityV1::class,
                    ShippingContactEntityV1::class,
                    IngredientEntityV1::class,
                    QuantityEntityV1::class,
                    RatioEntityV1::class,
                    MultiplePrescriptionInfoEntityV1::class,
                    PKVInvoiceEntityV1::class,
                    InvoiceEntityV1::class,
                    ChargeableItemV1::class,
                    PriceComponentV1::class,
                    IdentifierEntityV1::class,
                    AuthenticationEntityV1::class,
                    AuthenticationPasswordEntityV1::class,
                    DeviceRequestEntityV1::class,
                    DeviceRequestDispenseEntityV1::class
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
                        this.deviceRequest = DeviceRequestEntityV1()
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
