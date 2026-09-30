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

package de.gematik.ti.erp.app.pushnotifications.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.pushnotifications.ui.model.RegisteredDeviceUiModel
import de.gematik.ti.erp.app.pushnotifications.ui.model.RegisteredPushDevicesErrorState.CannotLoadRegisteredPushDevicesError
import de.gematik.ti.erp.app.pushnotifications.ui.model.RegisteredPushDevicesErrorState.NoInternetErrorPush
import de.gematik.ti.erp.app.utils.uistate.UiState

class RegisteredDevicesPreviewParameterProvider :
    PreviewParameterProvider<UiState<List<RegisteredDeviceUiModel>>> {
    override val values: Sequence<UiState<List<RegisteredDeviceUiModel>>> = sequenceOf(
        UiState.Loading(),
        UiState.Empty(),
        UiState.Data(
            listOf(
                RegisteredDeviceUiModel(
                    name = "Pixel 10",
                    pushKey = "pushkey-current",
                    appId = "de.gematik.erezept",
                    isCurrentDevice = true
                ),
                RegisteredDeviceUiModel(
                    name = "iPhone 16",
                    pushKey = "pushkey-other",
                    appId = "de.gematik.erezept",
                    isCurrentDevice = false
                )
            )
        ),
        UiState.Error(NoInternetErrorPush),
        UiState.Error(CannotLoadRegisteredPushDevicesError)
    )
}
