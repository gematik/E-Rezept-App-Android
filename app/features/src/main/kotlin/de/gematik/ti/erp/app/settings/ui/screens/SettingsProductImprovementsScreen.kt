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

package de.gematik.ti.erp.app.settings.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.settings.navigation.SettingsRoutes
import de.gematik.ti.erp.app.settings.presentation.rememberProductsImprovementsSettingsController
import de.gematik.ti.erp.app.settings.ui.preview.AnalyticsAllowPreviewData
import de.gematik.ti.erp.app.settings.ui.preview.SettingsProductImprovementPreviewParameter
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LabeledSwitch
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.shortToast

class SettingsProductImprovementsScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val settingsController = rememberProductsImprovementsSettingsController()
        val context = LocalContext.current
        val listState = rememberLazyListState()
        val analyticsState by settingsController.analyticsState
        var isAnalyticsAllowed by remember(
            analyticsState.analyticsAllowed
        ) { mutableStateOf(analyticsState.analyticsAllowed) }
        SettingsProductImprovementsScaffold(
            context = context,
            listState = listState,
            isAnalyticsAllowed = isAnalyticsAllowed,
            onIsAnalyticsAllowedChange = { isAnalyticsAllowed = !isAnalyticsAllowed },
            onDisallowAnalytics = {
                settingsController.changeAnalyticsAllowedState(false)
            },
            onNavigateBack = { navController.popBackStack() },
            navigateToAnalytics = {
                navController.navigate(SettingsRoutes.SettingsAllowAnalyticsScreen.path())
            }
        )
    }
}

@Composable
fun SettingsProductImprovementsScaffold(
    context: Context,
    listState: LazyListState,
    isAnalyticsAllowed: Boolean,
    onIsAnalyticsAllowedChange: () -> Unit,
    onDisallowAnalytics: () -> Unit,
    onNavigateBack: () -> Unit,
    navigateToAnalytics: () -> Unit
) {
    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.settings_product_improvement_headline),
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        onBack = onNavigateBack
    ) { contentPadding ->
        SettingsProductImprovementsScreenContent(
            contentPadding = contentPadding,
            listState = listState,
            isAnalyticsAllowed = isAnalyticsAllowed,
            onIsAnalyticsAllowedChange = {
                onIsAnalyticsAllowedChange()
            },
            onDisallowAnalytics = onDisallowAnalytics,
            context = context,
            navigateToAnalytics = navigateToAnalytics
        )
    }
}

@Composable
private fun SettingsProductImprovementsScreenContent(
    contentPadding: PaddingValues,
    listState: LazyListState,
    isAnalyticsAllowed: Boolean,
    onIsAnalyticsAllowedChange: () -> Unit,
    onDisallowAnalytics: () -> Unit,
    context: Context,
    navigateToAnalytics: () -> Unit
) {
    val disallowInfo = stringResource(R.string.settings_tracking_disallow_info)
    LazyColumn(
        contentPadding = contentPadding,
        state = listState
    ) {
        item {
            SpacerMedium()
            @Requirement(
                "O.Purp_5#2",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "The agreement to the use of the analytics framework could be revoked. " +
                    "But other agreements cannot be revoked, since the app could not operate properly."
            )
            AnalyticsSection(
                isAnalyticsAllowed
            ) { state ->
                onIsAnalyticsAllowedChange()
                if (!state) {
                    onDisallowAnalytics()
                    context.shortToast(disallowInfo)
                } else {
                    navigateToAnalytics()
                }
            }
        }
    }
}

@Requirement(
    "A_19089-01#2",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "User can opt-in and opt-out of analytics"
)
@Requirement(
    "A_19097-01#1",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Analytics can be enabled and disabled by the user. The user can revoke using the switch."
)
@Requirement(
    "O.Purp_5#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Toggle within Settings to enable and disable usage analytics."
)
@Requirement(
    "O.Purp_6#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Current Analytics state is inspectable by the user, as most of the user decisions are client side " +
        "no history is available. Only the current state can be inspected."
)
@Composable
private fun AnalyticsSection(
    analyticsAllowed: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    LabeledSwitch(
        checked = analyticsAllowed,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        icon = Icons.Rounded.Timeline,
        header = stringResource(R.string.settings_allow_analytics_header),
        description = stringResource(R.string.settings_allow_analytics_info)
    )
}

@LightDarkPreview
@Composable
fun PreviewSettingsProductImprovementsScreen(
    @PreviewParameter(SettingsProductImprovementPreviewParameter::class)
    analyticsPreviewData: AnalyticsAllowPreviewData
) {
    PreviewAppTheme {
        SettingsProductImprovementsScaffold(
            context = LocalContext.current,
            listState = rememberLazyListState(),
            isAnalyticsAllowed = analyticsPreviewData.isAnalyticsAllowed,
            onIsAnalyticsAllowedChange = {
                analyticsPreviewData.isAnalyticsAllowed =
                    !analyticsPreviewData.isAnalyticsAllowed
            },
            onDisallowAnalytics = {},
            onNavigateBack = {},
            navigateToAnalytics = {}
        )
    }
}
