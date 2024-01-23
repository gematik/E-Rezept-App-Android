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

package de.gematik.ti.erp.app.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.provideLinkForString
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ClickableTaggedText
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource

@Composable
fun PharmacyLicenseScreen(onClose: () -> Unit) {
    val scrollState = rememberScrollState()

    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.settings_licence_pharmacy_search),
        navigationMode = NavigationBarMode.Close,
        onBack = onClose,
        elevated = scrollState.value > 0,
        actions = {}
    ) {
        Column(
            modifier = Modifier
                .padding(
                    start = PaddingDefaults.Medium,
                    end = PaddingDefaults.Medium,
                    top = PaddingDefaults.Medium,
                    bottom = (PaddingDefaults.XLarge * 2)
                )
                .verticalScroll(scrollState)
        ) {
            Text(
                stringResource(R.string.license_pharmacy_search_description),
                style = AppTheme.typography.body1,
                color = AppTheme.colors.neutral999
            )

            SpacerMedium()

            val link =
                provideLinkForString(
                    stringResource(id = R.string.license_pharmacy_search_web_link),
                    annotation = stringResource(id = R.string.license_pharmacy_search_web_link),
                    tag = "URL",
                    linkColor = AppTheme.colors.primary500
                )

            val uriHandler = LocalUriHandler.current

            ClickableTaggedText(
                annotatedStringResource(R.string.license_pharmacy_search_web_link_info, link),
                style = AppTheme.typography.body1,
                onClick = { range ->
                    uriHandler.openUri(range.item)
                }
            )
        }
    }
}
