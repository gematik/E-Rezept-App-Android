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

package de.gematik.ti.erp.app.onboarding.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.onboarding.model.OnboardingAuthScenario

data class AuthScenarioPreviewData(
    val name: String,
    val authScenario: OnboardingAuthScenario
)

class AuthScenarioPreviewParameterProvider : PreviewParameterProvider<AuthScenarioPreviewData> {
    override val values: Sequence<AuthScenarioPreviewData>
        get() = sequenceOf(
            AuthScenarioPreviewData(
                name = "Device credentials enabled",
                authScenario = OnboardingAuthScenario.DEVICE_CREDENTIALS_ENABLED
            ),
            AuthScenarioPreviewData(
                name = "Biometric enabled",
                authScenario = OnboardingAuthScenario.BIOMETRIC_ENABLED
            ),
            AuthScenarioPreviewData(
                name = "Device credentials available but not set up",
                authScenario = OnboardingAuthScenario.DEVICE_CREDENTIALS_NOT_ENABLED
            ),
            AuthScenarioPreviewData(
                name = "Biometric available but not activated",
                authScenario = OnboardingAuthScenario.BIOMETRIC_NOT_ENABLED
            )
        )
}
