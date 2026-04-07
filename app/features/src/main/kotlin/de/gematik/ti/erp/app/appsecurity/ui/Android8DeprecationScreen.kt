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

package de.gematik.ti.erp.app.appsecurity.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.appsecurity.navigation.AppSecurityRoutes
import de.gematik.ti.erp.app.appsecurity.navigation.AppSecurityRoutes.DeviceCheckLoadingScreen
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.icon.HintIcon
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.navigateAndClearStack
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

class Android8DeprecationScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        val nextRoute = navBackStackEntry.arguments?.getString(AppSecurityRoutes.ANDROID8_NEXT_ROUTE)
        Android8DeprecationScreenContent(
            onContinueClick = {
                if (nextRoute != null) {
                    navController.navigateAndClearStack(nextRoute)
                } else {
                    navController.navigate(DeviceCheckLoadingScreen.path())
                }
            }
        )
    }
}

@Composable
private fun Android8DeprecationScreenContent(onContinueClick: () -> Unit = {}) {
    val listState = rememberLazyListState()
    val continueButtonText = stringResource(R.string.android8_deprecation_continue_button)
    AnimatedElevationScaffold(
        listState = listState,
        navigationMode = null,
        backLabel = "",
        closeLabel = "",
        topBarTitle = "",
        actions = {},
        onBack = {},
        bottomBar = {
            Button(
                onClick = onContinueClick,
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primary700),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SizeDefaults.double)
                    .padding(horizontal = SizeDefaults.sevenfold)
                    .semantics {
                        contentDescription = continueButtonText
                    }
            ) {
                Text(
                    text = continueButtonText,
                    color = AppTheme.colors.neutral000
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = SizeDefaults.double),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(SizeDefaults.sevenfold))

            // Decorative icon — hidden from accessibility tree
            HintIcon(
                painter = painterResource(R.drawable.information),
                contentDescription = null,
                modifier = Modifier
                    .size(SizeDefaults.fifteenfold)
                    .semantics { contentDescription = "" }
            )

            Spacer(modifier = Modifier.height(SizeDefaults.fourfold))

            // Screen title marked as heading for TalkBack navigation
            Text(
                text = stringResource(R.string.android8_deprecation_title),
                color = AppTheme.colors.neutral900,
                style = AppTheme.typography.h5,
                textAlign = TextAlign.Start,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(SizeDefaults.one))

            Text(
                text = stringResource(R.string.android8_deprecation_subtitle),
                color = AppTheme.colors.neutral900,
                style = AppTheme.typography.body2,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(SizeDefaults.fourfold))

            Column(modifier = Modifier.fillMaxWidth()) {
                // Section header marked as heading
                Text(
                    text = stringResource(R.string.android8_deprecation_section_header),
                    style = AppTheme.typography.subtitle1,
                    color = AppTheme.colors.neutral900,
                    modifier = Modifier.semantics { heading() }
                )

                Spacer(modifier = Modifier.height(SizeDefaults.double))

                ReasonItem(text = stringResource(R.string.android8_deprecation_reason_app_still_works))
                Spacer(modifier = Modifier.height(SizeDefaults.one))
                ReasonItem(text = stringResource(R.string.android8_deprecation_reason_no_updates))
                Spacer(modifier = Modifier.height(SizeDefaults.one))
                ReasonItem(text = stringResource(R.string.android8_deprecation_reason_upgrade_recommendation))
            }
        }
    }
}

@Composable
private fun ReasonItem(text: String) {
    // Merge icon + text into one semantics node so TalkBack reads them as a single item
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.semantics(mergeDescendants = true) {}
    ) {
        Image(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null, // decorative; text carries the meaning
            colorFilter = ColorFilter.tint(AppTheme.colors.green600)
        )
        Spacer(modifier = Modifier.width(SizeDefaults.double))
        Text(
            text = text,
            style = AppTheme.typography.body2,
            color = AppTheme.colors.neutral900
        )
    }
}

@LightDarkPreview
@Composable
fun Android8DeprecationScreenPreview() {
    PreviewAppTheme {
        Android8DeprecationScreenContent()
    }
}
