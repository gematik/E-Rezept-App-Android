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

package de.gematik.ti.erp.app.shimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
fun LabelShimmer(
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = Modifier
            .then(modifier)
            .clip(CircleShape)
            .height(SizeDefaults.oneHalf)
            .width(SizeDefaults.eightfoldAndHalf)
            .background(AppTheme.colors.neutral400)
    )
}

@Suppress("MagicNumber")
@Composable
fun LabelWithIconShimmer(
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        LabelShimmer(
            modifier = Modifier
                .weight(0.5f)
                .padding(top = SizeDefaults.fivefoldHalf)
        )
        Spacer(
            modifier = Modifier.weight(0.25f)
        )
        Icon(
            modifier = Modifier.weight(0.25f),
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = AppTheme.colors.neutral400
        )
    }
}

@Suppress("UnusedPrivateMember")
@Composable
@LightDarkPreview
private fun LabelShimmerPreview() {
    PreviewTheme {
        LabelShimmer()
    }
}

@Suppress("UnusedPrivateMember")
@Composable
@Preview
private fun LabelWithIconShimmerPreview() {
    PreviewTheme {
        LabelWithIconShimmer()
    }
}
