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
import de.gematik.ti.erp.app.authentication.presentation.biometricStatus
import de.gematik.ti.erp.app.authentication.presentation.deviceHasAuthenticationMethodEnabled
import de.gematik.ti.erp.app.authentication.presentation.deviceSecurityStatus
import de.gematik.ti.erp.app.authentication.presentation.deviceSupportsAuthenticationMethod
import de.gematik.ti.erp.app.onboarding.model.OnboardingAuthScenario

class DetermineAuthScenarioUseCase(
    private val context: Context
) {
    operator fun invoke(): OnboardingAuthScenario {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        val deviceSupportsBiometric = deviceSupportsAuthenticationMethod(context.biometricStatus())
        val deviceSupportsDeviceSecurity = deviceSupportsAuthenticationMethod(context.deviceSecurityStatus())
        val deviceHasBiometryEnabled = deviceHasAuthenticationMethodEnabled(context.biometricStatus())
        val deviceHasDeviceSecurityEnabled = keyguardManager.isDeviceSecure or
            deviceHasAuthenticationMethodEnabled(context.deviceSecurityStatus())

        return when {
            // Priority 1: If biometric is enabled, always show biometric
            deviceHasBiometryEnabled -> OnboardingAuthScenario.BIOMETRIC_ENABLED

            // Priority 2: If device security is enabled (and biometric is not), show device security
            deviceHasDeviceSecurityEnabled -> OnboardingAuthScenario.DEVICE_CREDENTIALS_ENABLED

            // Priority 3: If biometric is supported but not enabled, show biometric not enabled
            deviceSupportsBiometric -> OnboardingAuthScenario.BIOMETRIC_NOT_ENABLED

            // Priority 4: If device security is supported but not enabled, show device security not enabled
            deviceSupportsDeviceSecurity -> OnboardingAuthScenario.DEVICE_CREDENTIALS_NOT_ENABLED

            else -> OnboardingAuthScenario.BIOMETRIC_NOT_ENABLED
        }
    }
}
