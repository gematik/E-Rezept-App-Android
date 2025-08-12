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

package de.gematik.ti.erp.app.digas.presentation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.usecase.GetLastSuccessfulRefreshedTimeUseCase
import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.digas.domain.usecase.FetchDigaByPznUseCase
import de.gematik.ti.erp.app.digas.domain.usecase.GetDigaByTaskIdUseCase
import de.gematik.ti.erp.app.digas.domain.usecase.UpdateArchivedStatusUseCase
import de.gematik.ti.erp.app.digas.domain.usecase.UpdateDigaIsNewUseCase
import de.gematik.ti.erp.app.digas.domain.usecase.UpdateDigaStatusUseCase
import de.gematik.ti.erp.app.digas.mapper.toDigaMainScreenUiModel
import de.gematik.ti.erp.app.digas.navigation.DigasRoutes
import de.gematik.ti.erp.app.digas.ui.model.DigaBfarmUiModel
import de.gematik.ti.erp.app.digas.ui.model.DigaMainScreenUiModel
import de.gematik.ti.erp.app.digas.worker.FeedbackNavigationTriggerWorker
import de.gematik.ti.erp.app.insurance.usecase.FetchInsuranceProviderUseCase
import de.gematik.ti.erp.app.prescription.usecase.DeletePrescriptionUseCase
import de.gematik.ti.erp.app.profiles.presentation.GetActiveProfileController
import de.gematik.ti.erp.app.profiles.ui.extension.extract
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import de.gematik.ti.erp.app.viewmodel.rememberGraphScopedViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.annotations.VisibleForTesting
import org.kodein.di.compose.rememberInstance
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.hours

abstract class DigasGraphController(
    getActiveProfileUseCase: GetActiveProfileUseCase
) : GetActiveProfileController(getActiveProfileUseCase) {
    abstract val diga: StateFlow<UiState<DigaMainScreenUiModel>>
    abstract val digaBfarm: StateFlow<UiState<DigaBfarmUiModel>>
    abstract val taskId: StateFlow<String?>
    abstract val deleteCompletedEvent: ComposableEvent<Unit>
    abstract val showDeleteBlockedDialogEvent: ComposableEvent<Unit>
    abstract val needLoggedInTokenForDeletionEvent: ComposableEvent<ProfilesUseCaseData.Profile>
    abstract val isProfileRefreshing: StateFlow<Boolean>
    abstract val isInternetConnected: StateFlow<Boolean>
    abstract val lastRefreshedOn: StateFlow<Instant>
    abstract val isDownloading: StateFlow<Boolean>
    abstract val insuranceName: StateFlow<String?>
    abstract val telematikId: String?
    abstract val isLoadingTask: StateFlow<Boolean>
    abstract val isLoadingInsurance: StateFlow<Boolean>

    abstract fun updateTaskId(id: String?, isReady: Boolean?)
    abstract fun updateInsuranceInfo(id: String?, name: String?)
    abstract fun refresh()
    abstract fun reset()
    abstract fun onOpenAppWithRedeemCodeDiga()
    abstract fun onDownloadDiga()
    abstract fun onArchiveDiga(currentStatus: DigaStatus? = null)
    abstract fun onArchiveRevert(currentStatus: DigaStatus?)
    abstract fun onDeleteDiga()
    abstract fun observeIsDownloading(state: WorkInfo.State?)
    abstract fun registerFeedbackPrompt(context: Context)
}

class DefaultDigasGraphController(
    getActiveProfileUseCase: GetActiveProfileUseCase,
    private val networkStatusTracker: NetworkStatusTracker,
    private val updateDigaIsNewUseCase: UpdateDigaIsNewUseCase,
    private val updateDigaStatusUseCase: UpdateDigaStatusUseCase,
    private val getDigaByTaskIdUseCase: GetDigaByTaskIdUseCase,
    private val fetchDigaByPznUseCase: FetchDigaByPznUseCase,
    private val fetchInsuranceProviderUseCase: FetchInsuranceProviderUseCase,
    private val deletePrescriptionUseCase: DeletePrescriptionUseCase,
    private val updateArchivedStatusUseCase: UpdateArchivedStatusUseCase,
    private val getLastSuccessfulRefreshedTimeUseCase: GetLastSuccessfulRefreshedTimeUseCase
) : DigasGraphController(
    getActiveProfileUseCase = getActiveProfileUseCase
) {
    private val _isProfileRefreshing = MutableStateFlow(false)
    private val _isDownloading = MutableStateFlow(false)
    private val _isLoadingTask = MutableStateFlow(false)
    private val _isLoadingInsurance = MutableStateFlow(false)
    private val _digaBfarm = MutableStateFlow<UiState<DigaBfarmUiModel>>(UiState.Empty())

    override val isProfileRefreshing = _isProfileRefreshing.asStateFlow()
    override val isDownloading = _isDownloading.asStateFlow()
    override val isLoadingTask = _isLoadingTask.asStateFlow()
    override val isLoadingInsurance = _isLoadingInsurance.asStateFlow()
    override val needLoggedInTokenForDeletionEvent = ComposableEvent<ProfilesUseCaseData.Profile>()

    init {
        onRefreshProfileAction.listen(controllerScope) {
            _isProfileRefreshing.value = it
        }
    }

    @VisibleForTesting
    val refreshTrigger = MutableStateFlow(Unit) // Trigger for refreshing the flow

    private val _taskId: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _isReady: MutableStateFlow<Boolean?> = MutableStateFlow(false)

    private var _telematikId: String? = null
    override val telematikId get() = _telematikId

    private val _insuranceName = MutableStateFlow<String?>(null)
    override val insuranceName = _insuranceName.asStateFlow()

    override val digaBfarm: StateFlow<UiState<DigaBfarmUiModel>> = _digaBfarm

    private suspend fun getProfile() = activeProfile.first { it.isDataState }.data

    @OptIn(ExperimentalCoroutinesApi::class)
    override val diga: StateFlow<UiState<DigaMainScreenUiModel>> =
        _taskId
            .flatMapLatest { taskId ->
                when {
                    taskId.isNullOrEmpty() -> flowOf(UiState.Empty())
                    else -> {
                        refreshTrigger.flatMapLatest {
                            getDigaByTaskIdUseCase(taskId)
                                .mapLatest { diga ->
                                    val pzn = diga.deviceRequest?.pzn
                                    controllerScope.async {
                                        _digaBfarm.value = UiState.Loading()
                                        fetchDigaByPzn(pzn)
                                    }
                                    UiState.Data(diga.toDigaMainScreenUiModel())
                                }
                        }
                    }
                }
            }
            .distinctUntilChanged()
            .stateIn(
                scope = controllerScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = UiState.Loading()
            )

    suspend fun fetchDigaByPzn(pzn: String?) {
        _digaBfarm.value = fetchDigaByPznUseCase(pzn)
    }

    private fun fetchInsuranceProviderInfo() {
        if (_isReady.value != true) return

        controllerScope.launch {
            _isLoadingTask.value = true
            try {
                activeProfile.extract()?.takeIf { _insuranceName.value.isNullOrEmpty() }?.let { profile ->
                    fetchInsuranceProviderUseCase.invoke(profile.id)?.let { provider ->
                        _insuranceName.value = provider.name
                        _telematikId = provider.id
                    }
                }
            } finally {
                _isLoadingTask.value = false
            }
        }
    }

    override val taskId: StateFlow<String?> = _taskId.asStateFlow()

    override val isInternetConnected by lazy {
        networkStatusTracker.networkStatus
            .stateIn(
                controllerScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = true
            )
    }

    @Suppress("MagicNumber")
    override val lastRefreshedOn by lazy {
        getLastSuccessfulRefreshedTimeUseCase
            .invoke()
            .stateIn(
                controllerScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = Clock.System.now().minus(2.hours)
            )
    }

    override val deleteCompletedEvent: ComposableEvent<Unit> = ComposableEvent()

    override val showDeleteBlockedDialogEvent: ComposableEvent<Unit> = ComposableEvent()

    private fun taskId() =
        requireNotNull(taskId.value) { "taskId must not be null when calling DigasGraphController" }

    private fun updateNewLabel(taskId: String) {
        controllerScope.launch {
            updateDigaIsNewUseCase(taskId)
        }
    }

    override fun observeIsDownloading(state: WorkInfo.State?) {
        _isDownloading.value = when (state) {
            WorkInfo.State.RUNNING -> true
            else -> false
        }
    }

    override fun updateTaskId(id: String?, isReady: Boolean?) {
        id?.takeIf { it.isNotNullOrEmpty() }?.let {
            updateNewLabel(it)
            _taskId.value = it
        }
        _isReady.value = isReady
        if (isReady == true) {
            fetchInsuranceProviderInfo()
        }
    }

    override fun updateInsuranceInfo(id: String?, name: String?) {
        id?.takeIf { it.isNotNullOrEmpty() }?.let {
            _telematikId = it
            _insuranceName.value = name
        }
    }

    override fun refresh() {
        refreshActiveProfile()
        refreshTrigger.value = Unit
    }

    // the cleanup method when we are leaving the diga process
    override fun reset() {
        _taskId.value = null
        _telematikId = null
        _insuranceName.value = null
    }

    override fun onDownloadDiga() {
        controllerScope.launch {
            updateDigaStatusUseCase.invoke(
                taskId = taskId(),
                status = DigaStatus.OpenAppWithRedeemCode
            )
        }
    }

    // user action to activate the diga using the redeem code
    override fun onOpenAppWithRedeemCodeDiga() {
        controllerScope.launch {
            updateDigaStatusUseCase.invoke(
                taskId = taskId(),
                status = DigaStatus.ReadyForSelfArchiveDiga
            )
        }
    }

    override fun onArchiveDiga(currentStatus: DigaStatus?) {
        controllerScope.launch {
            updateArchivedStatusUseCase.invoke(
                taskId = taskId(),
                isArchived = true
            )
        }
    }

    override fun onArchiveRevert(currentStatus: DigaStatus?) {
        controllerScope.launch {
            if (currentStatus == DigaStatus.SelfArchiveDiga) {
                updateDigaStatusUseCase.invoke(
                    taskId = taskId(),
                    status = DigaStatus.ReadyForSelfArchiveDiga
                )
            } else {
                updateArchivedStatusUseCase.invoke(
                    taskId = taskId(),
                    isArchived = false
                )
            }
        }
    }

    @Suppress("MagicNumber")
    override fun registerFeedbackPrompt(context: Context) {
        controllerScope.launch {
            activeProfile.first { it.isDataState }.data?.let { activeProfile ->
                val workRequest = OneTimeWorkRequestBuilder<FeedbackNavigationTriggerWorker>()
                    .setInitialDelay(30, TimeUnit.SECONDS)
                    .setInputData(workDataOf("profileId" to activeProfile.id))
                    .build()

                val uniqueWorkName = "FeedbackPrompt-${activeProfile.id}"

                val workManager = WorkManager.getInstance(context)
                // provide the work request with a unique name
                workManager.enqueueUniqueWork(
                    uniqueWorkName,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }
        }
    }

    override fun onDeleteDiga() {
        controllerScope.launch {
            // 1) Block if there's an in-progress DiGA
            if (isDeleteBlocked()) {
                showDeleteBlockedDialogEvent.trigger()
                return@launch
            }

            // 2) Grab profile and verify token
            val profile = getProfile() ?: return@launch
            if (!profile.isSSOTokenValid()) {
                needLoggedInTokenForDeletionEvent.trigger(profile)
                return@launch
            }

            // 3) Finally perform the delete
            performDelete(profile.id, taskId())
        }
    }

    private suspend fun isDeleteBlocked(): Boolean {
        val status = diga.firstOrNull()?.data?.status
        return status is DigaStatus.InProgress
    }

    private suspend fun performDelete(profileId: String, taskId: String) {
        deletePrescriptionUseCase(profileId = profileId, taskId = taskId, deleteLocallyOnly = false)
            .firstOrNull()?.let { deleteCompletedEvent.trigger() }
    }

    override fun onCleared() {
        super.onCleared()
        reset()
    }
}

@Composable
internal fun digasSharedViewModel(
    navController: NavController,
    entry: NavBackStackEntry
): DigasGraphController {
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val networkStatusTracker by rememberInstance<NetworkStatusTracker>()
    val updateDigaIsNewUseCase by rememberInstance<UpdateDigaIsNewUseCase>()
    val updateDigaStatusUseCase by rememberInstance<UpdateDigaStatusUseCase>()
    val getDigaByTaskIdUseCase by rememberInstance<GetDigaByTaskIdUseCase>()
    val fetchDigaByPznUseCase by rememberInstance<FetchDigaByPznUseCase>()
    val fetchInsuranceProviderUseCase by rememberInstance<FetchInsuranceProviderUseCase>()
    val deletePrescriptionUseCase by rememberInstance<DeletePrescriptionUseCase>()
    val updateArchivedStatusUseCase by rememberInstance<UpdateArchivedStatusUseCase>()
    val getLastSuccessfulRefreshedTimeUseCase by rememberInstance<GetLastSuccessfulRefreshedTimeUseCase>()

    return rememberGraphScopedViewModel(
        navController = navController,
        navEntry = entry,
        graphRoute = DigasRoutes.subGraphName()
    ) {
        DefaultDigasGraphController(
            getActiveProfileUseCase = getActiveProfileUseCase,
            networkStatusTracker = networkStatusTracker,
            updateDigaIsNewUseCase = updateDigaIsNewUseCase,
            updateDigaStatusUseCase = updateDigaStatusUseCase,
            getDigaByTaskIdUseCase = getDigaByTaskIdUseCase,
            fetchDigaByPznUseCase = fetchDigaByPznUseCase,
            fetchInsuranceProviderUseCase = fetchInsuranceProviderUseCase,
            deletePrescriptionUseCase = deletePrescriptionUseCase,
            updateArchivedStatusUseCase = updateArchivedStatusUseCase,
            getLastSuccessfulRefreshedTimeUseCase = getLastSuccessfulRefreshedTimeUseCase
        )
    }
}
