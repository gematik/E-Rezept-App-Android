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

package de.gematik.ti.erp.app.appauthentication

import androidx.biometric.BiometricPrompt.PromptInfo
import app.cash.turbine.test
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationErpModel
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationFailureErpModel
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationMethodErpModel
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationPasswordErpModel
import de.gematik.ti.erp.app.authentication.observer.BiometricPromptBuilder
import de.gematik.ti.erp.app.appauthentication.observer.AuthenticationModeAndMethod
import de.gematik.ti.erp.app.appauthentication.observer.InactivityTimeoutObserver
import de.gematik.ti.erp.app.appauthentication.presentation.AppAuthenticationController
import de.gematik.ti.erp.app.appauthentication.repository.AppAuthenticationRepository
import de.gematik.ti.erp.app.appauthentication.usecase.ResetAuthenticationTimeOutSystemUptimeUseCase
import de.gematik.ti.erp.app.appauthentication.usecase.SetAuthenticationTimeOutSystemUptimeUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class AppAuthenticationControllerTest {
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)
    private val repository: AppAuthenticationRepository = mockk()
    private lateinit var controllerUnderTest: AppAuthenticationController
    private var inactivityTimeoutObserver: InactivityTimeoutObserver = mockk()
    private lateinit var setAuthenticationTimeOutSystemUptimeUseCase: SetAuthenticationTimeOutSystemUptimeUseCase
    private lateinit var resetAuthenticationTimeOutSystemUptimeUseCase: ResetAuthenticationTimeOutSystemUptimeUseCase
    private var biometricPromptBuilder: BiometricPromptBuilder = mockk()
    private var promptInfo: PromptInfo = mockk()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(dispatcher)

        setAuthenticationTimeOutSystemUptimeUseCase = SetAuthenticationTimeOutSystemUptimeUseCase(
            repository
        )
        resetAuthenticationTimeOutSystemUptimeUseCase = ResetAuthenticationTimeOutSystemUptimeUseCase(
            repository
        )

        controllerUnderTest = AppAuthenticationController(
            inactivityTimeoutObserver = inactivityTimeoutObserver,
            biometricPromptBuilder = biometricPromptBuilder,
            promptInfo = promptInfo,
            setAuthenticationTimeOutSystemUptimeUseCase = setAuthenticationTimeOutSystemUptimeUseCase,
            resetAuthenticationTimeOutSystemUptimeUseCase = resetAuthenticationTimeOutSystemUptimeUseCase
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `no timeout -loading from db- test `() {
        coEvery { inactivityTimeoutObserver.authenticationModeAndMethod } returns flowOf(
            AuthenticationModeAndMethod.AuthenticationRequired(
                AppAuthenticationErpModel(
                    authenticationMethod =
                    AppAuthenticationMethodErpModel.Password(
                        AppAuthenticationPasswordErpModel.fromPassword("")
                    ),
                    authenticationFailure = AppAuthenticationFailureErpModel(
                        failedAttempts = 4,
                        timeOutSystemUptime = null
                    )
                )
            )
        )

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.authenticationState.test {
                val state = awaitItem()
                val result = controllerUnderTest.calculateAuthenticationTimeOut(state)
                assertEquals(0, result)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `5 sec timeout -loading from db- test `() {
        coEvery { inactivityTimeoutObserver.authenticationModeAndMethod } returns flowOf(
            AuthenticationModeAndMethod.AuthenticationRequired(
                AppAuthenticationErpModel(
                    authenticationMethod =
                    AppAuthenticationMethodErpModel.Password(
                        AppAuthenticationPasswordErpModel.fromPassword("")
                    ),
                    authenticationFailure = AppAuthenticationFailureErpModel(
                        failedAttempts = 6,
                        timeOutSystemUptime = 0
                    )
                )
            )
        )
        coEvery { repository.setAuthenticationTimeOutSystemUptime(5) } returns Unit
        coEvery { repository.resetAuthenticationTimeOutSystemUptime() } returns Unit

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.authenticationState.test {
                val state = awaitItem()
                val result = controllerUnderTest.calculateAuthenticationTimeOut(state)
                assertEquals(5, result)
            }
        }
    }

    @Test
    fun `40 sec timeout -loading from db- test `() {
        coEvery { inactivityTimeoutObserver.authenticationModeAndMethod } returns flowOf(
            AuthenticationModeAndMethod.AuthenticationRequired(
                AppAuthenticationErpModel(
                    authenticationMethod =
                    AppAuthenticationMethodErpModel.Password(
                        AppAuthenticationPasswordErpModel.fromPassword("")
                    ),
                    authenticationFailure = AppAuthenticationFailureErpModel(
                        failedAttempts = 36,
                        timeOutSystemUptime = 0
                    )
                )
            )
        )
        coEvery { repository.setAuthenticationTimeOutSystemUptime(any()) } returns Unit
        coEvery { repository.resetAuthenticationTimeOutSystemUptime() } returns Unit

        testScope.runTest {
            controllerUnderTest.authenticationState.test {
                val state = awaitItem()
                val result = controllerUnderTest.calculateAuthenticationTimeOut(state)
                assertEquals(40, result)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `timeout already completed -loading from db- test `() {
        coEvery { inactivityTimeoutObserver.authenticationModeAndMethod } returns flowOf(
            AuthenticationModeAndMethod.AuthenticationRequired(
                AppAuthenticationErpModel(
                    authenticationMethod =
                    AppAuthenticationMethodErpModel.Password(
                        AppAuthenticationPasswordErpModel.fromPassword("")
                    ),
                    authenticationFailure = AppAuthenticationFailureErpModel(
                        failedAttempts = 6,
                        timeOutSystemUptime = -5000
                    )
                )
            )
        )
        coEvery { repository.resetAuthenticationTimeOutSystemUptime() } returns Unit

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.authenticationState.test {
                val state = awaitItem()
                val result = controllerUnderTest.calculateAuthenticationTimeOut(state)
                assertEquals(0, result)
            }
        }
    }
}
