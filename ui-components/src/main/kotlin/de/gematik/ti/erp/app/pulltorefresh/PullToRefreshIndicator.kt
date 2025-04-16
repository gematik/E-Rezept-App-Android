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

package de.gematik.ti.erp.app.pulltorefresh

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import de.gematik.ti.erp.app.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PullToRefreshIndicator(
    state: PullToRefreshState,
    modifier: Modifier = Modifier,
    containerColor: Color = AppTheme.colors.neutral000,
    color: Color = AppTheme.colors.primary400
) {
    PullToRefreshContainer(
        modifier = modifier,
        state = state,
        containerColor = containerColor,
        contentColor = color
    )
}
