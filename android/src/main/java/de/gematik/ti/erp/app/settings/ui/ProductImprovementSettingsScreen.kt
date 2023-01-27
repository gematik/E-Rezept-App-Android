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

package de.gematik.ti.erp.app.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LabeledSwitch
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.provideWebIntent

@Composable
fun ProductImprovementSettingsScreen(
    settingsViewModel: SettingsViewModel,
    onAllowAnalytics: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val state by produceState(SettingsScreen.defaultState) {
        settingsViewModel.screenState().collect {
            value = it
        }
    }

    val listState = rememberLazyListState()

    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.settings_product_improvement_headline),
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        onBack = onBack
    ) {
        LazyColumn(
            contentPadding = it,
            state = listState
        ) {
            item {
                SpacerMedium()
                AnalyticsSection(
                    state.analyticsAllowed
                ) { allow ->
                    onAllowAnalytics(allow)
                }
            }
            item {
                SurveySection()
            }
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
