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

package de.gematik.ti.erp.app.utils.compose

import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.base.openSettingsAsNewActivity
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Suppress("MagicNumber")
@Composable
fun AccessToCameraDenied(
    showSettingsButton: Boolean = false,
    showTopBar: Boolean = true,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        color = Color.Black,
        contentColor = Color.White,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .testTag("camera/disallowed")
        ) {
            if (showTopBar) {
                CameraTopBar(
                    flashEnabled = false,
                    onClickClose = onClick,
                    onFlashClick = {}
                )
                Spacer(Modifier.weight(0.4f))
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PaddingDefaults.Medium),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Rounded.ErrorOutline,
                    null,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    stringResource(R.string.cam_access_denied_headline),
                    style = AppTheme.typography.h6,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    stringResource(R.string.cam_access_denied_description),
                    style = AppTheme.typography.subtitle1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                if (showSettingsButton) {
                    SpacerMedium()
                    OutlinedButton(
                        colors = erezeptButtonColors(),
                        onClick = {
                            context.openSettingsAsNewActivity(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        }
                    ) {
                        androidx.compose.material3.Text(stringResource(R.string.open_settings))
                    }
                }
            }
            Spacer(Modifier.weight(0.6f))
        }
    }
}

@LightDarkPreview
@Composable
internal fun AccessToCameraDeniedPreview() {
    PreviewAppTheme {
        AccessToCameraDenied(
            onClick = {}
        )
    }
}

@LightDarkPreview
@Composable
internal fun AccessToCameraDeniedWithButtonPreview() {
    PreviewAppTheme {
        AccessToCameraDenied(
            showSettingsButton = true,
            showTopBar = false,
            onClick = {}
        )
    }
}
