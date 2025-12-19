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
package de.gematik.ti.erp.app.eurezept.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
internal fun EuLogo(
    size: Dp = SizeDefaults.thirtyfivefold
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(AppTheme.colors.euLogoColor),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.eu_flag_stars),
            contentDescription = "",
            modifier = Modifier.size(size)
        )
    }
}

@Composable
fun EuLogoRounded(
    modifier: Modifier = Modifier,
    size: Dp = SizeDefaults.sixfold
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(AppTheme.colors.euLogoColor)
            .padding(SizeDefaults.threeQuarter)
    ) {
        Image(
            painter = painterResource(id = R.drawable.eu_flag_stars),
            contentDescription = "",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@LightDarkPreview
@Composable
private fun EuLogoPreview() {
    PreviewTheme {
        EuLogo()
    }
}

@LightDarkPreview
@Composable
private fun EuLogoRoundedPreview() {
    PreviewTheme {
        EuLogoRounded()
    }
}
