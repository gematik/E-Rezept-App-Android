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
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.settings.ui.rememberSettingsController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.BottomAppBar
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import kotlinx.coroutines.launch
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
@Composable
fun InsecureDeviceScreen(
    headline: String,
    icon: Painter,
    headlineBody: String,
    infoText: String,
    toggleDescription: String,
    pinUseCase: Boolean = true,
    onBack: () -> Unit
) {
    var checked by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val settingsController = rememberSettingsController()

    AnimatedElevationScaffold(
        elevated = scrollState.value > 0,
        navigationMode = NavigationBarMode.Close,
        bottomBar = {
            BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        if (checked) {
                            scope.launch {
                                settingsController.onAcceptInsecureDevice()
                            }
                        }
                        onBack()
                    },
                    shape = RoundedCornerShape(PaddingDefaults.Small),
                    enabled = if (pinUseCase) true else checked
                ) {
                    if (checked && pinUseCase) {
                        Text(stringResource(R.string.understand).uppercase(Locale.getDefault()))
                    } else {
                        Text(stringResource(R.string.ok).uppercase(Locale.getDefault()))
                    }
                }
                SpacerMedium()
            }
        },
        actions = {},
        topBarTitle = headline,
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
                icon,
                null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxSize()
            )
            SpacerSmall()
            Text(
                headlineBody,
                style = AppTheme.typography.h6
            )
            SpacerSmall()
            Text(
                infoText,
                style = AppTheme.typography.body1
            )
            if (!pinUseCase) {
                val uriHandler = LocalUriHandler.current
                SpacerMedium()
                Text(
                    stringResource(R.string.insecure_device_safetynet_more_info),
                    style = AppTheme.typography.body2,
                    color = AppTheme.colors.neutral600
                )
                SpacerSmall()
                val link = stringResource(R.string.insecure_device_safetynet_link)
                TextButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = { uriHandler.openUri(link) }
                ) {
                    Text(
                        stringResource(id = R.string.insecure_device_safetynet_link_text),
                        style = AppTheme.typography.body2,
                        color = AppTheme.colors.primary600
                    )
                }
            }
            Spacer(modifier = Modifier.height(PaddingDefaults.XXLarge))
            Toggle(
                checked = checked,
                onCheckedChange = { checked = it },
                description = toggleDescription
            )
        }
    }
}

@Composable
private fun Toggle(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            description,
            style = AppTheme.typography.subtitle1,
            modifier = Modifier.weight(1f)
        )
        SpacerSmall()
        Switch(
            checked = checked,
            onCheckedChange = null
        )
    }
}
