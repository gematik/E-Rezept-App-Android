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

package de.gematik.ti.erp.app.bottombar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.extensions.rememberBottomScrollElevationState
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
fun AnimatedBottomBar(
    listState: LazyListState,
    content: @Composable ColumnScope.() -> Unit
) {
    val isBottomElevated by listState.rememberBottomScrollElevationState()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        elevation = if (isBottomElevated) SizeDefaults.double else SizeDefaults.zero,
        color = MaterialTheme.colors.surface
    ) {
        Column {
            // Optional: subtle top divider
            if (isBottomElevated) {
                Divider(
                    color = AppTheme.colors.neutral200,
                    thickness = SizeDefaults.eighth
                )
            }
            content()
        }
    }
}
