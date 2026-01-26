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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlashlightOff
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
fun CameraTopBar(
    modifier: Modifier = Modifier,
    flashEnabled: Boolean,
    onClickClose: () -> Unit,
    onFlashClick: () -> Unit
) {
    Surface(
        color = Color.Unspecified,
        contentColor = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = modifier.padding(PaddingDefaults.Medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            CircularIconButton(
                onClick = onClickClose,
                icon = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.health_card_order_close)
            )

            FlashlightToggleButton(
                isFlashlightOn = flashEnabled,
                onToggle = onFlashClick
            )
        }
    }
}

@Composable
private fun CircularIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(SizeDefaults.sixfold)
            .background(
                color = AppTheme.colors.neutral050,
                shape = CircleShape
            )
            .semantics {
                traversalIndex = 1f
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = AppTheme.colors.neutral700,
            modifier = Modifier.size(SizeDefaults.triple)
        )
    }
}

@Composable
private fun FlashlightToggleButton(
    isFlashlightOn: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedIconButton(
        onClick = onToggle,
        imageVector = if (isFlashlightOn) Icons.Outlined.FlashlightOff else Icons.Outlined.FlashlightOn,
        contentDescription = null,
        text = stringResource(
            if (isFlashlightOn) {
                R.string.cdw_scanner_flashlight_off
            } else {
                R.string.cdw_scanner_flashlight_on
            }
        ),
        border = BorderStroke(1.dp, AppTheme.colors.primary700),
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = AppTheme.colors.neutral000,
            contentColor = AppTheme.colors.primary700
        ),
        shape = RoundedCornerShape(SizeDefaults.triple),
        modifier = modifier.semantics {
            traversalIndex = 2f
        }
    )
}
