/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.messages.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.EmptyScreenComponent

@Composable
internal fun NoOrders(
    modifier: Modifier = Modifier,
    onClickRefresh: () -> Unit
) =
    EmptyScreenComponent(
        modifier = modifier,
        title = stringResource(R.string.messages_empty_title),
        body = stringResource(R.string.messages_empty_subtitle),
        image = {
            Image(
                painterResource(R.drawable.woman_red_shirt_circle_blue),
                contentDescription = null,
                modifier = Modifier.size(SizeDefaults.twentyfold)
            )
        },
        button = {
            TextButton(
                onClick = onClickRefresh
            ) {
                Icon(
                    Icons.Rounded.Refresh,
                    null,
                    modifier = Modifier.size(SizeDefaults.double),
                    tint = AppTheme.colors.primary600
                )
                SpacerSmall()
                Text(text = stringResource(R.string.home_egk_redeemed_buttontext), textAlign = TextAlign.Right)
            }
        }
    )
