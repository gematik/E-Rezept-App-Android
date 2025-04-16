/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.features.R

@Composable
fun CameraTopBar(
    flashEnabled: Boolean,
    onClickClose: () -> Unit,
    onFlashClick: (Boolean) -> Unit
) {
    Surface(
        color = Color.Unspecified,
        contentColor = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            val accCancel = stringResource(R.string.cam_acc_cancel)
            val accTorch = stringResource(R.string.cam_acc_torch)

            IconButton(
                onClick = { onClickClose() },
                modifier = Modifier
                    .testTag("camera/closeButton")
                    .semantics { contentDescription = accCancel }
            ) {
                Icon(Icons.Rounded.Close, null, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            IconToggleButton(
                checked = flashEnabled,
                onCheckedChange = onFlashClick,
                modifier = Modifier
                    .testTag("camera/flashToggle")
                    .semantics { contentDescription = accTorch }
            ) {
                val ic = if (flashEnabled) {
                    Icons.Rounded.FlashOn
                } else {
                    Icons.Rounded.FlashOff
                }
                Icon(ic, null, modifier = Modifier.size(24.dp))
            }
        }
    }
}
