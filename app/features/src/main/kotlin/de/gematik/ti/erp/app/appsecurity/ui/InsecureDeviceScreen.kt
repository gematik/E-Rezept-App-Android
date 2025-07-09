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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.appsecurity.presentation.rememberInsecureDeviceController
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.onboarding.navigation.finishOnboardingAsSuccessAndOpenPrescriptions
import de.gematik.ti.erp.app.onboarding.presentation.rememberOnboardingController
import de.gematik.ti.erp.app.onboarding.ui.SkipOnBoardingButton
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.BottomAppBar
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.Toggle
import de.gematik.ti.erp.app.utils.compose.preview.BooleanPreviewParameterProvider
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import java.util.Locale

@Requirement(
    "O.Plat_1#4",
    "O.Resi_1#5",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Insecure Devices warning screen that is shown to the user to make a informed decision."
)
class InsecureDeviceScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    val onBack: () -> Unit
) : Screen() {
    @Composable
    override fun Content() {
        var checked by rememberSaveable { mutableStateOf(false) }
        val listState = rememberLazyListState()
        val insecureDeviceController = rememberInsecureDeviceController()
        // used here only for skip button
        val onboardingController = rememberOnboardingController()
        AnimatedElevationScaffold(
            listState = listState,
            navigationMode = NavigationBarMode.Close,
            bottomBar = {
                BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            if (checked) {
                                insecureDeviceController.acceptRiskPermanent()
                            } else {
                                insecureDeviceController.acceptRiskForSession()
                            }
                            onBack()
                        },
                        shape = RoundedCornerShape(PaddingDefaults.Small)
                    ) {
                        Text(stringResource(R.string.ok).uppercase(Locale.getDefault()))
                    }
                    SpacerMedium()
                }
            },
            actions = {},
            topBarTitle = stringResource(id = R.string.insecure_device_title),
            onBack = onBack
        ) { innerPadding ->
            InsecureDeviceScreenContent(listState, innerPadding, { checked = it }, checked)
        }

        if (BuildConfigExtension.isInternalDebug) {
            SkipOnBoardingButton {
                onboardingController.createProfileOnSkipOnboarding()
                navController.finishOnboardingAsSuccessAndOpenPrescriptions()
            }
        }
    }
}

@Composable
private fun InsecureDeviceScreenContent(
    listState: LazyListState,
    innerPadding: PaddingValues,
    onToggle: (Boolean) -> Unit,
    checked: Boolean
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(PaddingDefaults.Medium)
    ) {
        item {
            Image(
                painterResource(id = R.drawable.laptop_woman_yellow),
                null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxSize()
            )
            SpacerSmall()
        }
        item {
            Text(
                stringResource(id = R.string.insecure_device_header),
                style = AppTheme.typography.h6
            )
            SpacerSmall()
        }
        item {
            Text(
                stringResource(id = R.string.insecure_device_info),
                style = AppTheme.typography.body1
            )
            Spacer(modifier = Modifier.height(PaddingDefaults.XXLarge))
        }
        item {
            Toggle(
                checked = checked,
                onCheckedChange = { onToggle(it) },
                description = stringResource(id = R.string.insecure_device_accept)
            )
        }
    }
}

@LightDarkPreview
@Composable
fun InsecureDeviceScreenPreview(@PreviewParameter(BooleanPreviewParameterProvider::class) checked: Boolean) {
    val lazyListState = rememberLazyListState()
    PreviewAppTheme {
        InsecureDeviceScreenContent(lazyListState, PaddingValues(PaddingDefaults.Medium), { }, checked)
    }
}
