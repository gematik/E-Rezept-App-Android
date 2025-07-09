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

package de.gematik.ti.erp.app.userauthentication

import androidx.biometric.BiometricPrompt.PromptInfo
import app.cash.turbine.test
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import de.gematik.ti.erp.app.userauthentication.observer.AuthenticationModeAndMethod
import de.gematik.ti.erp.app.userauthentication.observer.BiometricPromptBuilder
import de.gematik.ti.erp.app.userauthentication.observer.InactivityTimeoutObserver
import de.gematik.ti.erp.app.userauthentication.presentation.UserAuthenticationController
import de.gematik.ti.erp.app.userauthentication.usecase.ResetAuthenticationTimeOutSystemUptimeUseCase
import de.gematik.ti.erp.app.userauthentication.usecase.SetAuthenticationTimeOutSystemUptimeUseCase
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

class UserAuthenticationControllerTest {
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)
    private val settingsRepository: SettingsRepository = mockk()
    private lateinit var controllerUnderTest: UserAuthenticationController
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
            settingsRepository
        )
        resetAuthenticationTimeOutSystemUptimeUseCase = ResetAuthenticationTimeOutSystemUptimeUseCase(
            settingsRepository
        )

        controllerUnderTest = UserAuthenticationController(
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
                SettingsData.Authentication(
                    password = SettingsData.Authentication.Password(""),
                    deviceSecurity = false,
                    failedAuthenticationAttempts = 4,
                    authenticationTimeOutSystemUptime = null
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
                SettingsData.Authentication(
                    password = SettingsData.Authentication.Password(""),
                    deviceSecurity = false,
                    failedAuthenticationAttempts = 5,
                    authenticationTimeOutSystemUptime = 0
                )
            )
        )
        coEvery { settingsRepository.setAuthenticationTimeOutSystemUptime(5) } returns Unit
        coEvery { settingsRepository.resetAuthenticationTimeOutSystemUptime() } returns Unit

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.authenticationState.test {
                val state = awaitItem()
                val result = controllerUnderTest.calculateAuthenticationTimeOut(state)
                assertEquals(5, result)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `timeout already completed -loading from db- test `() {
        coEvery { inactivityTimeoutObserver.authenticationModeAndMethod } returns flowOf(
            AuthenticationModeAndMethod.AuthenticationRequired(
                SettingsData.Authentication(
                    password = SettingsData.Authentication.Password(""),
                    deviceSecurity = false,
                    failedAuthenticationAttempts = 5,
                    authenticationTimeOutSystemUptime = -5000
                )
            )
        )
        coEvery { settingsRepository.resetAuthenticationTimeOutSystemUptime() } returns Unit

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
