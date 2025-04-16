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

package de.gematik.ti.erp.app.userauthentication.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.userauthentication.presentation.AuthenticationStateData
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerShortMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.PrimaryButton

@Composable
internal fun UserAuthenticationLoginSection(
    authenticationState: AuthenticationStateData.AuthenticationState,
    onAuthenticate: () -> Unit,
    onShowPasswordDialog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.auth_title),
            style = AppTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        SpacerTiny()
        Text(
            stringResource(R.string.auth_body),
            style = AppTheme.typography.body1l,
            textAlign = TextAlign.Center
        )
        SpacerLarge()

        PrimaryButton(
            onClick = onAuthenticate,
            elevation = ButtonDefaults.elevation(SizeDefaults.one),
            shape = RoundedCornerShape(SizeDefaults.one),
            contentPadding = PaddingValues(
                horizontal = PaddingDefaults.Large,
                vertical = PaddingDefaults.ShortMedium
            )
        ) {
            Icon(Icons.Rounded.LockOpen, null)
            SpacerShortMedium()
            Text(
                if (authenticationState.authentication.failedAuthenticationAttempts > 0) {
                    stringResource(R.string.auth_retry_button)
                } else {
                    stringResource(R.string.auth_button)
                }
            )
        }

        SpacerMedium()
        if (
            authenticationState.authentication.bothMethodsAvailable
        ) {
            TextButton(onClick = onShowPasswordDialog) {
                Text(
                    text = stringResource(id = R.string.auth_alternative_button),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
