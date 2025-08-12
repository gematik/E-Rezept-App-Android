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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.userauthentication.presentation.AuthenticationStateData
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintCardDefaults
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource

@Composable
fun AuthenticationHintCard(
    state: AuthenticationStateData.AuthenticationState
) {
    Box(
        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
    ) {
        HintCard(
            modifier = Modifier.padding(vertical = PaddingDefaults.Medium),
            properties = HintCardDefaults.flatProperties(
                backgroundColor = AppTheme.colors.red100
            ),
            image = {
                HintSmallImage(
                    painterResource(R.drawable.oh_no_girl_hint_red),
                    innerPadding = it
                )
            },
            title = { Text(stringResource(R.string.auth_error_failed_auths_headline)) },
            body = {
                Text(
                    annotatedPluralsResource(
                        R.plurals.auth_error_failed_auths_info,
                        state.authentication.failedAuthenticationAttempts,
                        AnnotatedString(state.authentication.failedAuthenticationAttempts.toString())
                    )
                )
            }
        )
    }
}
