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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.DarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Suppress("FunctionNaming")
@Composable
fun RedeemFromDetailSection(onClickRedeemLocal: () -> Unit, onClickRedeemOnline: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = PaddingDefaults.Medium),
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RedeemSection(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.prescription_detail_redeem_online),
            image = painterResource(R.drawable.pharmacy_small_32),
            onClick = onClickRedeemOnline
        )

        RedeemSection(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.prescription_detail_redeem_local),
            image = painterResource(R.drawable.dm_code),
            onClick = onClickRedeemLocal
        )
    }
}

// TODO: Convert this into a component
@Suppress("FunctionNaming")
@Composable
private fun RedeemSection(modifier: Modifier = Modifier, text: String, image: Painter, onClick: () -> Unit) {
    val shape = RoundedCornerShape(SizeDefaults.oneHalf)

    Column(
        modifier = modifier
            .shadow(elevation = SizeDefaults.half, shape = shape)
            .background(AppTheme.colors.neutral050, shape) // using 050 since 025 does not show a difference from screen
            .border(
                width = SizeDefaults.quarter,
                shape = shape,
                color = AppTheme.colors.primary700
            )
            .clip(shape)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(PaddingDefaults.Small)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Image(
            image,
            null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(SizeDefaults.fourfoldAndHalf)
        )
        Text(
            text,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            style = AppTheme.typography.subtitle2,
            color = AppTheme.colors.primary700
        )
    }
}

@DarkPreview
@Composable
fun PreviewRedeemFromDetailSection() {
    PreviewAppTheme {
        RedeemFromDetailSection(
            onClickRedeemLocal = {},
            onClickRedeemOnline = {}
        )
    }
}
