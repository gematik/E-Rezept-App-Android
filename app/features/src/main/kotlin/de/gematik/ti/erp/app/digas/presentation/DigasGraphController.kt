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

package de.gematik.ti.erp.app.digas.presentation

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.presentation.GetActiveProfileController
import de.gematik.ti.erp.app.base.usecase.GetLastSuccessfulRefreshedTimeUseCase
import de.gematik.ti.erp.app.digas.domain.usecase.GetDigaByTaskIdUseCase
import de.gematik.ti.erp.app.digas.domain.usecase.UpdateArchivedStatusUseCase
import de.gematik.ti.erp.app.digas.domain.usecase.UpdateDigaIsNewUseCase
import de.gematik.ti.erp.app.digas.domain.usecase.UpdateDigaStatusUseCase
import de.gematik.ti.erp.app.digas.mapper.toDigaMainScreenUiModel
import de.gematik.ti.erp.app.digas.ui.model.DigaMainScreenUiModel
import de.gematik.ti.erp.app.digas.worker.FeedbackNavigationTriggerWorker
import de.gematik.ti.erp.app.fhir.model.DigaStatus
import de.gematik.ti.erp.app.prescription.usecase.DeletePrescriptionUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.hours

abstract class DigasGraphController(
    getActiveProfileUseCase: GetActiveProfileUseCase
) : GetActiveProfileController(getActiveProfileUseCase) {
    abstract val diga: StateFlow<UiState<DigaMainScreenUiModel>>
    abstract val taskId: StateFlow<String?>
    abstract val deleteCompletedEvent: ComposableEvent<Unit>
    abstract val showDeleteBlockedDialogEvent: ComposableEvent<Unit>
    abstract val needLoggedInTokenForDeletionEvent: ComposableEvent<ProfileIdentifier>
    abstract val isProfileRefreshing: StateFlow<Boolean>
    abstract val isInternetConnected: StateFlow<Boolean>
    abstract val lastRefreshedOn: StateFlow<Instant>
    abstract val isDownloading: StateFlow<Boolean>
    abstract val isBfarmReachable: StateFlow<Boolean>

    abstract fun updateTaskId(id: String?)
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
    private val deletePrescriptionUseCase: DeletePrescriptionUseCase,
    private val updateArchivedStatusUseCase: UpdateArchivedStatusUseCase,
    private val getLastSuccessfulRefreshedTimeUseCase: GetLastSuccessfulRefreshedTimeUseCase
) : DigasGraphController(
    getActiveProfileUseCase = getActiveProfileUseCase
) {

    private val _isBafimReachable = MutableStateFlow(false)
    private val _isProfileRefreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _isDownloading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isBfarmReachable: StateFlow<Boolean> = _isBafimReachable
    override val isProfileRefreshing = _isProfileRefreshing.asStateFlow()
    override val isDownloading = _isDownloading.asStateFlow()
    override val needLoggedInTokenForDeletionEvent = ComposableEvent<ProfileIdentifier>()

    init {
        onRefreshProfileAction.listen(controllerScope) {
            _isProfileRefreshing.value = it
        }
    }

    private val refreshTrigger = MutableStateFlow(Unit) // Trigger for refreshing the flow

    private val _taskId: MutableStateFlow<String?> = MutableStateFlow(null)

    private suspend fun getProfile() = activeProfile.first { it.isDataState }.data

    @OptIn(ExperimentalCoroutinesApi::class)
    override val diga: StateFlow<UiState<DigaMainScreenUiModel>> =
        _taskId
            .flatMapLatest { taskId ->
                if (taskId.isNullOrEmpty()) {
                    flowOf(UiState.Empty()) // no task id, we do not show the diga
                } else {
                    refreshTrigger.flatMapLatest {
                        getDigaByTaskIdUseCase.invoke(taskId).map { task ->
                            UiState.Data(data = task.toDigaMainScreenUiModel())
                        }
                    }
                }
            }.distinctUntilChanged() // avoids UI recompositions unless data changes
            .stateIn(
                controllerScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = UiState.Loading()
            )

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
        when (state) {
            WorkInfo.State.SUCCEEDED -> _isDownloading.value = false
            WorkInfo.State.FAILED -> _isDownloading.value = false
            WorkInfo.State.RUNNING -> _isDownloading.value = true
            else -> _isDownloading.value = false
        }
    }

    override fun updateTaskId(id: String?) {
        id?.takeIf { it.isNotNullOrEmpty() }?.let {
            updateNewLabel(it)
            _taskId.value = it
        }
    }

    override fun refresh() {
        refreshActiveProfile()
        refreshTrigger.value = Unit
    }

    // the cleanup method when we are leaving the diga process
    override fun reset() {
        _taskId.value = null
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
                needLoggedInTokenForDeletionEvent.trigger(profile.id)
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
