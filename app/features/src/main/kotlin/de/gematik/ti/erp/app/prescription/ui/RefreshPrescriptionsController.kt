/*
 * Copyright (c) 2024 gematik GmbH
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.authentication.model.PromptAuthenticator
import de.gematik.ti.erp.app.cardwall.mini.ui.Authenticator
import de.gematik.ti.erp.app.cardwall.mini.ui.NoneEnrolledException
import de.gematik.ti.erp.app.cardwall.mini.ui.UserNotAuthenticatedException
import de.gematik.ti.erp.app.core.LocalAuthenticator
import de.gematik.ti.erp.app.idp.usecase.IDPConfigException
import de.gematik.ti.erp.app.idp.usecase.RefreshFlowException
import de.gematik.ti.erp.app.mainscreen.presentation.MainScreenController
import de.gematik.ti.erp.app.prescription.usecase.RefreshPrescriptionUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.vau.interceptor.VauException
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retry
import org.kodein.di.compose.rememberInstance
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@Stable
class RefreshPrescriptionsController(
    private val refreshPrescriptionUseCase: RefreshPrescriptionUseCase,
    private val mainScreenController: MainScreenController,
    private val authenticator: Authenticator
) {

    val isRefreshing
        @Composable
        get() = refreshPrescriptionUseCase.refreshInProgress.collectAsStateWithLifecycle()

    suspend fun refresh(
        profileId: ProfileIdentifier,
        isUserAction: Boolean,
        onUserNotAuthenticated: () -> Unit,
        onShowCardWall: () -> Unit
    ) {
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
                mainScreenController.onRefresh(finalState)
            }
        }
    }

    private fun refreshFlow(
        profileId: ProfileIdentifier,
        isUserAction: Boolean
    ): Flow<PrescriptionServiceState> =
        refreshPrescriptionUseCase.downloadFlow(profileId)
            .map {
                RefreshedState(it) as PrescriptionServiceState
            }
            .retryWithAuthenticator(
                isUserAction = isUserAction,
                authenticate = authenticator.authenticateForPrescriptions(profileId)
            )
            .catchAndTransformRemoteExceptions()
            .flowOn(Dispatchers.IO)
}

@Composable
fun rememberRefreshPrescriptionsController(mainScreenController: MainScreenController): RefreshPrescriptionsController {
    val refreshPrescriptionUseCase by rememberInstance<RefreshPrescriptionUseCase>()
    val authenticator = LocalAuthenticator.current

    return remember {
        RefreshPrescriptionsController(
            refreshPrescriptionUseCase = refreshPrescriptionUseCase,
            mainScreenController = mainScreenController,
            authenticator = authenticator
        )
    }
}

fun Flow<PrescriptionServiceState>.retryWithAuthenticator(
    isUserAction: Boolean,
    authenticate: Flow<PromptAuthenticator.AuthResult>
) =
    retry(1) { throwable ->
        Napier.d("Retry with authenticator", throwable)

        when {
            !isUserAction ->
                throw CancellationException("Authentication cancelled due `isUserAction = false`")
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
                        }
                    }
            }
            else -> false
        }
    }

fun Flow<PrescriptionServiceState>.catchAndTransformRemoteExceptions() =
    catch { throwable ->
        Napier.d("Try to transform exception", throwable)

        throwable.walkCause()?.also { emit(it) } ?: throw throwable
    }

private fun Throwable.walkCause(): GeneralErrorState? =
    cause?.walkCause() ?: transformException()

private fun Throwable.transformException(): GeneralErrorState? =
    when (this) {
        is UserNotAuthenticatedException ->
            GeneralErrorState.UserNotAuthenticated
        is NoneEnrolledException ->
            GeneralErrorState.NoneEnrolled
        is VauException ->
            GeneralErrorState.FatalTruststoreState
        is IDPConfigException -> // TODO use other state
            GeneralErrorState.FatalTruststoreState
        is SocketTimeoutException,
        is UnknownHostException ->
            GeneralErrorState.NetworkNotAvailable
        is ApiCallException ->
            GeneralErrorState.ServerCommunicationFailedWhileRefreshing(
                this.response.code()
            )
        else -> null
    }
