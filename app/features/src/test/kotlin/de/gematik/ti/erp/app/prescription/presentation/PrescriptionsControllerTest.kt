/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.prescription.presentation

import app.cash.turbine.test
import de.gematik.ti.erp.app.analytics.tracker.Tracker
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.model.DownloadResourcesState
import de.gematik.ti.erp.app.base.model.DownloadResourcesState.NotStarted
import de.gematik.ti.erp.app.base.usecase.DownloadAllResourcesUseCase
import de.gematik.ti.erp.app.consent.repository.ConsentRepository
import de.gematik.ti.erp.app.consent.usecase.ShowGrantConsentDrawerUseCase
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.idp.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SCANNED_TASK
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SYNCED_TASK
import de.gematik.ti.erp.app.mocks.prescription.api.API_ARCHIVE_SCANNED_TASK
import de.gematik.ti.erp.app.mocks.prescription.api.API_ARCHIVE_SYNCED_TASK
import de.gematik.ti.erp.app.mocks.prescription.model.MODEL_SCANNED_PRESCRIPTION_ACTIVE
import de.gematik.ti.erp.app.mocks.prescription.model.MODEL_SCANNED_PRESCRIPTION_ARCHIVED
import de.gematik.ti.erp.app.mocks.prescription.model.MODEL_SYNCED_PRESCRIPTION_ACTIVE
import de.gematik.ti.erp.app.mocks.prescription.model.MODEL_SYNCED_PRESCRIPTION_ARCHIVE
import de.gematik.ti.erp.app.mocks.profile.api.API_MOCK_PROFILE
import de.gematik.ti.erp.app.mocks.profile.api.API_MOCK_WITH_SSO_TOKEN_PROFILE
import de.gematik.ti.erp.app.mocks.settings.api.SETTINGS_DATA_GENERAL
import de.gematik.ti.erp.app.prescription.repository.DownloadResourcesStateRepository
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.repository.TaskRepository
import de.gematik.ti.erp.app.prescription.usecase.GetActivePrescriptionsUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetArchivedPrescriptionsUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetDownloadResourcesSnapshotStateUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.SwitchActiveProfileUseCase
import de.gematik.ti.erp.app.redeem.usecase.HasRedeemableTasksUseCase
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import de.gematik.ti.erp.app.settings.usecase.GetCanStartToolTipsUseCase
import de.gematik.ti.erp.app.settings.usecase.GetMLKitAcceptedUseCase
import de.gematik.ti.erp.app.settings.usecase.GetShowWelcomeDrawerUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveToolTipsShownUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isErrorState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isLoadingState
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.rules.TestWatcher
import kotlin.test.assertEquals

class PrescriptionsControllerTest : TestWatcher() {

    private val profileRepository: ProfileRepository = mockk()
    private val idpRepository: IdpRepository = mockk()
    private val taskRepository: TaskRepository = mockk()
    private val communicationRepository: CommunicationRepository = mockk()
    private val invoicesRepository: InvoiceRepository = mockk()
    private val downloadResourcesStateRepository: DownloadResourcesStateRepository = mockk()
    private val prescriptionRepository: PrescriptionRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val consentRepository: ConsentRepository = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val biometricAuthenticator = mockk<BiometricAuthenticator>()
    private val networkStatusTracker = mockk<NetworkStatusTracker>()
    private val tracker = mockk<Tracker>()
    private val testScope = TestScope(dispatcher)

    private lateinit var controllerUnderTest: PrescriptionsController
    private lateinit var getActiveProfileUseCase: GetActiveProfileUseCase
    private lateinit var getActivePrescriptionsUseCase: GetActivePrescriptionsUseCase
    private lateinit var getArchivedPrescriptionsUseCase: GetArchivedPrescriptionsUseCase
    private lateinit var getMLKitAcceptedUseCase: GetMLKitAcceptedUseCase
    private lateinit var getShowWelcomeDrawerUseCase: GetShowWelcomeDrawerUseCase
    private lateinit var getCanStartToolTipsUseCase: GetCanStartToolTipsUseCase
    private lateinit var saveToolTipsShownUseCase: SaveToolTipsShownUseCase
    private lateinit var showGrantConsentDrawerUseCase: ShowGrantConsentDrawerUseCase
    private lateinit var chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase
    private lateinit var getProfileByIdUseCase: GetProfileByIdUseCase
    private lateinit var getProfilesUseCase: GetProfilesUseCase
    private lateinit var switchActiveProfileUseCase: SwitchActiveProfileUseCase
    private lateinit var downloadAllResourcesUseCase: DownloadAllResourcesUseCase
    private lateinit var snapshotStateUseCase: GetDownloadResourcesSnapshotStateUseCase
    private lateinit var hasRedeemableTasksUseCase: HasRedeemableTasksUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        MockKAnnotations.init(this)

        val snapshotState = MutableSharedFlow<DownloadResourcesState>(replay = 1).asSharedFlow()
        val detailState = MutableStateFlow(NotStarted).asStateFlow()

        every { settingsRepository.general } returns flowOf(SETTINGS_DATA_GENERAL)
        every { profileRepository.activeProfile() } returns flowOf(API_MOCK_PROFILE)
        every { downloadResourcesStateRepository.snapshotState() } returns snapshotState
        every { downloadResourcesStateRepository.detailState() } returns detailState
        every { networkStatusTracker.networkStatus } returns flowOf(true)

        getActiveProfileUseCase = GetActiveProfileUseCase(
            repository = profileRepository,
            dispatcher = dispatcher
        )
        getActivePrescriptionsUseCase = GetActivePrescriptionsUseCase(
            repository = prescriptionRepository,
            dispatcher = dispatcher
        )
        getArchivedPrescriptionsUseCase = GetArchivedPrescriptionsUseCase(
            repository = prescriptionRepository,
            dispatcher = dispatcher
        )
        getMLKitAcceptedUseCase = GetMLKitAcceptedUseCase(
            settingsRepository = settingsRepository,
            dispatcher = dispatcher
        )
        getShowWelcomeDrawerUseCase = GetShowWelcomeDrawerUseCase(
            settingsRepository = settingsRepository,
            dispatcher = dispatcher
        )
        getCanStartToolTipsUseCase = GetCanStartToolTipsUseCase(
            settingsRepository = settingsRepository,
            dispatcher = dispatcher
        )
        saveToolTipsShownUseCase = SaveToolTipsShownUseCase(
            settingsRepository = settingsRepository,
            dispatcher = dispatcher
        )
        showGrantConsentDrawerUseCase = ShowGrantConsentDrawerUseCase(
            consentRepository = consentRepository,
            profilesRepository = profileRepository
        )
        chooseAuthenticationDataUseCase = ChooseAuthenticationDataUseCase(
            profileRepository = profileRepository,
            idpRepository = idpRepository,
            dispatcher = dispatcher
        )
        getProfileByIdUseCase = GetProfileByIdUseCase(
            repository = profileRepository,
            dispatcher = dispatcher
        )
        getProfilesUseCase = GetProfilesUseCase(
            repository = profileRepository,
            dispatcher = dispatcher
        )
        switchActiveProfileUseCase = SwitchActiveProfileUseCase(
            repository = profileRepository,
            dispatcher = dispatcher
        )
        downloadAllResourcesUseCase = DownloadAllResourcesUseCase(
            profileRepository = profileRepository,
            taskRepository = taskRepository,
            communicationRepository = communicationRepository,
            invoicesRepository = invoicesRepository,
            stateRepository = downloadResourcesStateRepository,
            networkStatusTracker = networkStatusTracker,
            dispatcher = dispatcher
        )
        snapshotStateUseCase = GetDownloadResourcesSnapshotStateUseCase(
            downloadResourcesStateRepository = downloadResourcesStateRepository
        )
        hasRedeemableTasksUseCase = HasRedeemableTasksUseCase(
            prescriptionRepository = prescriptionRepository,
            dispatchers = dispatcher
        )

        controllerUnderTest = PrescriptionsController(
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            snapshotStateUseCase = snapshotStateUseCase,
            biometricAuthenticator = biometricAuthenticator,
            getActiveProfileUseCase = getActiveProfileUseCase,
            downloadAllResourcesUseCase = downloadAllResourcesUseCase,
            activePrescriptionsUseCase = getActivePrescriptionsUseCase,
            archivedPrescriptionsUseCase = getArchivedPrescriptionsUseCase,
            getMLKitAcceptedUseCase = getMLKitAcceptedUseCase,
            getShowWelcomeDrawerUseCase = getShowWelcomeDrawerUseCase,
            showGrantConsentDrawerUseCase = showGrantConsentDrawerUseCase,
            saveToolTipsShownUseCase = saveToolTipsShownUseCase,
            switchActiveProfileUseCase = switchActiveProfileUseCase,
            hasRedeemableTasksUseCase = hasRedeemableTasksUseCase,
            tracker = tracker,
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
    fun `profile is empty and screen in error state`() {
        every { profileRepository.activeProfile() } throws Exception("Error")

        testScope.runTest {
            advanceUntilIdle()
            val profile = controllerUnderTest.activeProfile.first()
            assert(profile.isErrorState)
        }
    }

    @Test
    fun `profile is loading and screen in loading state`() {
        every { profileRepository.activeProfile() } returns flowOf(API_MOCK_PROFILE)

        testScope.runTest {
            controllerUnderTest.activeProfile.test {
                val profile = awaitItem()
                println(profile)
                assert(profile.isLoadingState)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `profile is loaded and screen in content state with no prescriptions`() {
        every { profileRepository.activeProfile() } returns flowOf(API_MOCK_PROFILE)
        every { prescriptionRepository.scannedTasks(any()) } returns flowOf(emptyList())
        every { prescriptionRepository.syncedTasks(any()) } returns flowOf(emptyList())

        testScope.runTest {
            advanceUntilIdle()
            val profile = controllerUnderTest.activeProfile.first()
            assert(profile.isDataState)
            controllerUnderTest.activePrescriptions.test {
                val initialEmittedState = awaitItem()
                assertEquals(UiState.Loading(), initialEmittedState)
                val finalEmittedState = awaitItem()
                assertEquals(UiState.Data(emptyList()), finalEmittedState)
            }
            controllerUnderTest.archivedPrescriptions.test {
                val initialEmittedState = awaitItem()
                assertEquals(UiState.Loading(), initialEmittedState)
                val finalEmittedState = awaitItem()
                assertEquals(UiState.Data(null), finalEmittedState)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `event is sent that one new prescriptions is loaded`() {
        every { profileRepository.activeProfile() } returns flowOf(API_MOCK_WITH_SSO_TOKEN_PROFILE)
        every { prescriptionRepository.scannedTasks(any()) } returns flowOf(listOf(API_ACTIVE_SCANNED_TASK))
        every { prescriptionRepository.syncedTasks(any()) } returns flowOf(emptyList())
        val controller = spyk(controllerUnderTest)
        testScope.runTest {
            advanceUntilIdle()
            // one new prescription is loaded from the backend which makes it available for the user
            every { prescriptionRepository.syncedTasks(any()) } returns flowOf(listOf(API_ACTIVE_SYNCED_TASK))
            controller.refreshDownload()
            advanceTimeBy(1000)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loaded with only active scanned prescriptions`() {
        every { profileRepository.activeProfile() } returns flowOf(API_MOCK_PROFILE)
        every { prescriptionRepository.scannedTasks(any()) } returns flowOf(listOf(API_ACTIVE_SCANNED_TASK))
        every { prescriptionRepository.syncedTasks(any()) } returns flowOf(emptyList())

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.activePrescriptions.test {
                awaitItem() // initial emit should be always loading
                val emittedState = awaitItem()
                assertEquals(UiState.Data(listOf(MODEL_SCANNED_PRESCRIPTION_ACTIVE)), emittedState)
            }
            controllerUnderTest.archivedPrescriptions.test {
                awaitItem() // initial emit should be always loading
                val emittedState = awaitItem()
                assertEquals(UiState.Data(null), emittedState)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loaded with only active synced prescriptions`() {
        every { profileRepository.activeProfile() } returns flowOf(API_MOCK_PROFILE)
        every { prescriptionRepository.scannedTasks(any()) } returns flowOf(emptyList())
        every { prescriptionRepository.syncedTasks(any()) } returns flowOf(listOf(API_ACTIVE_SYNCED_TASK))

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.activePrescriptions.test {
                awaitItem() // initial emit should be always loading
                val emittedState = awaitItem()
                assertEquals(UiState.Data(listOf(MODEL_SYNCED_PRESCRIPTION_ACTIVE)), emittedState)
            }
            controllerUnderTest.archivedPrescriptions.test {
                awaitItem() // initial emit should be always loading
                val emittedState = awaitItem()
                assertEquals(UiState.Data(null), emittedState)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loaded with only archived scanned prescriptions`() {
        every { profileRepository.activeProfile() } returns flowOf(API_MOCK_PROFILE)
        every { prescriptionRepository.scannedTasks(any()) } returns flowOf(listOf(API_ARCHIVE_SCANNED_TASK))
        every { prescriptionRepository.syncedTasks(any()) } returns flowOf(emptyList())

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.activePrescriptions.test {
                awaitItem() // initial emit should be always loading
                val emittedState = awaitItem()
                assertEquals(UiState.Data(emptyList()), emittedState)
            }
            controllerUnderTest.archivedPrescriptions.test {
                awaitItem() // initial emit should be always loading
                val emittedState = awaitItem()
                assertEquals(UiState.Data(listOf(MODEL_SCANNED_PRESCRIPTION_ARCHIVED)), emittedState)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loaded with only archived synced prescriptions`() {
        every { profileRepository.activeProfile() } returns flowOf(API_MOCK_PROFILE)
        every { prescriptionRepository.scannedTasks(any()) } returns flowOf(emptyList())
        every { prescriptionRepository.syncedTasks(any()) } returns flowOf(listOf(API_ARCHIVE_SYNCED_TASK))

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.activePrescriptions.test {
                awaitItem() // initial emit should be always loading
                val emittedState = awaitItem()
                assertEquals(UiState.Data(emptyList()), emittedState)
            }
            controllerUnderTest.archivedPrescriptions.test {
                awaitItem() // initial emit should be always loading
                val emittedState = awaitItem()
                assertEquals(UiState.Data(listOf(MODEL_SYNCED_PRESCRIPTION_ARCHIVE)), emittedState)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loaded with only synced prescriptions`() {
        every { profileRepository.activeProfile() } returns flowOf(API_MOCK_PROFILE)
        every { prescriptionRepository.scannedTasks(any()) } returns flowOf(emptyList())
        every { prescriptionRepository.syncedTasks(any()) } returns flowOf(
            listOf(API_ACTIVE_SYNCED_TASK, API_ARCHIVE_SYNCED_TASK)
        )

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.activePrescriptions.test {
                awaitItem() // initial emit should be always loading
                val emittedState = awaitItem()
                assertEquals(UiState.Data(listOf(MODEL_SYNCED_PRESCRIPTION_ACTIVE)), emittedState)
            }
            controllerUnderTest.archivedPrescriptions.test {
                awaitItem() // initial emit should be always loading
                val emittedState = awaitItem()
                assertEquals(UiState.Data(listOf(MODEL_SYNCED_PRESCRIPTION_ARCHIVE)), emittedState)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loaded with only scanned prescriptions`() {
        every { profileRepository.activeProfile() } returns flowOf(API_MOCK_PROFILE)
        every { prescriptionRepository.scannedTasks(any()) } returns flowOf(
            listOf(API_ACTIVE_SCANNED_TASK, API_ARCHIVE_SCANNED_TASK)
        )
        every { prescriptionRepository.syncedTasks(any()) } returns flowOf(emptyList())

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.activePrescriptions.test {
                awaitItem() // initial emit should be always loading
                val emittedState = awaitItem()
                assertEquals(UiState.Data(listOf(MODEL_SCANNED_PRESCRIPTION_ACTIVE)), emittedState)
            }
            controllerUnderTest.archivedPrescriptions.test {
                awaitItem() // initial emit should be always loading
                val emittedState = awaitItem()
                assertEquals(UiState.Data(listOf(MODEL_SCANNED_PRESCRIPTION_ARCHIVED)), emittedState)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loaded with synced and scanned prescriptions`() {
        every { profileRepository.activeProfile() } returns flowOf(API_MOCK_PROFILE)
        every { prescriptionRepository.scannedTasks(any()) } returns flowOf(
            listOf(API_ACTIVE_SCANNED_TASK, API_ARCHIVE_SCANNED_TASK)
        )
        every { prescriptionRepository.syncedTasks(any()) } returns flowOf(
            listOf(API_ACTIVE_SYNCED_TASK, API_ARCHIVE_SYNCED_TASK)
        )

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.activePrescriptions.test {
                awaitItem() // initial emit should be always loading
                val emittedState = awaitItem()
                assertEquals(UiState.Data(listOf(MODEL_SYNCED_PRESCRIPTION_ACTIVE, MODEL_SCANNED_PRESCRIPTION_ACTIVE)), emittedState)
            }
            controllerUnderTest.archivedPrescriptions.test {
                awaitItem() // initial emit should be always loading
                val emittedState = awaitItem()
                assertEquals(UiState.Data(listOf(MODEL_SYNCED_PRESCRIPTION_ARCHIVE, MODEL_SCANNED_PRESCRIPTION_ARCHIVED)), emittedState)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `ml-kit acceptance test`() {
        coEvery { prescriptionRepository.scannedTasks(any()) } returns flowOf(emptyList())
        coEvery { prescriptionRepository.syncedTasks(any()) } returns flowOf(emptyList())

        testScope.runTest {
            advanceUntilIdle()
            assert(!controllerUnderTest.isMLKitAccepted.first())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `get the state to check if the welcome drawer needs to be shown`() {
        coEvery { prescriptionRepository.scannedTasks(any()) } returns flowOf(emptyList())
        coEvery { prescriptionRepository.syncedTasks(any()) } returns flowOf(emptyList())
        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.shouldShowWelcomeDrawer.test {
                val emittedState = awaitItem()
                awaitComplete()
                assertEquals(true, emittedState)
            }
        }
        verify(atMost = 1) { getShowWelcomeDrawerUseCase() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `get the state to check if the welcome drawer is already shown`() {
        every { settingsRepository.general } returns flowOf(SETTINGS_DATA_GENERAL.copy(welcomeDrawerShown = true))
        coEvery { prescriptionRepository.scannedTasks(any()) } returns flowOf(emptyList())
        coEvery { prescriptionRepository.syncedTasks(any()) } returns flowOf(emptyList())

        val showWelcomeDrawerUseCase = GetShowWelcomeDrawerUseCase(settingsRepository, dispatcher)

        val controller = PrescriptionsController(
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            snapshotStateUseCase = snapshotStateUseCase,
            biometricAuthenticator = biometricAuthenticator,
            getActiveProfileUseCase = getActiveProfileUseCase,
            downloadAllResourcesUseCase = downloadAllResourcesUseCase,
            activePrescriptionsUseCase = getActivePrescriptionsUseCase,
            archivedPrescriptionsUseCase = getArchivedPrescriptionsUseCase,
            getMLKitAcceptedUseCase = getMLKitAcceptedUseCase,
            getShowWelcomeDrawerUseCase = showWelcomeDrawerUseCase,
            showGrantConsentDrawerUseCase = showGrantConsentDrawerUseCase,
            saveToolTipsShownUseCase = saveToolTipsShownUseCase,
            switchActiveProfileUseCase = switchActiveProfileUseCase,
            hasRedeemableTasksUseCase = hasRedeemableTasksUseCase,
            tracker = tracker,
            networkStatusTracker = networkStatusTracker
        )

        testScope.runTest {
            advanceUntilIdle()
            controller.shouldShowWelcomeDrawer.test {
                val emittedState = awaitItem()
                awaitComplete()
                assertEquals(false, emittedState)
            }
        }
        // one call is from setup, the other one is from the test
        verify(atMost = 2) { getShowWelcomeDrawerUseCase() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test tracking`() {
        every { prescriptionRepository.scannedTasks(any()) } returns flowOf(listOf(API_ACTIVE_SCANNED_TASK, API_ARCHIVE_SCANNED_TASK))
        every { prescriptionRepository.syncedTasks(any()) } returns flowOf(listOf(API_ACTIVE_SYNCED_TASK, API_ARCHIVE_SYNCED_TASK))
        coEvery { tracker.trackEvent(any()) } returns Unit
        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.trackPrescriptionCounts()
        }

        coVerify(exactly = 3) { tracker.trackEvent(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `check if archive is not empty`() {
        every { prescriptionRepository.scannedTasks(any()) } returns flowOf(listOf(API_ACTIVE_SCANNED_TASK, API_ARCHIVE_SCANNED_TASK))
        every { prescriptionRepository.syncedTasks(any()) } returns flowOf(listOf(API_ACTIVE_SYNCED_TASK, API_ARCHIVE_SYNCED_TASK))
        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.isArchiveEmpty.test {
                awaitItem()
                val emittedState = awaitItem()
                assertEquals(false, emittedState)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `check if archive is empty as the initial state`() {
        every { prescriptionRepository.scannedTasks(any()) } returns flowOf(listOf(API_ACTIVE_SCANNED_TASK))
        every { prescriptionRepository.syncedTasks(any()) } returns flowOf(listOf(API_ACTIVE_SYNCED_TASK))
        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.isArchiveEmpty.test {
                val emittedState = awaitItem()
                assertEquals(true, emittedState)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `save the state of the tooltips once the user has accepted it`() {
        coJustRun { settingsRepository.saveMainScreenTooltipShown() }
        coEvery { prescriptionRepository.scannedTasks(any()) } returns flowOf(emptyList())
        coEvery { prescriptionRepository.syncedTasks(any()) } returns flowOf(emptyList())

        val saveToolTipsShownUseCase = spyk(SaveToolTipsShownUseCase(settingsRepository, dispatcher))
        val controllerUnderTest = PrescriptionsController(
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            snapshotStateUseCase = snapshotStateUseCase,
            biometricAuthenticator = biometricAuthenticator,
            getActiveProfileUseCase = getActiveProfileUseCase,
            downloadAllResourcesUseCase = downloadAllResourcesUseCase,
            activePrescriptionsUseCase = getActivePrescriptionsUseCase,
            archivedPrescriptionsUseCase = getArchivedPrescriptionsUseCase,
            getMLKitAcceptedUseCase = getMLKitAcceptedUseCase,
            getShowWelcomeDrawerUseCase = getShowWelcomeDrawerUseCase,
            showGrantConsentDrawerUseCase = showGrantConsentDrawerUseCase,
            saveToolTipsShownUseCase = saveToolTipsShownUseCase,
            switchActiveProfileUseCase = switchActiveProfileUseCase,
            hasRedeemableTasksUseCase = hasRedeemableTasksUseCase,
            tracker = tracker,
            networkStatusTracker = networkStatusTracker
        )
        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.saveToolTipsShown()
        }
        coVerify(exactly = 1) { saveToolTipsShownUseCase.invoke() }
    }
}
