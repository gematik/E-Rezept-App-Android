/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.invoice.repository

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.api.ErpService
import de.gematik.ti.erp.app.db.SchemaVersion
import de.gematik.ti.erp.app.db.TestDB
import de.gematik.ti.erp.app.db.entities.v1.AddressEntityV1
import de.gematik.ti.erp.app.db.entities.v1.AuthenticationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.AuthenticationPasswordEntityV1
import de.gematik.ti.erp.app.db.entities.v1.IdpAuthenticationDataEntityV1
import de.gematik.ti.erp.app.db.entities.v1.IdpConfigurationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.PasswordEntityV1
import de.gematik.ti.erp.app.db.entities.v1.PharmacySearchEntityV1
import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import de.gematik.ti.erp.app.db.entities.v1.SettingsEntityV1
import de.gematik.ti.erp.app.db.entities.v1.ShippingContactEntityV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.ChargeableItemV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.InvoiceEntityV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.PKVInvoiceEntityV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.PriceComponentV1
import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationEntityV1
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
import de.gematik.ti.erp.app.fhir.model.chargeItem_freetext
import de.gematik.ti.erp.app.profiles.repository.DefaultProfilesRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class InvoiceRepositoryTest : TestDB() {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    lateinit var invoiceLocalDataSource: InvoiceLocalDataSource
    lateinit var invoiceRemoteDataSource: InvoiceRemoteDataSource
    lateinit var invoiceRepository: DefaultInvoiceRepository
    lateinit var profileRepository: ProfileRepository

    lateinit var realm: Realm

    @MockK
    lateinit var erpService: ErpService

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        realm = Realm.open(
            RealmConfiguration.Builder(
                schema = setOf(
                    ProfileEntityV1::class,
                    SyncedTaskEntityV1::class,
                    OrganizationEntityV1::class,
                    PractitionerEntityV1::class,
                    PatientEntityV1::class,
                    InsuranceInformationEntityV1::class,
                    MedicationRequestEntityV1::class,
                    MedicationDispenseEntityV1::class,
                    CommunicationEntityV1::class,
                    AddressEntityV1::class,
                    MedicationEntityV1::class,
                    IngredientEntityV1::class,
                    RatioEntityV1::class,
                    QuantityEntityV1::class,
                    ScannedTaskEntityV1::class,
                    IdpAuthenticationDataEntityV1::class,
                    IdpConfigurationEntityV1::class,
                    SettingsEntityV1::class,
                    PharmacySearchEntityV1::class,
                    PasswordEntityV1::class,
                    ShippingContactEntityV1::class,
                    PharmacySearchEntityV1::class,
                    MultiplePrescriptionInfoEntityV1::class,
                    PKVInvoiceEntityV1::class,
                    InvoiceEntityV1::class,
                    ChargeableItemV1::class,
                    PriceComponentV1::class,
                    IdentifierEntityV1::class,
                    AuthenticationPasswordEntityV1::class,
                    AuthenticationEntityV1::class,
                    DeviceRequestEntityV1::class
                )
            )
                .schemaVersion(SchemaVersion.ACTUAL)
                .directory(tempDBPath)
                .build()
        )

        invoiceLocalDataSource = InvoiceLocalDataSource(realm)
        invoiceRemoteDataSource = InvoiceRemoteDataSource(erpService)
        invoiceRepository = DefaultInvoiceRepository(
            invoiceRemoteDataSource,
            invoiceLocalDataSource,
            coroutineRule.dispatchers
        )
        profileRepository = DefaultProfilesRepository(realm)
    }

    @Test
    fun `save invoices and load invoice`() {
        val chargeItemByIdBundle = Json.parseToJsonElement(chargeItem_freetext)

        runTest {
            profileRepository.createNewProfile("test")
            val testProfileId =
                profileRepository.profiles().first()[0].id

            invoiceRepository.saveInvoice(testProfileId, chargeItemByIdBundle)
            val invoice = invoiceRepository.invoices(testProfileId).first()[0]

            assertEquals("200.334.138.469.717.92", invoice.taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", invoice.accessCode)
            assertEquals(36.15, invoice.invoice.totalBruttoAmount)
            assertEquals(Instant.parse("2023-07-07T23:30:00Z"), invoice.timestamp)

            val attachments = invoiceRepository.loadInvoiceAttachments(invoice.taskId)

            assertEquals("200.334.138.469.717.92_verordnung.p7s", attachments?.get(0)?.first)
            assertEquals("application/pkcs7-mime", attachments?.get(0)?.second)

            assertEquals("200.334.138.469.717.92_abgabedaten.p7s", attachments?.get(1)?.first)
            assertEquals("application/pkcs7-mime", attachments?.get(1)?.second)

            assertEquals("200.334.138.469.717.92_quittung.p7s", attachments?.get(2)?.first)
            assertEquals("application/pkcs7-mime", attachments?.get(2)?.second)
        }
    }
}
