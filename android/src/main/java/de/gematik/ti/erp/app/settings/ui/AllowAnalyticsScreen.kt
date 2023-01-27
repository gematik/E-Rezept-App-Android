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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.onboarding.ui.OnboardingBottomBar
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold

import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.annotatedStringBold
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.createToastShort

@Composable
fun AllowAnalyticsScreen(onAllowAnalytics: (Boolean) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val allowStars = stringResource(R.string.settings_tracking_allow_emoji)
    val allowText = annotatedStringResource(
        R.string.settings_tracking_allow_info,
        annotatedStringBold(allowStars)
    ).toString()
    val disAllowToast = stringResource(R.string.settings_tracking_disallow_info)
    val lazyListState = rememberLazyListState()

    AnimatedElevationScaffold(
        modifier = Modifier.navigationBarsPadding(),
        navigationMode = NavigationBarMode.Back,
        topBarTitle = stringResource(R.string.settings_tracking_allow_title),
        onBack = {
            onAllowAnalytics(false)
            createToastShort(context, disAllowToast)
            onBack()
        },
        listState = lazyListState,
        bottomBar = {
            OnboardingBottomBar(
                buttonText = stringResource(R.string.settings_tracking_allow_button),
                onButtonClick = {
                    onAllowAnalytics(true)
                    createToastShort(context, allowText)
                    onBack()
                },
                buttonEnabled = true,
                info = null,
                buttonModifier = Modifier.testTag(TestTag.Onboarding.Analytics.AcceptAnalyticsButton)
            )
        }
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .wrapContentSize()
                .padding(
                    horizontal = PaddingDefaults.Medium
                )
                .padding(bottom = it.calculateBottomPadding())
                .testTag(TestTag.Onboarding.Analytics.ScreenContent)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .semantics(mergeDescendants = true) {}
                ) {
                    Text(
                        stringResource(R.string.settings_tracking_dialog_title),
                        style = AppTheme.typography.h6,
                        modifier = Modifier.padding(
                            top = PaddingDefaults.Medium,
                            bottom = PaddingDefaults.Large
                        )
                    )

                    Text(
                        stringResource(R.string.settings_tracking_dialog_text_1),
                        style = AppTheme.typography.body1,
                        modifier = Modifier.padding(bottom = PaddingDefaults.Small)
                    )

                    Text(
                        stringResource(R.string.settings_tracking_dialog_text_2),
                        style = AppTheme.typography.body1,
                        modifier = Modifier.padding(bottom = PaddingDefaults.Small)
                    )

                    Text(
                        stringResource(R.string.settings_tracking_dialog_text_3),
                        style = AppTheme.typography.body1,
                        modifier = Modifier.padding(bottom = PaddingDefaults.Medium)
                    )
                }
            }
        }
    }
}
