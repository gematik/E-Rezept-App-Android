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

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.api.HttpErrorState
import de.gematik.ti.erp.app.api.HttpErrorState.ErrorWithCause
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.presentation.ChooseAuthenticationController
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.collectResult
import de.gematik.ti.erp.app.consent.model.ConsentState
import de.gematik.ti.erp.app.consent.model.ConsentState.Companion.isConsentGranted
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.fhir.temporal.Year
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.invoice.model.InvoiceResult
import de.gematik.ti.erp.app.invoice.model.InvoiceResult.InvoiceError
import de.gematik.ti.erp.app.invoice.model.InvoiceResult.InvoiceSuccess.SuccessOnDeletion
import de.gematik.ti.erp.app.invoice.model.InvoiceResult.UserNotLoggedInError
import de.gematik.ti.erp.app.invoice.usecase.DeleteAllLocalInvoices
import de.gematik.ti.erp.app.invoice.usecase.DeleteInvoiceUseCase
import de.gematik.ti.erp.app.invoice.usecase.DownloadInvoicesUseCase
import de.gematik.ti.erp.app.invoice.usecase.GetInvoiceByTaskIdUseCase
import de.gematik.ti.erp.app.invoice.usecase.GetInvoicesByProfileUseCase
import de.gematik.ti.erp.app.pkv.FileProviderAuthority
import de.gematik.ti.erp.app.pkv.presentation.model.InvoiceCardUiState
import de.gematik.ti.erp.app.pkv.usecase.ShareInvoiceUseCase
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class InvoiceController(
    getProfileByIdUseCase: GetProfileByIdUseCase,
    getProfilesUseCase: GetProfilesUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase,
    biometricAuthenticator: BiometricAuthenticator,
    networkStatusTracker: NetworkStatusTracker,
    private val profileId: ProfileIdentifier,
    private val downloadInvoicesUseCase: DownloadInvoicesUseCase,
    private val getInvoicesByProfileUseCase: GetInvoicesByProfileUseCase,
    private val getInvoiceByTaskIdUseCase: GetInvoiceByTaskIdUseCase,
    private val deleteInvoiceUseCase: DeleteInvoiceUseCase,
    private val deleteAllInvoicesUseCase: DeleteAllLocalInvoices,
    private val shareInvoiceUseCase: ShareInvoiceUseCase,
    val invoiceDetailScreenEvents: InvoiceDetailScreenEvents = InvoiceDetailScreenEvents(),
    val invoiceListScreenEvents: InvoiceListScreenEvents = InvoiceListScreenEvents()
) : ChooseAuthenticationController(
    profileId = profileId,
    getProfileByIdUseCase = getProfileByIdUseCase,
    getActiveProfileUseCase = getActiveProfileUseCase,
    getProfilesUseCase = getProfilesUseCase,
    chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
    networkStatusTracker = networkStatusTracker,
    biometricAuthenticator = biometricAuthenticator
) {
    private val getInvoicesTrigger: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)

    private fun refreshGetInvoices() {
        getInvoicesTrigger.value = !getInvoicesTrigger.value
    }

    private fun getInvoices() = getInvoicesByProfileUseCase.invoke(profileId).map { UiState.Data(it) }

    private fun completeDownloadEvent() {
        refreshGetInvoices()
        invoiceListScreenEvents.downloadCompletedEvent.trigger()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val invoices: StateFlow<UiState<Map<Year, List<InvoiceData.PKVInvoiceRecord>>>> by lazy {
        getInvoicesTrigger.flatMapLatest { getInvoices() }
            .stateIn(
                controllerScope,
                started = SharingStarted.Eagerly,
                initialValue = UiState.Loading()
            )
    }

    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        refreshGetInvoices()

        biometricAuthenticationSuccessEvent.listen(controllerScope) {
            downloadInvoices()
            invoiceListScreenEvents.getConsentEvent.trigger(profileId)
        }

        isProfileRefreshingEvent.listen(controllerScope) {
            _isRefreshing.value = it
        }
    }

    fun getInvoiceForTaskId(taskId: String): Flow<InvoiceData.PKVInvoiceRecord?> =
        getInvoiceByTaskIdUseCase(taskId)

    fun deleteInvoice(
        taskId: String,
        profileId: ProfileIdentifier = this.profileId
    ) {
        controllerScope.launch {
            deleteInvoiceUseCase.invoke(taskId, profileId)
                .fold(
                    onSuccess = { result ->
                        when (result) {
                            is SuccessOnDeletion -> invoiceDetailScreenEvents.deleteSuccessfulEvent.trigger()
                            is InvoiceError -> invoiceListScreenEvents.invoiceErrorEvent.trigger(result)
                            else -> Napier.e { "wrong state reached on invoice delete $result" } // state should not be reached
                        }
                    },
                    onFailure = { error ->
                        when (error) {
                            is InvoiceError -> invoiceListScreenEvents.invoiceErrorEvent.trigger(error)
                            is UserNotLoggedInError -> invoiceDetailScreenEvents.askUserToLoginEvent.trigger() // user needs to login to continue
                            else -> {
                                invoiceListScreenEvents.invoiceErrorEvent.trigger(
                                    InvoiceError(
                                        ErrorWithCause(error.message ?: "Unknown error on invoice delete")
                                    )
                                )
                            }
                        }
                    }
                ).also {
                    refreshGetInvoices()
                }
        }
    }

    fun downloadInvoices(
        profileId: ProfileIdentifier = this.profileId
    ) {
        controllerScope.launch {
            downloadInvoicesUseCase.invoke(
                profileId = profileId,
                onDownloadStarted = {
                    // do nothing, design decision to not show a loading indicator
                }
            ).collectResult(
                onSuccess = { result ->
                    completeDownloadEvent()
                    when (result) {
                        is InvoiceError -> invoiceListScreenEvents.invoiceErrorEvent.trigger(result)
                        // these are logged for debug purposes
                        is InvoiceResult.InvoiceSuccess -> Napier.i { "Invoices downloaded successfully" }
                        is InvoiceResult.InvoiceCombinedError -> invoiceListScreenEvents.invoiceErrorEvent.trigger(InvoiceError(result.errorStates.first()))
                        else -> Napier.e { "wrong state reached on invoice download $result" }
                    }
                },
                onFailure = { error ->
                    completeDownloadEvent()
                    when (error) {
                        is UserNotLoggedInError -> invoiceDetailScreenEvents.askUserToLoginEvent.trigger() // user needs to login to continue
                        is InvoiceError -> invoiceListScreenEvents.invoiceErrorEvent.trigger(error)
                        // What do we do when we have many errors on downloading the charge items?
                        is InvoiceResult.InvoiceCombinedError -> invoiceListScreenEvents.invoiceErrorEvent.trigger(InvoiceError(error.errorStates.first()))
                        else -> invoiceListScreenEvents.invoiceErrorEvent.trigger(
                            InvoiceError(ErrorWithCause(error.message ?: "Unknown error on invoice download"))
                        )
                    }
                }
            )
        }
    }

    fun deleteLocalInvoices() {
        controllerScope.launch {
            invoices.first { it.isDataState }.data?.let { invoiceList ->
                deleteAllInvoicesUseCase.invoke(
                    taskIds = invoiceList.values.flatten().map { it.taskId }
                ).fold(
                    onSuccess = {
                        refreshGetInvoices()
                    },
                    onFailure = { result ->
                        // state should not be reached
                        Napier.e { "wrong state reached on invoice delete $result" }
                    }
                )
            }
        }
    }

    fun shareInvoice(
        context: Context,
        taskId: String,
        fileProvider: FileProviderAuthority,
        onCompletion: () -> Unit
    ) {
        controllerScope.launch {
            getInvoiceForTaskId(taskId).first()?.let {
                shareInvoiceUseCase.invoke(
                    context = context,
                    invoice = it,
                    fileProviderAuthority = fileProvider
                )
                onCompletion()
            }
        }
    }

    fun uiState(
        consentState: ConsentState,
        ssoTokenValid: Boolean,
        invoice: InvoiceData.PKVInvoiceRecord?
    ): InvoiceCardUiState {
        return when {
            consentState == ConsentState.ValidState.Loading && ssoTokenValid -> InvoiceCardUiState.Loading
            !consentState.isConsentGranted() && consentState != ConsentState.ValidState.UnknownConsent -> InvoiceCardUiState.NoConsent
            consentState.isConsentGranted() && invoice == null -> InvoiceCardUiState.NoInvoice
            consentState.isConsentGranted() && invoice != null -> InvoiceCardUiState.ShowInvoice
            else -> InvoiceCardUiState.Loading // Fallback state
        }
    }

    // TODO: Make different error messages for different error states
    @Requirement(
        "A_20085#2",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Error messages on refreshing invoices are localized"
    )
    @Requirement(
        "O.Plat_4#3",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "String resources are used tp show the mapped errors."
    )
    fun invoiceErrorMessage(context: Context, errorState: HttpErrorState): String =
        when (errorState) {
            HttpErrorState.BadRequest,
            HttpErrorState.Conflict,
            HttpErrorState.Forbidden,
            HttpErrorState.Gone,
            HttpErrorState.MethodNotAllowed,
            HttpErrorState.NotFound,
            HttpErrorState.TooManyRequest,
            HttpErrorState.Unauthorized,
            HttpErrorState.RequestTimeout
            -> context.getString(R.string.error_message_vau_error)

            HttpErrorState.ServerError -> context.getString(R.string.error_message_server_communication_failed)
                .format(errorState.errorCode)

            is ErrorWithCause, HttpErrorState.Unknown -> context.getString(R.string.error_message_network_not_available)
        }
}

@Composable
fun rememberInvoiceController(profileId: ProfileIdentifier): InvoiceController {
    val biometricAuthenticator = LocalBiometricAuthenticator.current
    val networkStatusTracker by rememberInstance<NetworkStatusTracker>()

    val getInvoicesByProfileUseCase by rememberInstance<GetInvoicesByProfileUseCase>()
    val getInvoiceByTaskIdUseCase by rememberInstance<GetInvoiceByTaskIdUseCase>()

    val deleteInvoiceUseCase by rememberInstance<DeleteInvoiceUseCase>()
    val deleteAllInvoicesUseCase by rememberInstance<DeleteAllLocalInvoices>()

    val downloadInvoicesUseCase by rememberInstance<DownloadInvoicesUseCase>()
    val shareInvoiceUseCase by rememberInstance<ShareInvoiceUseCase>()

    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()

    val chooseAuthenticationDataUseCase by rememberInstance<ChooseAuthenticationDataUseCase>()

    return remember {
        InvoiceController(
            profileId = profileId,
            biometricAuthenticator = biometricAuthenticator,
            networkStatusTracker = networkStatusTracker,
            downloadInvoicesUseCase = downloadInvoicesUseCase,
            getProfilesUseCase = getProfilesUseCase,
            getProfileByIdUseCase = getProfileByIdUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            shareInvoiceUseCase = shareInvoiceUseCase,
            deleteInvoiceUseCase = deleteInvoiceUseCase,
            deleteAllInvoicesUseCase = deleteAllInvoicesUseCase,
            getInvoiceByTaskIdUseCase = getInvoiceByTaskIdUseCase,
            getInvoicesByProfileUseCase = getInvoicesByProfileUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase
        )
    }
}
