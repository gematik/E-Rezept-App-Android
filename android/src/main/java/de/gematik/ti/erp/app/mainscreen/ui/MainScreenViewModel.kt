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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.core.BaseViewModel
import de.gematik.ti.erp.app.db.entities.CommunicationProfile
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.mainscreen.ui.model.MainScreenData
import de.gematik.ti.erp.app.messages.usecase.MessageUseCase
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import java.net.URI
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach

sealed class RefreshEvent {
    object NetworkNotAvailable : RefreshEvent()
    data class ServerCommunicationFailedWhileRefreshing(val code: Int) : RefreshEvent()
    object FatalTruststoreState : RefreshEvent()
    data class NewPrescriptionsEvent(val nrOfNewPrescriptions: Int) : RefreshEvent()
}

/**
 * Event used to indicate an action that should be visible to the user on main screen.
 */
sealed class ActionEvent {
    data class ReturnFromPharmacyOrder(val successfullyOrdered: PharmacyScreenData.OrderOption) : ActionEvent()
}

enum class PullRefreshState {
    None,
    HasFirstTimeValidToken,
    IsFirstTimeBiometricAuthentication,
    HasValidToken
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val demoUseCase: DemoUseCase,
    private val messageUseCase: MessageUseCase,
    private val prescriptionUseCase: PrescriptionUseCase,
    private val profileUseCase: ProfilesUseCase,
    private val coroutineDispatchProvider: DispatchProvider,
    private val idpUseCase: IdpUseCase,
) : BaseViewModel() {

    private val _onRefreshEvent = MutableSharedFlow<RefreshEvent>()
    val onRefreshEvent: Flow<RefreshEvent>
        get() = _onRefreshEvent

    private val _onActionEvent = MutableStateFlow<ActionEvent?>(null)
    val onActionEvent: Flow<ActionEvent>
        get() = _onActionEvent.filterNotNull().onEach { _onActionEvent.value = null }

    fun profileUiState() = profileUseCase.profiles.flowOn(coroutineDispatchProvider.unconfined())

    fun refreshState(): Flow<PullRefreshState> = profileUiState()
        .map {
            val activeProfile = it.find { profile -> profile.active }!!

            val ssoToken = activeProfile.ssoToken
            val now = Instant.now()
            when {
                ssoToken is SingleSignOnToken.AlternateAuthenticationWithoutToken -> PullRefreshState.IsFirstTimeBiometricAuthentication
                ssoToken != null && ssoToken.validOn in (now - Duration.ofSeconds(5))..(now) -> PullRefreshState.HasFirstTimeValidToken
                ssoToken != null && ssoToken.isValid(now) -> PullRefreshState.HasValidToken
                else -> PullRefreshState.None
            }
        }

    fun redeemState(): Flow<MainScreenData.RedeemState> =
        combine(
            prescriptionUseCase.redeemableAndValidSyncedTaskIds(),
            prescriptionUseCase.redeemableScannedTaskIds()
        ) { syncedTaskIds, scannedTaskIds ->
            MainScreenData.RedeemState(
                scannedTaskIds = TaskIds(ids = scannedTaskIds), syncedTaskIds = TaskIds(ids = syncedTaskIds)
            )
        }

    fun saveActiveProfile(profile: ProfilesUseCaseData.Profile) {
        viewModelScope.launch { profileUseCase.switchActiveProfile(profile) }
    }

    fun unreadMessagesAvailable() =
        messageUseCase.unreadCommunicationsAvailable(CommunicationProfile.ErxCommunicationReply)

    suspend fun onRefresh(event: RefreshEvent) {
        _onRefreshEvent.emit(event)
    }

    fun onAction(event: ActionEvent) {
        viewModelScope.launch(coroutineDispatchProvider.default()) {
            _onActionEvent.emit(event)
        }
    }

    fun onDeactivateDemoMode() {
        demoUseCase.deactivateDemoMode()
    }

    fun isDemoActive(): Boolean = demoUseCase.isDemoModeActive

    fun onExternAppAuthorizationResult(uri: URI) {
        Timber.d(uri.toString())
        viewModelScope.launch {
            idpUseCase.authenticateWithExternalAppAuthorization(uri)
            prescriptionUseCase.downloadTasks(profileUseCase.activeProfileName().first())
        }
    }
}
