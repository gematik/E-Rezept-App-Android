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

package de.gematik.ti.erp.app.pkv.presentation

import de.gematik.ti.erp.app.api.HttpErrorState
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.fhir.temporal.Year
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.invoice.model.InvoiceResult.InvoiceError
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.invoice.usecase.DeleteAllLocalInvoices
import de.gematik.ti.erp.app.invoice.usecase.DeleteInvoiceUseCase
import de.gematik.ti.erp.app.invoice.usecase.DownloadInvoicesUseCase
import de.gematik.ti.erp.app.invoice.usecase.GetInvoiceByTaskIdUseCase
import de.gematik.ti.erp.app.invoice.usecase.GetInvoicesByProfileUseCase
import de.gematik.ti.erp.app.mocks.PROFILE_ID
import de.gematik.ti.erp.app.mocks.TASK_ID
import de.gematik.ti.erp.app.mocks.invoice.model.mockPkvInvoiceRecord
import de.gematik.ti.erp.app.mocks.invoice.model.mockedInvoiceChargeItemBundle
import de.gematik.ti.erp.app.mocks.profile.api.API_MOCK_PROFILE
import de.gematik.ti.erp.app.pkv.usecase.ShareInvoiceUseCase
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.uistate.UiState
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.rules.TestWatcher

class InvoiceControllerTest : TestWatcher() {

    private val invoiceRepository: InvoiceRepository = mockk()
    private val profileRepository: ProfileRepository = mockk()
    private val idpRepository: IdpRepository = mockk()
    private val biometricAuthenticator = mockk<BiometricAuthenticator>()
    private val networkStatusTracker = mockk<NetworkStatusTracker>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(dispatcher)

    // events
    private val askUserToLoginEvent: ComposableEvent<Unit> = spyk(ComposableEvent())
    private val deleteSuccessfulEvent: ComposableEvent<Unit> = spyk(ComposableEvent())
    private val downloadCompletedEvent: ComposableEvent<Unit> = spyk(ComposableEvent())
    private val invoiceErrorEvent: ComposableEvent<InvoiceError> = spyk(ComposableEvent())
    private val getConsentEvent: ComposableEvent<ProfileIdentifier> = spyk(ComposableEvent())
    private val showAuthenticationErrorDialog: ComposableEvent<AuthenticationResult.Error> = spyk(ComposableEvent())

    private lateinit var getProfileByIdUseCase: GetProfileByIdUseCase
    private lateinit var getProfilesUseCase: GetProfilesUseCase
    private lateinit var getActiveProfileUseCase: GetActiveProfileUseCase
    private lateinit var chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase
    private lateinit var downloadInvoiceUseCase: DownloadInvoicesUseCase
    private lateinit var getInvoicesByProfileUseCase: GetInvoicesByProfileUseCase
    private lateinit var getInvoiceByTaskIdUseCase: GetInvoiceByTaskIdUseCase
    private lateinit var deleteInvoiceUseCase: DeleteInvoiceUseCase
    private lateinit var deleteAllInvoicesUseCase: DeleteAllLocalInvoices
    private lateinit var shareInvoiceUseCase: ShareInvoiceUseCase

    private lateinit var controllerUnderTest: InvoiceController

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        MockKAnnotations.init(this)

        every { networkStatusTracker.networkStatus } returns flowOf(true)

        getProfileByIdUseCase = GetProfileByIdUseCase(profileRepository, dispatcher)
        getProfilesUseCase = GetProfilesUseCase(profileRepository, dispatcher)
        getActiveProfileUseCase = GetActiveProfileUseCase(profileRepository, dispatcher)
        chooseAuthenticationDataUseCase =
            ChooseAuthenticationDataUseCase(profileRepository, idpRepository, dispatcher)
        downloadInvoiceUseCase = DownloadInvoicesUseCase(profileRepository, invoiceRepository, dispatcher)
        getInvoicesByProfileUseCase = GetInvoicesByProfileUseCase(invoiceRepository, TimeZone.of("UTC"), dispatcher)
        getInvoiceByTaskIdUseCase = GetInvoiceByTaskIdUseCase(invoiceRepository, dispatcher)
        deleteInvoiceUseCase = spyk(DeleteInvoiceUseCase(profileRepository, invoiceRepository, dispatcher))
        deleteAllInvoicesUseCase = DeleteAllLocalInvoices(invoiceRepository, dispatcher)
        shareInvoiceUseCase = ShareInvoiceUseCase(invoiceRepository, dispatcher)

        controllerUnderTest = InvoiceController(
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            biometricAuthenticator = biometricAuthenticator,
            profileId = PROFILE_ID,
            downloadInvoicesUseCase = downloadInvoiceUseCase,
            getInvoicesByProfileUseCase = getInvoicesByProfileUseCase,
            getInvoiceByTaskIdUseCase = getInvoiceByTaskIdUseCase,
            deleteInvoiceUseCase = deleteInvoiceUseCase,
            deleteAllInvoicesUseCase = deleteAllInvoicesUseCase,
            shareInvoiceUseCase = shareInvoiceUseCase,
            invoiceDetailScreenEvents = InvoiceDetailScreenEvents(
                askUserToLoginEvent = askUserToLoginEvent,
                deleteSuccessfulEvent = deleteSuccessfulEvent
            ),
            invoiceListScreenEvents = InvoiceListScreenEvents(
                downloadCompletedEvent = downloadCompletedEvent,
                invoiceErrorEvent = invoiceErrorEvent,
                getConsentEvent = getConsentEvent,
                showAuthenticationErrorDialog = showAuthenticationErrorDialog
            ),
            networkStatusTracker = networkStatusTracker
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `getting the list of invoices with loading state at start`() {
        coEvery { profileRepository.getProfileById(PROFILE_ID) } returns flowOf(API_MOCK_PROFILE)
        coEvery { invoiceRepository.invoices(PROFILE_ID) } returns flowOf(listOf(mockPkvInvoiceRecord()))
        coEvery { invoiceRepository.deleteLocalInvoiceById(TASK_ID) } returns Unit

        val result = mapOf(Year(2024) to listOf(mockPkvInvoiceRecord()))
        testScope.runTest {
            advanceUntilIdle()
            val item = controllerUnderTest.invoices.first()
            assertEquals(UiState.Data(result), item)
            coVerify(exactly = 1) { getInvoicesByProfileUseCase.invoke(PROFILE_ID) }
            coVerify(exactly = 1) { invoiceRepository.invoices(PROFILE_ID) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `deleting the invoice when the user is not logged in`() {
        every { askUserToLoginEvent.trigger(Unit) } just Runs
        coEvery { profileRepository.isSsoTokenValid(PROFILE_ID) } returns flowOf(false)
        coEvery { invoiceRepository.deleteRemoteInvoiceById(TASK_ID, PROFILE_ID) } returns Result.success(Unit)
        coEvery { invoiceRepository.invoices(PROFILE_ID) } returns flowOf(listOf(mockPkvInvoiceRecord(PROFILE_ID)))

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.deleteInvoice(TASK_ID, PROFILE_ID)
            verify(exactly = 1) { askUserToLoginEvent.trigger(Unit) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `deleting the invoice and it returns an error`() {
        val error = InvoiceError(HttpErrorState.Unknown)
        every { invoiceErrorEvent.trigger(error) } just Runs
        coEvery { profileRepository.isSsoTokenValid(PROFILE_ID) } returns flowOf(true)
        coEvery { invoiceRepository.deleteRemoteInvoiceById(TASK_ID, PROFILE_ID) } returns Result.failure(InvoiceError(HttpErrorState.Unknown))
        coEvery { invoiceRepository.deleteLocalInvoiceById(TASK_ID) } returns Unit
        coEvery { invoiceRepository.invoices(PROFILE_ID) } returns flowOf(listOf(mockPkvInvoiceRecord(PROFILE_ID)))

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.deleteInvoice(TASK_ID, PROFILE_ID)
            verify(exactly = 1) { invoiceErrorEvent.trigger(error) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `deleting the invoice successfully`() {
        coEvery { profileRepository.isSsoTokenValid(PROFILE_ID) } returns flowOf(true)
        coEvery { invoiceRepository.deleteRemoteInvoiceById(TASK_ID, PROFILE_ID) } returns Result.success(Unit)
        coEvery { invoiceRepository.deleteLocalInvoiceById(TASK_ID) } returns Unit
        coEvery { invoiceRepository.invoices(PROFILE_ID) } returns flowOf(listOf(mockPkvInvoiceRecord(PROFILE_ID)))

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.deleteInvoice(TASK_ID, PROFILE_ID)
            verify(exactly = 0) { invoiceErrorEvent.trigger(any()) }
            verify(exactly = 0) { askUserToLoginEvent.trigger(Unit) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `downloading the invoice when the user is not logged in`() {
        every { askUserToLoginEvent.trigger(Unit) } just Runs
        coEvery { profileRepository.isSsoTokenValid(PROFILE_ID) } returns flowOf(false)

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.downloadInvoices(PROFILE_ID)
            verify(exactly = 1) { askUserToLoginEvent.trigger(Unit) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `downloading the invoices and it returns an error on no charge item bundle`() {
        val error = InvoiceError(HttpErrorState.Unknown)
        val latestTimestamp = "2024-01-01T00:00:00Z"
        coEvery { profileRepository.isSsoTokenValid(PROFILE_ID) } returns flowOf(true)
        coEvery { invoiceRepository.getLatestTimeStamp(PROFILE_ID) } returns flowOf(latestTimestamp)
        coEvery { invoiceRepository.downloadChargeItemBundle(PROFILE_ID, latestTimestamp) } returns Result.failure(error)
        coEvery { invoiceRepository.invoices(PROFILE_ID) } returns flowOf(listOf(mockPkvInvoiceRecord(PROFILE_ID)))

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.downloadInvoices(PROFILE_ID)
            verify(exactly = 1) { invoiceErrorEvent.trigger(error) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `downloading the invoices and it returns an error on no charge item from task id`() {
        val error = InvoiceError(HttpErrorState.Unknown)
        val latestTimestamp = "2024-01-01T00:00:00Z"
        coEvery { profileRepository.isSsoTokenValid(PROFILE_ID) } returns flowOf(true)
        coEvery { invoiceRepository.getLatestTimeStamp(PROFILE_ID) } returns flowOf(latestTimestamp)
        coEvery { invoiceRepository.downloadChargeItemBundle(PROFILE_ID, latestTimestamp) } returns Result.success(
            mockedInvoiceChargeItemBundle(listOf(TASK_ID))
        )
        coEvery { invoiceRepository.downloadChargeItemByTaskId(PROFILE_ID, TASK_ID) } returns Result.failure(error)
        coEvery { invoiceRepository.invoices(PROFILE_ID) } returns flowOf(listOf(mockPkvInvoiceRecord(PROFILE_ID)))

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.downloadInvoices(PROFILE_ID)
            verify(exactly = 1) { invoiceErrorEvent.trigger(error) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `downloading the invoices successfully`() {
        val someJson: JsonElement = JsonPrimitive("success")
        val latestTimestamp = "2024-01-01T00:00:00Z"
        coEvery { profileRepository.isSsoTokenValid(PROFILE_ID) } returns flowOf(true)
        coEvery { invoiceRepository.getLatestTimeStamp(PROFILE_ID) } returns flowOf(latestTimestamp)
        coEvery { invoiceRepository.downloadChargeItemBundle(PROFILE_ID, latestTimestamp) } returns Result.success(
            mockedInvoiceChargeItemBundle(listOf(TASK_ID))
        )
        coEvery { invoiceRepository.downloadChargeItemByTaskId(PROFILE_ID, TASK_ID) } returns Result.success(someJson)
        coEvery { invoiceRepository.saveInvoice(PROFILE_ID, someJson) } returns Unit
        coEvery { invoiceRepository.invoices(PROFILE_ID) } returns flowOf(listOf(mockPkvInvoiceRecord(PROFILE_ID)))

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.downloadInvoices(PROFILE_ID)
            verify(exactly = 0) { invoiceErrorEvent.trigger(any()) }
            verify(exactly = 0) { askUserToLoginEvent.trigger(Unit) }
        }
    }
}
