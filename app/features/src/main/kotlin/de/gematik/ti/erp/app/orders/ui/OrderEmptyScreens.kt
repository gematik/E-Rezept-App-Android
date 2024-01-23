/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.orders.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.prescription.ui.EmptyScreenHome
import de.gematik.ti.erp.app.prescription.ui.HomeConnectedWithoutToken
import de.gematik.ti.erp.app.prescription.ui.HomeConnectedWithoutTokenBiometrics
import de.gematik.ti.erp.app.prescription.ui.HomeHealthCardDisconnected
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile.Companion.ProfileConnectionState
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.SpacerSmall

@Composable
fun LazyItemScope.OrderEmptyScreen(
    connectionState: ProfileConnectionState?,
    onClickRefresh: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillParentMaxSize()
            .padding(PaddingDefaults.Medium),
        contentAlignment = Alignment.Center
    ) {
        when (connectionState) {
            ProfileConnectionState.LoggedOut -> {
                HomeHealthCardDisconnected(
                    onClickAction = onClickRefresh
                )
            }
            ProfileConnectionState.LoggedOutWithoutTokenBiometrics -> {
                HomeConnectedWithoutTokenBiometrics(
                    onClickAction = onClickRefresh
                )
            }
            ProfileConnectionState.LoggedOutWithoutToken -> {
                HomeConnectedWithoutToken(
                    onClickAction = onClickRefresh
                )
            }
            else -> {
                NoOrders(
                    onClickRefresh = onClickRefresh
                )
            }
        }
    }
}

@Composable
private fun NoOrders(
    modifier: Modifier = Modifier,
    onClickRefresh: () -> Unit
) =
    EmptyScreenHome(
        modifier = modifier,
        header = stringResource(R.string.orders_empty_title),
        description = stringResource(R.string.orders_empty_subtitle),
        image = {
            Image(
                painterResource(R.drawable.woman_red_shirt_circle_blue),
                contentDescription = null,
                modifier = Modifier.size(160.dp)
            )
        },
        button = {
            TextButton(
                onClick = onClickRefresh
            ) {
                Icon(
                    Icons.Rounded.Refresh,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = AppTheme.colors.primary600
                )
                SpacerSmall()
                Text(text = stringResource(R.string.home_egk_redeemed_buttontext), textAlign = TextAlign.Right)
            }
        }
    )
