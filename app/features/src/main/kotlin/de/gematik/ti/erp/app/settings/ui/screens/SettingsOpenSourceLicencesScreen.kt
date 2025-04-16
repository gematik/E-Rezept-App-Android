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

package de.gematik.ti.erp.app.settings.ui.screens

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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.settings.model.LicenseEntry
import de.gematik.ti.erp.app.settings.model.parseLicenses
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ClickableTaggedText
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.annotatedLinkStringLight
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.openUriWhenValid

const val LicenseFileUri = "open_source_licenses.json"

class SettingsOpenSourceLicencesScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        OpenSourceLicensesScreenScaffoldContent(
            onBack = navController::popBackStack
        )
    }
}

@Composable
fun OpenSourceLicensesScreenScaffoldContent(
    onBack: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    val licenses = rememberLicenses()

    AnimatedElevationScaffold(
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        topBarTitle = stringResource(R.string.settings_legal_licences),
        onBack = onBack
    ) {
        val insetPaddings = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
        LazyColumn(
            modifier = Modifier.padding(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
            contentPadding = PaddingValues(
                start = PaddingDefaults.Medium,
                top = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium + insetPaddings.calculateBottomPadding()
            )
        ) {
            @Requirement(
                "O.Arch_7",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "The list of third party libraries are shown here in a list to the user"
            )
            licenses.forEach {
                item {
                    LicenseItem(item = it)
                }
            }
        }
    }
}

@Composable
private fun rememberLicenses(): List<LicenseEntry> {
    val context = LocalContext.current
    return remember {
        val json = context.assets.open(LicenseFileUri).bufferedReader().readText()
        parseLicenses(json)
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
                    uriHandler.openUriWhenValid(it.item)
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
                    uriHandler.openUriWhenValid(it.item)
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
fun OpenSourceLicensesScreenScaffoldScreenPreview() {
    PreviewAppTheme {
        OpenSourceLicensesScreenScaffoldContent(
            onBack = {}
        )
    }
}
