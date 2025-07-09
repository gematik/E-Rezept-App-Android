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

package de.gematik.ti.erp.app.onboarding.usecase

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.biometric.BiometricManager
import de.gematik.ti.erp.app.onboarding.model.OnboardingAuthScenario
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DetermineAuthScenarioUseCaseTest {
    private val context: Context = mockk(relaxed = true)
    private val keyguardManager: KeyguardManager = mockk()
    private val packageManager: PackageManager = mockk(relaxed = true)
    private val biometricManager: BiometricManager = mockk()
    private lateinit var determineAuthScenarioUseCase: DetermineAuthScenarioUseCase

    @Before
    fun setUp() {
        mockkStatic(BiometricManager::class)

        every { context.getSystemService(Context.KEYGUARD_SERVICE) } returns keyguardManager
        every { context.applicationContext } returns context
        every { context.packageManager } returns packageManager
        every { BiometricManager.from(context) } returns biometricManager

        every {
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
        } returns BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE

        every {
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        } returns BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE

        every { packageManager.hasSystemFeature(any()) } returns false

        determineAuthScenarioUseCase = DetermineAuthScenarioUseCase(context)
    }

    @After
    fun tearDown() {
        unmockkStatic(BiometricManager::class)
    }

    @Test
    fun `invoke returns BIOMETRIC_ENABLED when biometric is available and enabled`() = runTest {
        // Device with biometric enabled (fingerprint/face unlock working)
        every { keyguardManager.isDeviceSecure } returns true
        every {
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
        } returns BiometricManager.BIOMETRIC_SUCCESS
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) } returns true

        val result = determineAuthScenarioUseCase.invoke()

        // Prioritize biometric when its available
        assertEquals(OnboardingAuthScenario.BIOMETRIC_ENABLED, result)
    }

    @Test
    fun `invoke returns BIOMETRIC_NOT_ENABLED when biometric is supported but not set up`() = runTest {
        // Device supports biometric but user hasn't enrolled any fingerprints/face
        every { keyguardManager.isDeviceSecure } returns false
        every {
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
        } returns BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) } returns true

        val result = determineAuthScenarioUseCase.invoke()

        // use biometric when hardware supports it
        assertEquals(OnboardingAuthScenario.BIOMETRIC_NOT_ENABLED, result)
    }

    @Test
    fun `invoke returns DEVICE_CREDENTIALS_ENABLED when device is secure via keyguard`() = runTest {
        // Device has PIN/password set up, no biometric
        every { keyguardManager.isDeviceSecure } returns true
        every {
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
        } returns BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE

        val result = determineAuthScenarioUseCase.invoke()

        // use device credentials when keyguard reports device as secure
        assertEquals(OnboardingAuthScenario.DEVICE_CREDENTIALS_ENABLED, result)
    }

    @Test
    fun `invoke returns DEVICE_CREDENTIALS_NOT_ENABLED when only device security is supported but not enabled`() = runTest {
        // Device with no biometric hardware but supports device credentials (PIN/password)
        every { keyguardManager.isDeviceSecure } returns false

        // no biometric hardware available
        every {
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
        } returns BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
        // device credential supported but not enrolled
        every {
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        } returns BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) } returns false

        val result = determineAuthScenarioUseCase.invoke()

        // use device credentials when only device security is available
        assertEquals(OnboardingAuthScenario.DEVICE_CREDENTIALS_NOT_ENABLED, result)
    }
}
