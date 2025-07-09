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

@file:Suppress("UnusedPrivateMember")

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@LightDarkPreview
@Composable
fun SimpleBannerPreview() {
    PreviewAppTheme {
        Scaffold { paddingValues ->
            SimpleBanner(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(top = PaddingDefaults.Tiny),
                containerColor = AppTheme.colors.neutral200,
                text = stringResource(R.string.no_internet_text)
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun AllIconsBannerPreview() {
    PreviewAppTheme {
        Banner(
            title = "Lorem ipsum dolor sit amet",
            text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt " +
                "ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et",
            startIcon = BannerClickableIcon(BannerIcon.Info) {},
            gearsIcon = BannerClickableIcon(BannerIcon.Gears) {},
            bottomIcon = BannerClickableTextIcon(
                text = "Lorem ipsum dolor sit amet",
                icon = BannerIcon.Custom(Icons.AutoMirrored.Filled.ArrowForward, AppTheme.colors.neutral999)
            ) {},
            onClickClose = {}
        )
    }
}

@LightDarkPreview
@Composable
private fun OnlyStartIconBannerPreview() {
    PreviewAppTheme {
        Banner(
            title = "Lorem ipsum dolor sit amet",
            startIcon = BannerClickableIcon(BannerIcon.Warning) {},
            contentColor = AppTheme.colors.neutral000,
            containerColor = AppTheme.colors.yellow600,
            text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt " +
                "ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et"
        )
    }
}

@LightDarkPreview
@Composable
private fun OnlyGearsIconBannerPreview() {
    PreviewAppTheme {
        Banner(
            title = "Lorem ipsum dolor sit amet",
            contentColor = AppTheme.colors.neutral999,
            containerColor = AppTheme.colors.primary200,
            text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt " +
                "ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et",
            gearsIcon = BannerClickableIcon(BannerIcon.Gears) {}
        )
    }
}

@LightDarkPreview
@Composable
private fun BottomTextBannerPreview() {
    PreviewAppTheme {
        Banner(
            title = "404 Seite?",
            contentColor = AppTheme.colors.neutral999,
            containerColor = AppTheme.colors.primary100,
            text = "Bitte aktivieren Sie in Ihren Einstellungen das Öffnen von Links, um fortzufahren",
            bottomIcon = BannerClickableTextIcon(
                text = "Lorem ipsum dolor sit amet",
                icon = BannerIcon.Custom(Icons.AutoMirrored.Filled.ArrowForward, AppTheme.colors.primary700)
            ) {},
            onClickClose = {}
        )
    }
}

@LightDarkPreview
@Composable
private fun NoInternetBannerPreview() {
    PreviewAppTheme {
        Banner(
            contentColor = AppTheme.colors.neutral000,
            containerColor = AppTheme.colors.yellow600,
            text = "Kein Internet"
        )
    }
}
