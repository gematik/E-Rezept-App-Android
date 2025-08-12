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

package de.gematik.ti.erp.app.userauthentication.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.userauthentication.presentation.AuthenticationStateData

@Composable
internal fun UserAuthenticationBottomSection() {
    Image(
        painterResource(R.drawable.crew),
        null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium),
        contentScale = ContentScale.FillWidth
    )
}

@Composable
internal fun UserAuthenticationBottomErrorSection(
    state: AuthenticationStateData.AuthenticationState
) {
    Divider(
        modifier = Modifier.fillMaxWidth(),
        color = AppTheme.colors.neutral300
    )
    Column(
        modifier = Modifier
            .padding(
                bottom = PaddingDefaults.Large,
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                top = PaddingDefaults.Medium
            )
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            state.authentication.methodIsDeviceSecurity ->
                Text(
                    text = stringResource(R.string.auth_failed_biometry_info),
                    style = AppTheme.typography.body2l,
                    textAlign = TextAlign.Center
                )
            state.authentication.methodIsPassword ->
                Text(
                    text = stringResource(R.string.auth_failed_password_error_info),
                    style = AppTheme.typography.body2l,
                    textAlign = TextAlign.Center
                )
            state.authentication.bothMethodsAvailable ->
                Text(
                    text = stringResource(R.string.auth_failed_both_methods_error_info),
                    style = AppTheme.typography.body2l,
                    textAlign = TextAlign.Center
                )
        }
    }
}
