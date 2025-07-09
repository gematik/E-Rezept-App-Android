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

package de.gematik.ti.erp.app.troubleshooting.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode

@Composable
fun TroubleShootingScaffold(
    title: String,
    onBack: () -> Unit,
    bottomBarButton: @Composable RowScope.() -> Unit,
    content: @Composable () -> Unit
) {
    val listState = rememberLazyListState()

    AnimatedElevationScaffold(
        modifier = Modifier.testTag("cardWall/intro"),
        topBarTitle = stringResource(R.string.cdw_troubleshooting_title),
        topBarColor = MaterialTheme.colors.surface,
        listState = listState,
        actions = {},
        navigationMode = NavigationBarMode.Back,
        bottomBar = {
            Surface(
                color = MaterialTheme.colors.surface,
                elevation = SizeDefaults.half
            ) {
                Row(Modifier.navigationBarsPadding()) {
                    bottomBarButton()
                }
            }
        },
        onBack = onBack
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .padding(PaddingDefaults.Medium)
        ) {
            item {
                Text(
                    title,
                    style = AppTheme.typography.h6,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.fillMaxWidth()
                )
                SpacerLarge()
            }
            item {
                content()
            }
        }
    }
}
