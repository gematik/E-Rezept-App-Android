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

package de.gematik.ti.erp.app.cardwall.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.BannerClickableTextIcon
import de.gematik.ti.erp.app.utils.compose.BannerIcon
import de.gematik.ti.erp.app.utils.compose.ErezeptBanner
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PreviewAppTheme

@Composable
fun DomainVerifierBanner(
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    ErezeptBanner(
        contentColor = AppTheme.colors.neutral999,
        containerColor = AppTheme.colors.primary100,
        title = stringResource(R.string.domain_verifier_banner_title),
        text = stringResource(R.string.domain_verifier_dialog_body),
        bottomIcon = BannerClickableTextIcon(
            text = stringResource(R.string.domain_verifier_dialog_button),
            icon = BannerIcon.Custom(
                Icons.Default.ArrowForward,
                AppTheme.colors.primary600
            )
        ) {
            onClick()
        },
        onClickClose = onClose
    )
}

@LightDarkPreview
@Composable
fun DomainVerifierBannerPreview() {
    PreviewAppTheme {
        DomainVerifierBanner(
            onClick = {},
            onClose = {}
        )
    }
}
