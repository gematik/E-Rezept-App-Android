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

package de.gematik.ti.erp.app.utils.compose.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
fun NamedPreviewBox(
    name: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    PreviewAppTheme {
        val isDark = isSystemInDarkTheme()
        Surface(
            modifier = modifier
                .background(if (isDark) AppTheme.colors.neutral999 else AppTheme.colors.neutral000)
                .padding(SizeDefaults.double)
                .border(SizeDefaults.eighth, Color.Gray)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(if (isDark) AppTheme.colors.neutral999 else AppTheme.colors.neutral000)
                    .padding(SizeDefaults.one)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) AppTheme.colors.neutral100 else AppTheme.colors.neutral800,
                    modifier = Modifier.padding(bottom = SizeDefaults.one)
                )
                content()
            }
        }
    }
}
