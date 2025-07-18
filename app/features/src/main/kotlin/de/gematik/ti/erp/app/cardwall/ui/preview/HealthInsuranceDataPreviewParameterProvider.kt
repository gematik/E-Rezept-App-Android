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
import de.gematik.ti.erp.app.idp.model.HealthInsuranceData
import de.gematik.ti.erp.app.utils.uistate.UiState

class HealthInsuranceDataPreviewParameterProvider : PreviewParameterProvider<UiState<List<HealthInsuranceData>>> {
    override val values = sequenceOf(
        UiState.Loading(),
        UiState.Empty(),
        UiState.Error(Throwable("Error")),
        UiState.Data(
            listOf(
                HealthInsuranceData(
                    name = "Insurance 1",
                    id = "1",
                    isPKV = false,
                    logo = null
                ),
                HealthInsuranceData(
                    name = "Insurance 2",
                    id = "2",
                    isPKV = false,
                    logo = null
                ),
                HealthInsuranceData(
                    name = "Insurance 3",
                    id = "3",
                    isPKV = false,
                    logo = null
                ),
                HealthInsuranceData(
                    name = "Insurance 4",
                    id = "4",
                    isPKV = false,
                    logo = null
                ),
                HealthInsuranceData(
                    name = "Insurance 5",
                    id = "5",
                    isPKV = false,
                    logo = null
                ),
                HealthInsuranceData(
                    name = "Insurance 6",
                    id = "6",
                    isPKV = false,
                    logo = null
                ),
                HealthInsuranceData(
                    name = "Insurance 7",
                    id = "7",
                    isPKV = false,
                    logo = null
                ),
                HealthInsuranceData(
                    name = "Insurance 8",
                    id = "8",
                    isPKV = false,
                    logo = null
                ),
                HealthInsuranceData(
                    name = "Insurance 9",
                    id = "9",
                    isPKV = false,
                    logo = null
                ),
                HealthInsuranceData(
                    name = "Insurance 10",
                    id = "10",
                    isPKV = false,
                    logo = null
                )
            )
        )
    )
}
