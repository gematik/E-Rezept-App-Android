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

@file:Suppress("UnusedPrivateMember")

package de.gematik.ti.erp.app.prescription.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.analytics.model.TrackedEvent.ArchivePrescriptionCount
import de.gematik.ti.erp.app.analytics.model.TrackedEvent.ScannedPrescriptionCount
import de.gematik.ti.erp.app.analytics.model.TrackedEvent.SyncedPrescriptionCount
import de.gematik.ti.erp.app.analytics.tracker.Tracker
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.httpErrorState
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult
import de.gematik.ti.erp.app.authentication.model.ChooseAuthenticationController
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.model.DownloadResourcesState
import de.gematik.ti.erp.app.base.usecase.DownloadAllResourcesUseCase
import de.gematik.ti.erp.app.consent.usecase.ShowGrantConsentDrawerUseCase
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.core.complexAutoSaver
import de.gematik.ti.erp.app.idp.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.mainscreen.model.MultiProfileAppBarWrapper
import de.gematik.ti.erp.app.mainscreen.model.ProfileLifecycleState
import de.gematik.ti.erp.app.prescription.model.PrescriptionErrorState
import de.gematik.ti.erp.app.prescription.usecase.GetActivePrescriptionsUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetArchivedPrescriptionsUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetDownloadResourcesSnapshotStateUseCase
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription.Companion.countByType
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.SwitchActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile
import de.gematik.ti.erp.app.redeem.usecase.HasRedeemableTasksUseCase
import de.gematik.ti.erp.app.settings.usecase.GetMLKitAcceptedUseCase
import de.gematik.ti.erp.app.settings.usecase.GetShowWelcomeDrawerUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveToolTipsShownUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveWelcomeDrawerShownUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isNotDataState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

private val TAG = PrescriptionsController::class.qualifiedName.toString()

@Suppress("ConstructorParameterNaming", "LongParameterList")
@Stable
class PrescriptionsController(
    getProfileByIdUseCase: GetProfileByIdUseCase,
    getProfilesUseCase: GetProfilesUseCase,
    chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase,
    snapshotStateUseCase: GetDownloadResourcesSnapshotStateUseCase,
    biometricAuthenticator: BiometricAuthenticator,
    private val tracker: Tracker,
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val downloadAllResourcesUseCase: DownloadAllResourcesUseCase,
    private val activePrescriptionsUseCase: GetActivePrescriptionsUseCase,
    private val archivedPrescriptionsUseCase: GetArchivedPrescriptionsUseCase,
    private val getMLKitAcceptedUseCase: GetMLKitAcceptedUseCase,
    private val getShowWelcomeDrawerUseCase: GetShowWelcomeDrawerUseCase,
    private val showGrantConsentDrawerUseCase: ShowGrantConsentDrawerUseCase,
    private val saveToolTipsShownUseCase: SaveToolTipsShownUseCase,
    private val switchActiveProfileUseCase: SwitchActiveProfileUseCase,
    private val hasRedeemableTasksUseCase: HasRedeemableTasksUseCase,
    private val networkStatusTracker: NetworkStatusTracker,
    // events
    val refreshEvent: ComposableEvent<Boolean> = ComposableEvent(),
    val onUserNotAuthenticatedErrorEvent: ComposableEvent<Unit> = ComposableEvent(),
    private val localPrescriptionsRefreshTrigger: MutableStateFlow<Boolean> = MutableStateFlow(false)

) : ChooseAuthenticationController(
    getProfileByIdUseCase = getProfileByIdUseCase,
    getActiveProfileUseCase = getActiveProfileUseCase,
    getProfilesUseCase = getProfilesUseCase,
    chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
    biometricAuthenticator = biometricAuthenticator,
    networkStatusTracker = networkStatusTracker,
    onActiveProfileSuccess = { _, scope ->
        scope.launch {
            localPrescriptionsRefreshTrigger.value = !localPrescriptionsRefreshTrigger.value
        }
    }
) {
    private val _isProfileRefreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        biometricAuthenticationSuccessEvent.listen(controllerScope) { refreshDownload() }

        biometricAuthenticationResetErrorEvent.listen(controllerScope) { error ->
            if (error is AuthenticationResult.IdpCommunicationError.UserNotAuthenticated) {
                onUserNotAuthenticatedErrorEvent.trigger()
            }
        }

        onRefreshProfileAction.listen(controllerScope) { isRefreshing ->
            Napier.i(tag = "Profile") { "profile refresh state $isRefreshing" }
            updateProfileRefreshingState(isRefreshing)
        }
    }

    private fun loadPrescriptions() {
        localPrescriptionsRefreshTrigger.value = !localPrescriptionsRefreshTrigger.value
    }

    private fun updateProfileRefreshingState(isRefreshing: Boolean) {
        _isProfileRefreshing.value = isRefreshing
    }

    private fun downloadAllResources() {
        controllerScope.launch {
            val profile = activeProfile.extract()
            if (profile != null && profile.isSSOTokenValid()) {
                disableProfileRefresh()
                enablePrescriptionsRefresh()
                downloadAllResourcesUseCase.invoke(profile.id)
                    .fold(
                        onSuccess = { numberOfNewPrescriptions ->
                            Napier.d { "Download successful with $numberOfNewPrescriptions prescriptions" }
                            loadPrescriptions()
                        },
                        onFailure = { exception ->
                            handleError(exception) { error ->
                                loadPrescriptions()
                                disablePrescriptionRefresh()
                                Napier.e { "error: $error" }
                            }
                        }
                    )
            }
        }
    }

    private fun disableProfileRefresh() {
        onRefreshProfileAction.trigger(false)
    }

    private fun enablePrescriptionsRefresh() {
        refreshEvent.trigger(true)
    }

    fun disablePrescriptionRefresh() {
        refreshEvent.trigger(false)
    }

    private fun handleError(error: Throwable, tag: String = TAG, onFailure: ((PrescriptionErrorState) -> Unit)? = null) {
        val errorState: PrescriptionErrorState = when (error) {
            is ApiCallException -> PrescriptionErrorState.HttpError(errorState = error.response.httpErrorState())
            else -> PrescriptionErrorState.UnknownError
        }
        Napier.e(tag = tag) { "Error on download: ${error.stackTraceToString()}" }
        onFailure?.invoke(errorState)
    }

    fun refreshDownload() {
        downloadAllResources()
    }

    fun saveToolTipsShown() {
        controllerScope.launch {
            saveToolTipsShownUseCase()
        }
    }

    fun switchActiveProfile(id: ProfileIdentifier) {
        controllerScope.launch {
            switchActiveProfileUseCase.invoke(id)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val archivedPrescriptions: StateFlow<UiState<List<Prescription>>> by lazy {
        localPrescriptionsRefreshTrigger
            .flatMapLatest {
                activeProfile.extract()?.id?.let { selectedProfileId ->
                    archivedPrescriptionsUseCase.invoke(selectedProfileId).map {
                        when {
                            it.isEmpty() -> UiState.Empty()
                            else -> UiState.Data(it)
                        }
                    }
                } ?: emptyFlow()
            }.stateIn(
                scope = controllerScope,
                initialValue = UiState.Loading(),
                started = SharingStarted.WhileSubscribed()
            )
    }

    private val isArchiveDataEmpty by lazy {
        archivedPrescriptions.map { it.data?.isEmpty() == true }
    }

    private val isArchiveNotLoaded by lazy {
        archivedPrescriptions.map { it.isNotDataState }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val activePrescriptions: StateFlow<UiState<List<Prescription>>> by lazy {
        localPrescriptionsRefreshTrigger
            .flatMapLatest {
                activeProfile.extract()?.id?.let { selectedProfileId ->
                    activePrescriptionsUseCase.invoke(selectedProfileId).map { UiState.Data(it) }
                } ?: emptyFlow()
            }.stateIn(
                scope = controllerScope,
                initialValue = UiState.Loading(),
                started = SharingStarted.WhileSubscribed()
            )
    }

    val isArchiveEmpty: StateFlow<Boolean> by lazy {
        combine(isArchiveDataEmpty, isArchiveNotLoaded) { isEmpty, isNotLoaded ->
            isEmpty || isNotLoaded
        }.stateIn(
            scope = controllerScope,
            initialValue = true,
            started = SharingStarted.WhileSubscribed()
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val hasRedeemableTasks by lazy {
        getActiveProfileUseCase().flatMapLatest { profile ->
            hasRedeemableTasksUseCase(profile.id)
        }.stateIn(
            scope = controllerScope,
            initialValue = false,
            started = SharingStarted.WhileSubscribed()
        )
    }

    val isMLKitAccepted by lazy {
        getMLKitAcceptedUseCase().stateIn(controllerScope, Lazily, false)
    }

    val shouldShowWelcomeDrawer by lazy { getShowWelcomeDrawerUseCase() }

    val shouldShowGrantConsentDrawer by lazy { showGrantConsentDrawerUseCase() }

    val resourcesDownloadedState: SharedFlow<DownloadResourcesState> = snapshotStateUseCase()

    val multiProfileData by lazy {

        getActiveProfileUseCase()
            .map { it.isSSOTokenValid() }
            .stateIn(controllerScope, Eagerly, MultiProfileAppBarWrapper.DEFAULT_EMPTY_PROFILE)

        MultiProfileAppBarWrapper(
            existingProfiles = getProfilesUseCase().stateIn(controllerScope, Eagerly, listOf(MultiProfileAppBarWrapper.DEFAULT_EMPTY_PROFILE)),
            activeProfile = getActiveProfileUseCase().stateIn(controllerScope, Eagerly, MultiProfileAppBarWrapper.DEFAULT_EMPTY_PROFILE),
            profileLifecycleState = ProfileLifecycleState(
                networkStatus = networkStatusTracker.networkStatus.stateIn(controllerScope, Eagerly, false),
                isProfileRefreshing = _isProfileRefreshing.asStateFlow(),
                isTokenValid = getActiveProfileUseCase().map { it.isSSOTokenValid() }
                    .stateIn(controllerScope, Eagerly, false),
                isRegistered = getActiveProfileUseCase().map { it.lastAuthenticated != null }
                    .stateIn(controllerScope, Eagerly, false)
            )
        )
    }

    fun trackPrescriptionCounts() {
        controllerScope.launch {
            with(tracker) {
                activePrescriptions.syncedCount()?.let { trackEvent(SyncedPrescriptionCount(it)) }
                activePrescriptions.scannedCount()?.let { trackEvent(ScannedPrescriptionCount(it)) }
                archivedPrescriptions.count()?.let { trackEvent(ArchivePrescriptionCount(it)) }
            }
        }
    }

    companion object {
        private suspend fun StateFlow<UiState<Profile>>.extract(): Profile? = first { it.isDataState }.data

        private suspend fun StateFlow<UiState<List<Prescription>>>.syncedCount(): Int? =
            countByType(Prescription.SyncedPrescription::class.java)

        private suspend fun StateFlow<UiState<List<Prescription>>>.scannedCount(): Int? =
            countByType(Prescription.ScannedPrescription::class.java)

        private suspend fun StateFlow<UiState<List<Prescription>>>.count(): Int? =
            first { it.isDataState }.data?.takeIf { it.isNotEmpty() }?.count()
    }
}

@Composable
fun rememberPrescriptionsController(): PrescriptionsController {
    val activePrescriptionsUseCase by rememberInstance<GetActivePrescriptionsUseCase>()
    val archivedPrescriptionsUseCase by rememberInstance<GetArchivedPrescriptionsUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val switchActiveProfileUseCase by rememberInstance<SwitchActiveProfileUseCase>()
    val mlKitAcceptedUseCase by rememberInstance<GetMLKitAcceptedUseCase>()
    val getShowWelcomeDrawerUseCase by rememberInstance<GetShowWelcomeDrawerUseCase>()
    val showGrantConsentDrawerUseCase by rememberInstance<ShowGrantConsentDrawerUseCase>()
    val saveWelcomeDrawerShownUseCase by rememberInstance<SaveWelcomeDrawerShownUseCase>()
    val saveToolTipsShownUseCase by rememberInstance<SaveToolTipsShownUseCase>()
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val downloadAllResourcesUseCase by rememberInstance<DownloadAllResourcesUseCase>()
    val chooseAuthenticationDataUseCase by rememberInstance<ChooseAuthenticationDataUseCase>()
    val getDownloadResourcesSnapshotStateUseCase by rememberInstance<GetDownloadResourcesSnapshotStateUseCase>()
    val networkStatusTracker by rememberInstance<NetworkStatusTracker>()
    val tracker by rememberInstance<Tracker>()
    val hasRedeemableTasksUseCase by rememberInstance<HasRedeemableTasksUseCase>()

    val biometricAuthenticator = LocalBiometricAuthenticator.current
    val activeProfile by getActiveProfileUseCase().collectAsStateWithLifecycle(null)

    return rememberSaveable(activeProfile, saver = complexAutoSaver()) {
        PrescriptionsController(
            getActiveProfileUseCase = getActiveProfileUseCase,
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            activePrescriptionsUseCase = activePrescriptionsUseCase,
            archivedPrescriptionsUseCase = archivedPrescriptionsUseCase,
            getMLKitAcceptedUseCase = mlKitAcceptedUseCase,
            showGrantConsentDrawerUseCase = showGrantConsentDrawerUseCase,
            getShowWelcomeDrawerUseCase = getShowWelcomeDrawerUseCase,
            saveToolTipsShownUseCase = saveToolTipsShownUseCase,
            downloadAllResourcesUseCase = downloadAllResourcesUseCase,
            switchActiveProfileUseCase = switchActiveProfileUseCase,
            snapshotStateUseCase = getDownloadResourcesSnapshotStateUseCase,
            tracker = tracker,
            networkStatusTracker = networkStatusTracker,
            hasRedeemableTasksUseCase = hasRedeemableTasksUseCase,
            biometricAuthenticator = biometricAuthenticator
        )
    }
}
