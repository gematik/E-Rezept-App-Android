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

package de.gematik.ti.erp.app.userauthentication.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
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
                        state.nrOfAuthFailures,
                        AnnotatedString(state.nrOfAuthFailures.toString())
                    )
                )
            }
        )
    }
}
