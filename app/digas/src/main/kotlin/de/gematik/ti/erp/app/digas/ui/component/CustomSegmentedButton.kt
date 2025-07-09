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

package de.gematik.ti.erp.app.digas.ui.component

import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRowScope
import androidx.compose.runtime.Composable
import de.gematik.ti.erp.app.theme.AppTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SingleChoiceSegmentedButtonRowScope.CustomSegmentedButton(
    index: Int,
    options: List<String>,
    selectedIndex: Int,
    label: String,
    onClick: (Int) -> Unit = {}
) {
    SegmentedButton(
        shape = SegmentedButtonDefaults.itemShape(
            index = index,
            count = options.size
        ),
        onClick = { onClick(index) },
        selected = index == selectedIndex,
        label = { Text(label) },
        colors = SegmentedButtonDefaults.colors(
            activeBorderColor = AppTheme.colors.primary900,
            activeContainerColor = AppTheme.colors.primary200,
            activeContentColor = AppTheme.colors.primary900,
            inactiveContentColor = AppTheme.colors.primary900,
            inactiveBorderColor = AppTheme.colors.primary900,
            inactiveContainerColor = AppTheme.colors.neutral000
        )
    )
}
