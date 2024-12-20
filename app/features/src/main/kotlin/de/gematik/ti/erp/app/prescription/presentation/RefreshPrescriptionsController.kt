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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.ErpServiceState
import de.gematik.ti.erp.app.api.GeneralErrorState
import de.gematik.ti.erp.app.api.RefreshedState
import de.gematik.ti.erp.app.authentication.model.PromptAuthenticator
import de.gematik.ti.erp.app.cardwall.mini.ui.Authenticator
import de.gematik.ti.erp.app.cardwall.mini.ui.NoneEnrolledException
import de.gematik.ti.erp.app.cardwall.mini.ui.RedirectUrlWrongException
import de.gematik.ti.erp.app.cardwall.mini.ui.UserNotAuthenticatedException
import de.gematik.ti.erp.app.core.LocalAuthenticator
import de.gematik.ti.erp.app.idp.usecase.IDPConfigException
import de.gematik.ti.erp.app.idp.usecase.RefreshFlowException
import de.gematik.ti.erp.app.mainscreen.presentation.AppController
import de.gematik.ti.erp.app.prescription.usecase.RefreshPrescriptionUseCase
import de.gematik.ti.erp.app.prescription.usecase.RefreshState
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.vau.interceptor.VauException
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@Stable
class RefreshPrescriptionsController(
    private val refreshPrescriptionUseCase: RefreshPrescriptionUseCase,
    private val appController: AppController,
    private val authenticator: Authenticator,
    private val scope: CoroutineScope
) {

    val isRefreshing: State<RefreshState>
        @Composable
        get() = refreshPrescriptionUseCase.refreshInProgress.collectAsStateWithLifecycle()

    fun refresh(
        profileId: ProfileIdentifier,
        isUserAction: Boolean,
        onUserNotAuthenticated: () -> Unit,
        onShowCardWall: () -> Unit
    ) {
        scope.launch {
            val finalState = refreshFlow(
                profileId = profileId,
                isUserAction = isUserAction
            ).cancellable().first()

            when (finalState) {
                GeneralErrorState.NoneEnrolled -> {
                    onShowCardWall()
                }

                GeneralErrorState.UserNotAuthenticated -> {
                    onUserNotAuthenticated()
                }

                else -> {
                }
            }
        }
    }

    private fun refreshFlow(
        profileId: ProfileIdentifier,
        isUserAction: Boolean
    ): Flow<ErpServiceState> =
        refreshPrescriptionUseCase.downloadFlow(profileId)
            .map { RefreshedState(it) }
            .retryWithAuthenticator(
                isUserAction = isUserAction,
                authenticate = authenticator.getAuthResult(profileId)
            )
            .catchAndTransformRemoteExceptions()
            .flowOn(Dispatchers.IO)
}

@Composable
fun rememberRefreshPrescriptionsController(appController: AppController): RefreshPrescriptionsController {
    val refreshPrescriptionUseCase by rememberInstance<RefreshPrescriptionUseCase>()
    val authenticator = LocalAuthenticator.current
    val scope = rememberCoroutineScope()
    return remember {
        RefreshPrescriptionsController(
            refreshPrescriptionUseCase = refreshPrescriptionUseCase,
            appController = appController,
            authenticator = authenticator,
            scope = scope
        )
    }
}

fun Flow<ErpServiceState>.retryWithAuthenticator(
    isUserAction: Boolean,
    authenticate: Flow<PromptAuthenticator.AuthResult>
) =
// Retries collection of the given flow up to retries times when an exception that
    // matches the given predicate occurs in the upstream flow.
    retry(1) { throwable ->
        Napier.d("Retry with authenticator", throwable)

        when {
            !isUserAction -> throw CancellationException("Authentication cancelled due `isUserAction = false`")

            (throwable.cause as? RefreshFlowException)?.isUserAction == true -> {
                authenticate
                    .first()
                    .let {
                        when (it) {
                            PromptAuthenticator.AuthResult.Authenticated -> true
                            PromptAuthenticator.AuthResult.Cancelled ->
                                throw CancellationException("Authentication dialog cancelled")

                            PromptAuthenticator.AuthResult.NoneEnrolled ->
                                throw NoneEnrolledException()

                            PromptAuthenticator.AuthResult.UserNotAuthenticated ->
                                throw UserNotAuthenticatedException()

                            PromptAuthenticator.AuthResult.RedirectLinkNotRight ->
                                throw RedirectUrlWrongException()
                        }
                    }
            }

            else -> false
        }
    }

fun Flow<ErpServiceState>.catchAndTransformRemoteExceptions() =
    catch { throwable ->
        Napier.d("Try to transform exception", throwable)

        throwable.walkCause()?.also { emit(it) } ?: throw throwable
    }

private fun Throwable.walkCause(): GeneralErrorState? =
    cause?.walkCause() ?: transformException()

@Requirement(
    "O.Plat_4#4",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "The error that occurs is captured and transformed. " +
        "The sensitive data that might come with the errors are transformed so that it is not transmitted."
)
private fun Throwable.transformException(): GeneralErrorState? {
    Napier.e { "Exception transformed $this" }

    return when (this) {
        is RedirectUrlWrongException -> GeneralErrorState.RedirectUrlForExternalAuthenticationWrong
        is UserNotAuthenticatedException -> GeneralErrorState.UserNotAuthenticated
        is NoneEnrolledException -> GeneralErrorState.NoneEnrolled
        is VauException -> GeneralErrorState.FatalTruststoreState
        is IDPConfigException -> GeneralErrorState.FatalTruststoreState
        is SocketTimeoutException, is UnknownHostException -> GeneralErrorState.NetworkNotAvailable
        is ApiCallException -> GeneralErrorState.ServerCommunicationFailedWhileRefreshing(this.response.code())
        else -> null
    }
}
