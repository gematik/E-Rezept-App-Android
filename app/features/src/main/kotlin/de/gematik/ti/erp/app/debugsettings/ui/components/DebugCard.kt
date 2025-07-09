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

package de.gematik.ti.erp.app.debugsettings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium

@Composable
fun DebugCard(
    modifier: Modifier = Modifier,
    title: String,
    onReset: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) =
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        backgroundColor = AppTheme.colors.neutral100,
        elevation = 10.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            content = {
                Column(
                    Modifier.padding(PaddingDefaults.Medium),
                    verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    SpacerMedium()
                    content()
                }
                onReset?.run {
                    IconButton(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(PaddingDefaults.Small),
                        onClick = onReset
                    ) {
                        Icon(Icons.Rounded.Refresh, null)
                    }
                }
            }
        )
    }
