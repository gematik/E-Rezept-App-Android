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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.appsecurity.presentation.rememberInsecureDeviceController
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.BottomAppBar
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.Toggle
import java.util.Locale

@Requirement(
    "O.Plat_1#4",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "insecure Devices warning."
)
class InsecureDeviceScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    val onBack: () -> Unit
) : Screen() {

    @Composable
    override fun Content() {
        var checked by rememberSaveable { mutableStateOf(false) }
        val scrollState = rememberScrollState()

        val insecureDeviceController = rememberInsecureDeviceController()

        AnimatedElevationScaffold(
            elevated = scrollState.value > 0,
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(PaddingDefaults.Medium)
            ) {
                Image(
                    painterResource(id = R.drawable.laptop_woman_yellow),
                    null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxSize()
                )
                SpacerSmall()
                Text(
                    stringResource(id = R.string.insecure_device_header),
                    style = AppTheme.typography.h6
                )
                SpacerSmall()
                Text(
                    stringResource(id = R.string.insecure_device_info),
                    style = AppTheme.typography.body1
                )
                Spacer(modifier = Modifier.height(PaddingDefaults.XXLarge))
                Toggle(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    description = stringResource(id = R.string.insecure_device_accept)
                )
            }
        }
    }
}
