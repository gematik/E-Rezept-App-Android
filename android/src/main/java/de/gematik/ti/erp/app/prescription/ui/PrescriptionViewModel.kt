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

package de.gematik.ti.erp.app.prescription.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.api.map
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationUseCase
import de.gematik.ti.erp.app.common.usecase.HintUseCase
import de.gematik.ti.erp.app.common.usecase.model.CancellableHint
import de.gematik.ti.erp.app.core.BaseViewModel
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken
import de.gematik.ti.erp.app.idp.usecase.IDPConfigException
import de.gematik.ti.erp.app.idp.usecase.RefreshFlowException
import de.gematik.ti.erp.app.mainscreen.ui.PullRefreshState
import de.gematik.ti.erp.app.mainscreen.ui.RefreshEvent
import de.gematik.ti.erp.app.prescription.ui.model.PrescriptionScreenData
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import de.gematik.ti.erp.app.vau.interceptor.VauException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PrescriptionViewModel @Inject constructor(
    private val prescriptionUseCase: PrescriptionUseCase,
    private val profilesUseCase: ProfilesUseCase,
    private val settingsUseCase: SettingsUseCase,
    private val demoUseCase: DemoUseCase,
    private val dispatchProvider: DispatchProvider,
    private val hintUseCase: HintUseCase,
    private val authenticationUseCase: AuthenticationUseCase
) : BaseViewModel() {

    val defaultState = PrescriptionScreenData.State(
        demoUseCase.isDemoModeActive,
        emptyList(),
        emptyList(),
        0,
        activeProfile = null
    )

    fun downloadAllAuditEvents(profileName: String) {
        prescriptionUseCase.downloadAllAuditEvents(profileName = profileName)
    }

    @OptIn(FlowPreview::class)
    fun screenState(): Flow<PrescriptionScreenData.State> {
        val prescriptionFlow = combine(
            prescriptionUseCase.scannedRecipes(),
            prescriptionUseCase.syncedRecipes()
        ) { lowDetail, fullDetail ->
            (lowDetail + fullDetail)
        }.onStart {
            emit(emptyList())
        }

        return combine(
            demoUseCase.demoModeActive,
            prescriptionFlow,
            prescriptionUseCase.redeemedPrescriptions(),
            settingsUseCase.settings,
            profilesUseCase.profiles,
        ) { demoActive, prescriptions, redeemed, settings, profiles ->
            // TODO: split redeemed & unredeemed
            PrescriptionScreenData.State(
                showDemoBanner = demoActive,
                prescriptions = prescriptions,
                redeemedPrescriptions = redeemed,
                nowInEpochDays = LocalDate.now().toEpochDay(),
                activeProfile = profiles.find { it.active }
            )
        }.flowOn(dispatchProvider.unconfined())
    }

    suspend fun refreshPrescriptions(
        pullRefreshState: PullRefreshState,
        isDemoModeActive: Boolean,
        onShowSecureHardwarePrompt: suspend () -> Unit,
        onShowCardWall: suspend (canAvailable: Boolean) -> Unit,
        onRefresh: suspend (event: RefreshEvent) -> Unit
    ) {
        val profileName = profilesUseCase.activeProfileName().flowOn(dispatchProvider.io()).first()

        Timber.d("Refreshing prescriptions for $profileName")

        val result = withContext(dispatchProvider.io()) { prescriptionUseCase.downloadTasks(profileName) }
            .map { nrOfNewPrescriptions ->
                if (!isDemoModeActive) {
                    prescriptionUseCase.downloadCommunications(profileName).map {
                        downloadAllAuditEvents(profileName)
                        Result.Success(nrOfNewPrescriptions)
                    }
                } else {
                    Result.Success(nrOfNewPrescriptions)
                }
            }

        when (result) {
            is Result.Error -> {
                (result.exception.cause as? CancellationException)?.let {
                    return
                }

                (result.exception.cause as? RefreshFlowException)?.let { // Hint: We are now in unauthorized state
                    if (it.userActionRequired) {
                        if (it.ssoToken is SingleSignOnToken.AlternateAuthenticationWithoutToken) {
                            onShowSecureHardwarePrompt()
                        } else {
                            val canAvailable = isCanAvailable()
                            onShowCardWall(canAvailable)
                        }
                    }
                    return
                }

                (result.exception.cause as? IDPConfigException)?.let {
                    // TODO propagate a more meaningful message
                    onRefresh(RefreshEvent.FatalTruststoreState)
                    return
                }

                when (result.exception.cause?.cause) {
                    is SocketTimeoutException,
                    is UnknownHostException -> {
                        onRefresh(RefreshEvent.NetworkNotAvailable)
                        return
                    }
                }

                (result.exception as? ApiCallException)?.let {
                    onRefresh(
                        RefreshEvent.ServerCommunicationFailedWhileRefreshing(
                            it.response.code()
                        )
                    )
                    return
                }

                (result.exception.cause as? VauException)?.let {
                    onRefresh(RefreshEvent.FatalTruststoreState)
                    return
                }
            }
            is Result.Success -> {
                if (pullRefreshState != PullRefreshState.HasValidToken && !isDemoModeActive) {
                    onRefresh(RefreshEvent.NewPrescriptionsEvent(result.data))
                }
            }
        }
    }

    fun onCloseHintCard(hint: CancellableHint) {
        hintUseCase.cancelHint(hint)
    }

    fun onAlternateAuthentication() {
        viewModelScope.launch {
            authenticationUseCase.authenticateWithSecureElement()
                .catch {
                    Timber.e(it)
                    cancel("just because")
                }
                .collect()
        }
    }

    suspend fun isCanAvailable() = authenticationUseCase.isCanAvailable()
}
