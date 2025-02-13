/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.prescription.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Suppress("ModifierParameter")
@Composable
internal fun CommonDrawerScreenContent(
    modifierPrimary: Modifier = Modifier,
    modifierText: Modifier = Modifier,
    header: String,
    info: String,
    image: Painter,
    connectButtonText: String,
    cancelButtonText: String,
    onClickConnect: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpacerSmall()
        Image(
            painter = image,
            contentDescription = null
        )
        Text(
            text = header,
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center
        )
        SpacerSmall()
        Text(
            text = info,
            style = AppTheme.typography.body2l,
            textAlign = TextAlign.Center
        )
        SpacerLarge()
        PrimaryButton(
            modifier = modifierPrimary,
            onClick = onClickConnect,
            colors = ButtonDefaults.buttonColors(
                contentColor = AppTheme.colors.primary700
            ),
            contentPadding = PaddingValues(
                vertical = SizeDefaults.oneHalf,
                horizontal = SizeDefaults.sixfold
            )
        ) {
            Text(
                text = connectButtonText,
                style = AppTheme.typography.body2,
                color = AppTheme.colors.neutral050
            )
        }
        SpacerMedium()
        TextButton(
            onClick = onCancel,
            modifier = modifierText,
            contentPadding = PaddingValues(vertical = SizeDefaults.oneHalf)
        ) {
            Text(cancelButtonText)
        }
    }
}

@LightDarkPreview
@Composable
internal fun CommonDrawerScreenContentPreview() {
    PreviewAppTheme {
        CommonDrawerScreenContent(
            header = "Header",
            info = "Info",
            image = painterResource(R.drawable.man_phone_blue_circle),
            connectButtonText = "Connect",
            cancelButtonText = "Cancel",
            onClickConnect = {},
            onCancel = {}
        )
    }
}
