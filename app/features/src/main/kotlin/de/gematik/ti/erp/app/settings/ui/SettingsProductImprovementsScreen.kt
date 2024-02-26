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

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.settings.navigation.SettingsNavigationScreens
import de.gematik.ti.erp.app.settings.presentation.rememberProductsImprovementsSettingsController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LabeledSwitch
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.provideWebIntent
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
        AnimatedElevationScaffold(
            topBarTitle = stringResource(R.string.settings_product_improvement_headline),
            navigationMode = NavigationBarMode.Back,
            listState = listState,
            onBack = navController::popBackStack
        ) { contentPadding ->
            SettingsProductImprovementsScreenContent(
                contentPadding,
                listState,
                onDisallowAnalytics = { settingsController.changeAnalyticsAllowedState(false) },
                isAnalyticsAllowed = isAnalyticsAllowed,
                onIsAnalyticsAllowedChange = { isAnalyticsAllowed = !isAnalyticsAllowed },
                context = context,
                navigateToAnalytics = {
                    navController.navigate(SettingsNavigationScreens.SettingsAllowAnalyticsScreen.path())
                }
            )
        }
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
            AnalyticsSection(
                isAnalyticsAllowed
            ) { state ->
                onIsAnalyticsAllowedChange()
                if (!state) {
                    onDisallowAnalytics()
                    context.shortToast(disallowInfo)
                } else {
                    @Requirement(
                        "O.Purp_5#2",
                        sourceSpecification = "BSI-eRp-ePA",
                        rationale = "The agreement to the use of the analytics framework could be revoked. " +
                            "But other agreements cannot be revoked, since the app could not operate properly."
                    )
                    @Requirement(
                        "A_19982",
                        sourceSpecification = "gemSpec_eRp_FdV",
                        rationale = "The agreement to the use of the analytics framework could be revoked. " +
                            "But other agreements cannot be revoked, since the app could not operate properly."
                    )
                    navigateToAnalytics()
                }
            }
        }
        item {
            SurveySection()
        }
    }
}

@Composable
private fun SurveySection() {
    val context = LocalContext.current
    val surveyAddress = stringResource(R.string.settings_contact_survey_address)

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = { context.handleIntent(provideWebIntent(surveyAddress)) },
                role = Role.Button
            )
            .padding(PaddingDefaults.Medium)
            .semantics(mergeDescendants = true) {}
    ) {
        Icon(Icons.Outlined.OpenInBrowser, null, tint = AppTheme.colors.primary600)
        SpacerSmall()
        Column(
            modifier = Modifier
                .weight(1.0f)
                .padding(horizontal = PaddingDefaults.Small)
        ) {
            Text(
                text = stringResource(R.string.settings_contact_feedback),
                style = AppTheme.typography.body1
            )
            Text(
                text = stringResource(R.string.settings_contact_feedback_description),
                style = AppTheme.typography.body2l
            )
        }
    }
}

@Requirement(
    "A_19088#2",
    "A_20187#3",
    "A_19088",
    "A_19089#2",
    "A_19097",
    "A_19181-01#2",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "User can opt-in and opt-out of analytics"
)
@Requirement(
    "O.Purp_5#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Toggle within Settings to enable and disable usage analytics."
)
@Requirement(
    "O.Purp_6#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Current Analytics state is inspectable by the user, as most of the user deciscions are client side " +
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
