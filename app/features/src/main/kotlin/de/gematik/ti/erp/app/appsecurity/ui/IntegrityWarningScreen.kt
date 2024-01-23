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

package de.gematik.ti.erp.app.appsecurity.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.appsecurity.navigation.AppSecurityRoutes
import de.gematik.ti.erp.app.appsecurity.presentation.rememberIntegrityWarningController
import de.gematik.ti.erp.app.appsecurity.ui.model.AppSecurityResult
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.fromNavigationString
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.BottomAppBar
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.Toggle
import io.github.aakira.napier.Napier
import java.util.Locale

@Requirement(
    "O.Arch_6#3",
    "O.Resi_2#3",
    "O.Resi_3#3",
    "O.Resi_4#3",
    "O.Resi_5#3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Show integrity warning."
)
@Requirement(
    "A_21574",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "Show integrity warning."
)
class IntegrityWarningScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    val onBack: (Boolean) -> Unit
) : Screen() {
    @Composable
    override fun Content() {
        val result =
            remember {
                navBackStackEntry.arguments?.getString(AppSecurityRoutes.IntegrityWarningScreenArgument) ?: ""
            }

        val appSecurityResult = fromNavigationString<AppSecurityResult>(result)

        Napier.d { "app security result $appSecurityResult" }
        var checked by rememberSaveable { mutableStateOf(false) }
        val scrollState = rememberScrollState()
        val integrityWarningController = rememberIntegrityWarningController()

        AnimatedElevationScaffold(
            elevated = scrollState.value > 0,
            navigationMode = NavigationBarMode.Close,
            bottomBar = {
                BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            if (checked) {
                                integrityWarningController.acceptRiskPermanent()
                            } else {
                                integrityWarningController.acceptRiskForSession()
                            }
                            onBack(appSecurityResult.isDeviceSecure)
                        },
                        shape = RoundedCornerShape(PaddingDefaults.Small)
                    ) {
                        Text(stringResource(R.string.ok).uppercase(Locale.getDefault()))
                    }
                    SpacerMedium()
                }
            },
            actions = {},
            topBarTitle = stringResource(id = R.string.insecure_device_title_safetynet),
            onBack = {
                onBack(appSecurityResult.isDeviceSecure)
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(PaddingDefaults.Medium)
            ) {
                Image(
                    painterResource(id = R.drawable.laptop_woman_pink),
                    null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxSize()
                )
                SpacerSmall()
                Text(
                    stringResource(id = R.string.insecure_device_header_safetynet),
                    style = AppTheme.typography.h6
                )
                SpacerSmall()
                Text(
                    stringResource(id = R.string.insecure_device_info_safetynet),
                    style = AppTheme.typography.body1
                )
                // Todo wait for new uri from security staff
//            val uriHandler = LocalUriHandler.current
//            SpacerMedium()
//            Text(
//                stringResource(R.string.insecure_device_safetynet_more_info),
//                style = AppTheme.typography.body2,
//                color = AppTheme.colors.neutral600
//            )
//            SpacerSmall()
//            val link = stringResource(R.string.insecure_device_safetynet_link)
//            TextButton(
//                modifier = Modifier.align(Alignment.End),
//                onClick = { uriHandler.openUri(link) }
//            ) {
//                Text(
//                    stringResource(id = R.string.insecure_device_safetynet_link_text),
//                    style = AppTheme.typography.body2,
//                    color = AppTheme.colors.primary600
//                )
//            }
                Spacer(modifier = Modifier.height(PaddingDefaults.XXLarge))
                Toggle(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    description = stringResource(id = R.string.insecure_device_accept_safetynet)
                )
            }
        }
    }
}
