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

package de.gematik.ti.erp.app.profiles.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.profiles.model.ProfilePairedDevicesErrorState.CannotLoadPairedDevicesError
import de.gematik.ti.erp.app.profiles.model.ProfilePairedDevicesErrorState.NoInternetError
import de.gematik.ti.erp.app.profiles.model.ProfilePairedDevicesErrorState.UserNotLoggedInWithBiometricsError
import de.gematik.ti.erp.app.profiles.usecase.model.PairedDevice
import de.gematik.ti.erp.app.utils.uistate.UiState

class PairedDevicesPreviewParameterProvider : PreviewParameterProvider<UiState<List<PairedDevice>>> {
    override val values: Sequence<UiState<List<PairedDevice>>>
        get() = sequenceOf(
            UiState.Loading(),
            UiState.Empty(),
            UiState.Error(UserNotLoggedInWithBiometricsError),
            UiState.Error(CannotLoadPairedDevicesError),
            UiState.Error(NoInternetError),
            pairedDevices
        )
}

private val pairedDevices = UiState.Data(
    data = listOf(
        PairedDevice(
            name = "Tony StarksPhone",
            alias = "IronPhone",
            connectedOn = "2021-08-01",
            isCurrentDevice = true
        ),
        PairedDevice(
            name = "Thor OdinsonsTablet",
            alias = "HammerTab",
            connectedOn = "2021-07-15",
            isCurrentDevice = false
        ),
        PairedDevice(
            name = "Peter ParkersLaptop",
            alias = "SpideyWeb",
            connectedOn = "2021-09-10",
            isCurrentDevice = false
        ),
        PairedDevice(
            name = "Steve RogersWatch",
            alias = "CapTime",
            connectedOn = "2021-10-05",
            isCurrentDevice = true
        ),
        PairedDevice(
            name = "Bruce BannersSmartwatch",
            alias = "HulkSmashTime",
            connectedOn = "2021-11-20",
            isCurrentDevice = false
        )
    )
)
