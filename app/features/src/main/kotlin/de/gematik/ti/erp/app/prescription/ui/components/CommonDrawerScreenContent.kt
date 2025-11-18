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

package de.gematik.ti.erp.app.prescription.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.PrimaryOutlinedButton
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Suppress("ModifierParameter")
@Composable
internal fun DefaultDrawerScreenContent(
    modifierPrimaryButton: Modifier = Modifier,
    modifierOutlinedButton: Modifier = Modifier,
    header: String,
    info: String,
    image: Painter,
    primaryButtonText: String,
    outlinedButtonText: String,
    onClickPrimary: () -> Unit,
    onClickOutlined: () -> Unit,
    bottomLinkText: String? = null,
    onClickBottomLink: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
    ) {
        Image(
            painter = image,
            contentDescription = null
        )
        Text(
            modifier = Modifier.padding(top = PaddingDefaults.Medium),
            text = header,
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.padding(bottom = PaddingDefaults.Medium),
            text = info,
            style = AppTheme.typography.body2l,
            textAlign = TextAlign.Center
        )

        PrimaryButton(
            modifier = modifierPrimaryButton
                .fillMaxWidth(),
            onClick = onClickPrimary,
            shape = RoundedCornerShape(SizeDefaults.triple),
            contentPadding = PaddingValues(
                vertical = PaddingDefaults.MediumSmall,
                horizontal = PaddingDefaults.XXLargePlus
            )
        ) {
            Text(
                text = primaryButtonText
            )
        }
        PrimaryOutlinedButton(
            onClick = onClickOutlined,
            modifier = modifierOutlinedButton
                .fillMaxWidth()
        ) {
            Text(
                text = outlinedButtonText
            )
        }

        bottomLinkText?.let { text ->
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { onClickBottomLink.invoke() }
                    .padding(top = PaddingDefaults.Medium)
            ) {
                Text(
                    text = text,
                    style = AppTheme.typography.subtitle1l,
                    color = AppTheme.colors.primary700
                )

                SpacerSmall()

                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = AppTheme.colors.primary700,
                    modifier = Modifier.size(SizeDefaults.triple)
                )
            }
            SpacerMedium()
        }
    }
}

@LightDarkPreview
@Composable
internal fun CommonDrawerScreenContentPreview() {
    PreviewAppTheme {
        DefaultDrawerScreenContent(
            header = "Header",
            info = "Info",
            image = painterResource(R.drawable.man_phone_blue_circle),
            bottomLinkText = stringResource(R.string.cardwall_select_insurance_federal_link),
            primaryButtonText = "Connect",
            outlinedButtonText = "Cancel",
            onClickPrimary = {},
            onClickOutlined = {},
            onClickBottomLink = {}
        )
    }
}
