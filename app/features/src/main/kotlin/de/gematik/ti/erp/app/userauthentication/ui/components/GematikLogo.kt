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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.features.BuildConfig
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.OutlinedDebugButton

@Composable
internal fun GematikLogo(
    onSkipAuthentication: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(top = PaddingDefaults.Medium)
            .padding(horizontal = PaddingDefaults.Medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            painterResource(R.drawable.ic_onboarding_logo_flag),
            null,
            modifier = Modifier.padding(end = SizeDefaults.oneQuarter)
        )
        Icon(
            painterResource(R.drawable.ic_onboarding_logo_gematik),
            null,
            tint = AppTheme.colors.primary900
        )
        if (BuildKonfig.INTERNAL && BuildConfig.DEBUG) {
            Spacer(modifier = Modifier.weight(1f))
            OutlinedDebugButton("SKIP", onClick = onSkipAuthentication)
        }
    }
}
