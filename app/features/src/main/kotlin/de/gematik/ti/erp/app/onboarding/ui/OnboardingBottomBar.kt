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

package de.gematik.ti.erp.app.onboarding.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerShortMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall

@Composable
fun OnboardingBottomBar(
    modifier: Modifier = Modifier,
    info: String?,
    buttonText: String,
    buttonEnabled: Boolean,
    buttonModifier: Modifier,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.neutral000)
            .padding(horizontal = PaddingDefaults.Medium)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpacerShortMedium()
        if (info != null) {
            Text(info, style = AppTheme.typography.caption1l, textAlign = TextAlign.Center)
            SpacerSmall()
        }
        PrimaryButtonSmall(
            modifier = buttonModifier,
            enabled = buttonEnabled,
            onClick = onButtonClick
        ) {
            Text(buttonText)
        }
        SpacerLarge()
    }
}
