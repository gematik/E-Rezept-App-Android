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

package de.gematik.ti.erp.app.database.migration

import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.database.api.AppAuthenticationLocalDataSource
import de.gematik.ti.erp.app.database.api.CommunicationLocalDataSource
import de.gematik.ti.erp.app.database.api.IdpConfigurationLocalDataSource
import de.gematik.ti.erp.app.database.api.InternalMessagesLocalDataSource
import de.gematik.ti.erp.app.database.api.MedicationPlanLocalDataSource
import de.gematik.ti.erp.app.database.api.ProfileLocalDataSource
import de.gematik.ti.erp.app.database.api.SettingsLocalDataSource
import de.gematik.ti.erp.app.database.api.ShippingInfoLocalDataSource
import de.gematik.ti.erp.app.database.api.TrustStoreLocalDataSource
import de.gematik.ti.erp.app.database.api.UserAuthenticationLocalDataSource
import de.gematik.ti.erp.app.database.api.eurezept.EuTaskLocalDataSource
import de.gematik.ti.erp.app.database.api.invoice.InvoiceLocalDataSource
import de.gematik.ti.erp.app.database.api.pharmacy.PharmacyLocalDataSource
import de.gematik.ti.erp.app.database.api.pharmacy.PharmacySearchAccessTokenLocalDataSource
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSource
import de.gematik.ti.erp.app.eurezept.model.EuOrderErpModel
import de.gematik.ti.erp.app.invoice.model.PKVInvoiceErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyErpModel
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.task.model.TaskErpModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DataMigratorTest {

    private val profileV1: ProfileLocalDataSource = mockk(relaxed = true)
    private val profileV2: ProfileLocalDataSource = mockk(relaxed = true)
    private val userAuthV1: UserAuthenticationLocalDataSource = mockk(relaxed = true)
    private val userAuthV2: UserAuthenticationLocalDataSource = mockk(relaxed = true)
    private val pharmacyV1: PharmacyLocalDataSource = mockk(relaxed = true)
    private val pharmacyV2: PharmacyLocalDataSource = mockk(relaxed = true)
    private val searchTokenV1: PharmacySearchAccessTokenLocalDataSource = mockk(relaxed = true)
    private val searchTokenV2: PharmacySearchAccessTokenLocalDataSource = mockk(relaxed = true)
    private val internalMessageV1: InternalMessagesLocalDataSource = mockk(relaxed = true)
    private val internalMessageV2: InternalMessagesLocalDataSource = mockk(relaxed = true)
    private val settingsV1: SettingsLocalDataSource = mockk(relaxed = true)
    private val settingsV2: SettingsLocalDataSource = mockk(relaxed = true)
    private val invoiceV1: InvoiceLocalDataSource = mockk(relaxed = true)
    private val invoiceV2: InvoiceLocalDataSource = mockk(relaxed = true)
    private val taskV1: TaskLocalDataSource = mockk(relaxed = true)
    private val taskV2: TaskLocalDataSource = mockk(relaxed = true)
    private val communicationV1: CommunicationLocalDataSource = mockk(relaxed = true)
    private val communicationV2: CommunicationLocalDataSource = mockk(relaxed = true)
    private val idpConfigV1: IdpConfigurationLocalDataSource = mockk(relaxed = true)
    private val idpConfigV2: IdpConfigurationLocalDataSource = mockk(relaxed = true)
    private val trustStoreV1: TrustStoreLocalDataSource = mockk(relaxed = true)
    private val trustStoreV2: TrustStoreLocalDataSource = mockk(relaxed = true)
    private val shippingInfoV1: ShippingInfoLocalDataSource = mockk(relaxed = true)
    private val shippingInfoV2: ShippingInfoLocalDataSource = mockk(relaxed = true)
    private val appAuthV1: AppAuthenticationLocalDataSource = mockk(relaxed = true)
    private val appAuthV2: AppAuthenticationLocalDataSource = mockk(relaxed = true)
    private val euTaskV1: EuTaskLocalDataSource = mockk(relaxed = true)
    private val euTaskV2: EuTaskLocalDataSource = mockk(relaxed = true)
    private val medicationPlanV1: MedicationPlanLocalDataSource = mockk(relaxed = true)
    private val medicationPlanV2: MedicationPlanLocalDataSource = mockk(relaxed = true)

    private lateinit var migrator: DefaultDataMigrator

    @Before
    fun setUp() {
        migrator = DefaultDataMigrator(
            profileV1, profileV2, userAuthV1, userAuthV2,
            pharmacyV1, pharmacyV2, searchTokenV1, searchTokenV2,
            internalMessageV1, internalMessageV2,
            settingsV1, settingsV2, invoiceV1, invoiceV2,
            taskV1, taskV2, communicationV1, communicationV2,
            idpConfigV1, idpConfigV2, trustStoreV1, trustStoreV2,
            shippingInfoV1, shippingInfoV2, appAuthV1, appAuthV2,
            euTaskV1, euTaskV2, medicationPlanV1, medicationPlanV2
        )
    }

    @Test
    fun `migration ports data for multiple profiles correctly`() = runTest {
        // GIVEN
        val profile1: ProfileErpModel = mockk(relaxed = true)
        every { profile1.id } returns "profile1"
        every { profile1.active } returns true

        val profile2: ProfileErpModel = mockk(relaxed = true)
        every { profile2.id } returns "profile2"
        every { profile2.active } returns false

        coEvery { profileV1.loadProfiles() } returns flowOf(listOf(profile1, profile2))
        coEvery { profileV2.loadProfiles() } returns flowOf(listOf(profile1, profile2))

        val taskSynced: TaskErpModel.Synced.Prescription = mockk(relaxed = true)
        every { taskSynced.taskId } returns "task1"

        coEvery { taskV2.loadATaskIdStringList() } returns flowOf(listOf("task1"))

        val taskScanned: TaskErpModel.Scanned = mockk(relaxed = true)
        every { taskScanned.taskId } returns "task2"

        val taskDiga: TaskErpModel.Synced.Diga = mockk(relaxed = true)
        every { taskDiga.taskId } returns "task3"

        coEvery { taskV1.loadTaskListByProfileId(any()) } returns flowOf(listOf(taskSynced, taskScanned, taskDiga))

        val pharmacy: PharmacyErpModel = mockk(relaxed = true)
        coEvery { pharmacyV1.loadPharmacies() } returns flowOf(listOf(pharmacy))
        coEvery { pharmacyV1.isPharmacyInFavorites(any()) } returns flowOf(true)
        coEvery { pharmacyV1.isPharmacyOftenUsed(any()) } returns flowOf(false)

        val invoice: PKVInvoiceErpModel = mockk(relaxed = true)
        every { invoice.taskId } returns "task1"
        every { invoice.profileId } returns "profile1"
        coEvery { invoiceV1.loadInvoices(any()) } returns flowOf(listOf(invoice))

        val euOrder: EuOrderErpModel = mockk(relaxed = true)
        coEvery { euTaskV1.observeAllEuOrders() } returns flowOf(listOf(euOrder))

        val medicationSchedule: MedicationScheduleErpModel = mockk(relaxed = true)
        every { medicationSchedule.taskId } returns "task1"
        every { medicationSchedule.profileId } returns "profile1"
        coEvery { medicationPlanV1.getAllMedicationSchedules() } returns flowOf(listOf(medicationSchedule))

        coEvery { internalMessageV1.getInternalMessages() } returns flowOf(emptyList())
        coEvery { settingsV1.loadSettings() } returns flowOf(mockk(relaxed = true))
        coEvery { userAuthV1.getUserAuthenticationForProfile(any()) } returns flowOf(mockk(relaxed = true))

        val comm: CommunicationErpModel = mockk(relaxed = true)
        every { comm.taskId } returns "task1"
        every { comm.communicationId } returns "comm1"
        every { comm.senderTelematikId } returns "tel1"

        coEvery { communicationV1.loadDispReqCommunicationsByProfileId(any()) } returns flowOf(listOf(comm))
        coEvery { communicationV1.loadRepliedCommunicationsByProfileId(any()) } returns flowOf(emptyList())
        coEvery { trustStoreV1.loadUntrusted() } returns flowOf(null)
        coEvery { appAuthV1.getAppAuthenticationErpModel() } returns flowOf(mockk(relaxed = true))

        // WHEN
        migrator.migrate()

        // THEN
        coVerify { profileV2.saveProfile(profile1) }
        coVerify { profileV2.saveProfile(profile2) }
        coVerify { taskV2.saveTask(taskSynced) }
        coVerify { taskV2.saveTask(taskScanned) }
        coVerify { taskV2.saveTask(taskDiga) }
        coVerify { pharmacyV2.markPharmacyAsFavourite(pharmacy) }
        coVerify { invoiceV2.saveInvoice(invoice) }
        coVerify { userAuthV2.saveUserAuthenticationForProfile(any(), any()) }
        coVerify { appAuthV2.initialiseAppAuthenticationEntity(any()) }
        coVerify { communicationV2.saveCommunications(listOf(comm)) }
        coVerify { euTaskV2.importMigratedOrder(euOrder) }
        coVerify { medicationPlanV2.importMigratedSchedule(medicationSchedule) }
    }

    @Test
    fun `migration failure is reported in progress`() = runTest {
        // GIVEN
        coEvery { profileV1.loadProfiles() } throws RuntimeException("Migration failed")

        // WHEN
        try {
            migrator.migrate()
        } catch (_: Exception) {
            // expected
        }

        // THEN
        assertNotNull(migrator.progress.value.error)
        assertTrue(migrator.progress.value.isFinished)
        coVerify(exactly = 0) { profileV2.saveProfile(any()) }
    }
}
