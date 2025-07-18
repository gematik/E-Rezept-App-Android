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

package de.gematik.ti.erp.app.cardwall.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.cardwall.ui.screens.AuthenticationMethod

data class CardWallSaveCredentialsScreenPreviewData(
    val name: String,
    val selectedAuthMode: AuthenticationMethod,
    val showFutureLogOutHint: Boolean
)

data class CardWallSaveCredentialsInfoScreenPreviewData(
    val name: String,
    val isBiometricStrong: Boolean
)

class CardWallSaveCredentialsPreviewParameterProvider :
    PreviewParameterProvider<CardWallSaveCredentialsScreenPreviewData> {
    override val values = sequenceOf(
        CardWallSaveCredentialsScreenPreviewData(
            name = "AuthenticationMethod.None",
            selectedAuthMode = AuthenticationMethod.None,
            showFutureLogOutHint = true
        ),
        CardWallSaveCredentialsScreenPreviewData(
            name = "AuthenticationMethod.Alternative",
            selectedAuthMode = AuthenticationMethod.Alternative,
            showFutureLogOutHint = false
        ),
        CardWallSaveCredentialsScreenPreviewData(
            name = "AuthenticationMethod.HealthCard",
            selectedAuthMode = AuthenticationMethod.HealthCard,
            showFutureLogOutHint = true
        )
    )
}

class CardWallSaveCredentialsInfoPreviewParameterProvider :
    PreviewParameterProvider<CardWallSaveCredentialsInfoScreenPreviewData> {
    override val values = sequenceOf(
        CardWallSaveCredentialsInfoScreenPreviewData(
            name = "BiometricStrong",
            isBiometricStrong = true
        ),
        CardWallSaveCredentialsInfoScreenPreviewData(
            name = "BiometricWeak",
            isBiometricStrong = false
        )
    )
}
