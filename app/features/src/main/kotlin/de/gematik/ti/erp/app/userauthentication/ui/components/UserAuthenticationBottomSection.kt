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

package de.gematik.ti.erp.app.userauthentication.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.settings.ui.screens.provideLinkForString
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.userauthentication.presentation.AuthenticationStateData
import de.gematik.ti.erp.app.utils.compose.ClickableTaggedText
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.extensions.openUriWhenValid

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
    Column(
        modifier = Modifier
            .background(
                color = AppTheme.colors.neutral100
            )
            .padding(
                bottom = PaddingDefaults.Large,
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                top = PaddingDefaults.Medium
            )
            .fillMaxWidth()
    ) {
        when {
            state.authentication.deviceSecurity ->
                Text(
                    text = stringResource(R.string.auth_failed_biometry_info),
                    style = AppTheme.typography.body2l,
                    textAlign = TextAlign.Center
                )
            else -> {
                val link =
                    provideLinkForString(
                        stringResource(id = R.string.auth_link_to_gematik_helptext),
                        annotation = stringResource(id = R.string.auth_link_to_gematik_q_and_a),
                        tag = "URL",
                        linkColor = AppTheme.colors.primary500
                    )

                val uriHandler = LocalUriHandler.current

                ClickableTaggedText(
                    annotatedStringResource(R.string.auth_failed_password_error_info, link),
                    style = AppTheme.typography.body2l,
                    onClick = { range ->
                        uriHandler.openUriWhenValid(range.item)
                    }
                )
            }
        }
    }
}
