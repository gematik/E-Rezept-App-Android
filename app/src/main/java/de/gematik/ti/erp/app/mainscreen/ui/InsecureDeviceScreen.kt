/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.core.MainViewModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import java.util.Locale

@Composable
fun InsecureDeviceScreen(navController: NavController, mainViewModel: MainViewModel) {
    var checked by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Close,
                headline = stringResource(R.string.insecure_device_title),
            ) { navController.popBackStack() }
        },
        bottomBar = {
            BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        if (checked) {
                            mainViewModel.onAcceptInsecureDevice()
                        }
                        navController.popBackStack()
                    },
                    shape = RoundedCornerShape(PaddingDefaults.Small)
                ) {
                    if (checked) {
                        Text(stringResource(R.string.understand).uppercase(Locale.getDefault()))
                    } else {
                        Text(stringResource(R.string.ok).uppercase(Locale.getDefault()))
                    }
                }
                SpacerMedium()
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(PaddingDefaults.Medium)
        ) {
            Image(
                painterResource(R.drawable.laptop_woman_yellow),
                null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxSize()
            )
            SpacerSmall()
            Text(
                stringResource(R.string.insecure_device_header),
                style = MaterialTheme.typography.h6
            )
            SpacerSmall()
            Text(
                stringResource(R.string.insecure_device_info),
                style = MaterialTheme.typography.body1
            )
            Spacer(modifier = Modifier.height(PaddingDefaults.XXLarge))
            Toggle(
                checked = checked,
                onCheckedChange = { checked = it }
            )
        }
    }
}

@Composable
private fun Toggle(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            )
            .background(color = AppTheme.colors.neutral100, shape = RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .padding(PaddingDefaults.Medium)
            .semantics(true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.insecure_device_accept),
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.weight(1f)
        )
        SpacerSmall()
        Switch(
            checked = checked,
            onCheckedChange = null,
        )
    }
}
