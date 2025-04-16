/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.cardwall.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.idp.model.HealthInsuranceData
import de.gematik.ti.erp.app.theme.PaddingDefaults

@Composable
fun GidItem(
    profileId: String,
    healthInsuranceData: HealthInsuranceData,
    onClickHealthInsuranceIdp: (String, HealthInsuranceData) -> Unit,
    onNotGid: () -> Unit
) {
    Text(
        modifier = Modifier
            .padding(PaddingDefaults.Medium)
            .fillMaxWidth()
            .clickable {
                if (healthInsuranceData.isGid) {
                    onClickHealthInsuranceIdp(profileId, healthInsuranceData)
                } else {
                    onNotGid()
                }
            },
        text = healthInsuranceData.name
    )
}
