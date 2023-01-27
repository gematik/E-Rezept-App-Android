/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.license.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.license.model.License
import de.gematik.ti.erp.app.license.model.LicenseEntry
import de.gematik.ti.erp.app.license.model.parseLicenses
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ClickableTaggedText
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.annotatedLinkStringLight

const val LicenseFileUri = "open_source_licenses.json"

@Composable
fun rememberLicenses(): List<LicenseEntry> {
    val context = LocalContext.current
    return remember {
        val json = context.assets.open(LicenseFileUri).bufferedReader().readText()
        parseLicenses(json)
    }
}

@Composable
fun LicenseScreen(
    navigationMode: NavigationBarMode = NavigationBarMode.Back,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    AnimatedElevationScaffold(
        navigationMode = navigationMode,
        listState = listState,
        topBarTitle = stringResource(R.string.settings_legal_licences),
        onBack = onBack
    ) {
        val licenses = rememberLicenses()

        val insetPaddings = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
        LazyColumn(
            modifier = Modifier.padding(),
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
            contentPadding = PaddingValues(
                start = PaddingDefaults.Medium,
                top = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium + insetPaddings.calculateBottomPadding()
            )
        ) {
            licenses.forEach {
                item {
                    LicenseItem(item = it)
                }
            }
        }
    }
}

@Composable
private fun LicenseItem(
    modifier: Modifier = Modifier,
    item: LicenseEntry
) {
    val title = buildAnnotatedString {
        append(item.project)
        if (!item.version.isNullOrBlank()) {
            append(" (${item.version})")
        }
        if (!item.year.isNullOrBlank()) {
            append(" ${item.year}")
        }
    }

    val uriHandler = LocalUriHandler.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
    ) {
        Text(title, style = AppTheme.typography.h6)
        Text(item.dependency, style = AppTheme.typography.body2l, fontStyle = FontStyle.Italic)
        item.description?.let {
            Text(item.description, style = AppTheme.typography.body2l, fontStyle = FontStyle.Italic)
        }
        item.developers.takeIf { it.isNotEmpty() }?.let {
            Text(item.developers.joinToString(), style = AppTheme.typography.body2)
        }
        item.url?.let {
            ClickableTaggedText(
                text = annotatedLinkStringLight(item.url, item.url),
                style = AppTheme.typography.body2
            ) {
                if (it.tag == "URL") {
                    uriHandler.openUri(it.item)
                }
            }
        }
        SpacerSmall()
        item.licenses.forEach {
            ClickableTaggedText(
                text = annotatedLinkStringLight(it.licenseUrl, it.license),
                style = AppTheme.typography.body2
            ) {
                if (it.tag == "URL") {
                    uriHandler.openUri(it.item)
                }
            }
        }
    }
}

@Preview
@Composable
private fun LicenseItemPreview() {
    AppTheme {
        LicenseItem(
            item = LicenseEntry(
                project = "Test 1234",
                description = "Some short description",
                version = "1.2.3",
                developers = listOf(
                    "Some Author",
                    "Another Author",
                    "And Another One"
                ),
                url = "https://localhost/123456",
                year = "2022",
                licenses = listOf(
                    License(
                        "Apache License, Version 2.0",
                        "https://www.apache.org/licenses/LICENSE-2.0"
                    ),
                    License(
                        "Apache License, Version 2.0",
                        "https://www.apache.org/licenses/LICENSE-2.0"
                    )
                ),
                dependency = "de.abc.def:1.2.3"
            )
        )
    }
}
