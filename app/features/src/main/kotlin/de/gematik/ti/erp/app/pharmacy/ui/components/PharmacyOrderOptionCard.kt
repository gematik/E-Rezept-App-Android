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

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacyOrderOptionCardType.Flat
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacyOrderOptionCardType.Long
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.shortToast

private const val DISABLED_ALPHA = 0.3f
private const val ENABLED_ALPHA = 1f

enum class PharmacyOrderOptionCardType {
    Flat, Long
}

@Composable
fun RowScope.PharmacyOrderOptionCard(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isError: Boolean = false,
    isServiceEnabled: Boolean,
    text: String,
    image: Painter,
    type: PharmacyOrderOptionCardType,
    showDisabledToast: Boolean = false,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val serviceDisabledText = stringResource(R.string.connect_for_pharmacy_service)
    val accessibilityLabel = buildString {
        append(
            when {
                isSelected -> ", ${stringResource(R.string.a11y_selected)}"
                else -> ", ${stringResource(R.string.a11y_not_selected)}"
            }
        )
        if (!isServiceEnabled) append(", ${stringResource(R.string.a11y_disabled)}")
    }

    val shape = when (type) {
        Flat -> RoundedCornerShape(PaddingDefaults.MediumPlus)
        Long -> RoundedCornerShape(PaddingDefaults.Medium)
    }

    Column(
        modifier = modifier
            .weight(1f)
            .then(
                if (type == Long) {
                    Modifier.fillMaxSize(1f)
                } else {
                    Modifier.height(IntrinsicSize.Min)
                }
            )
            .shadow(elevation = SizeDefaults.half, shape = shape)
            .background(AppTheme.colors.neutral025, shape)
            .border(
                width = when {
                    isSelected || isError -> SizeDefaults.quarter
                    else -> SizeDefaults.eighth
                },
                shape = shape,
                color = when {
                    isError -> AppTheme.colors.red600
                    isSelected -> AppTheme.colors.primary700
                    else -> AppTheme.colors.neutral500
                }
            )
            .clip(shape)
            .clickable(
                role = Role.Button,
                onClick = {
                    when {
                        isServiceEnabled -> onClick()
                        showDisabledToast -> context.shortToast(serviceDisabledText)
                    }
                }
            )
            .semantics(mergeDescendants = true) { contentDescription = accessibilityLabel }
            .then(
                when (type) {
                    Flat -> Modifier.padding(PaddingDefaults.Small)
                    Long -> Modifier.padding(PaddingDefaults.Medium)
                }
            )
            .alpha(if (isServiceEnabled) ENABLED_ALPHA else DISABLED_ALPHA)
    ) {
        Image(
            painter = image,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .then(if (type == Flat) Modifier.height(SizeDefaults.sixfold) else Modifier)
        )
        SpacerTiny()
        Text(
            text,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .then(if (type == Flat) Modifier.padding(SizeDefaults.quarter) else Modifier),
            textAlign = TextAlign.Center,
            style = AppTheme.typography.subtitle2
        )
    }
}

@LightDarkPreview
@Composable
private fun OrderIconFlatPreview() {
    PreviewTheme {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
        ) {
            PharmacyOrderOptionCard(
                type = Flat,
                isSelected = false,
                isServiceEnabled = true,
                isError = false,
                image = painterResource(R.drawable.pharmacy_small),
                text = "Go2"
            ) {}
            PharmacyOrderOptionCard(
                type = Flat,
                isSelected = false,
                isServiceEnabled = true,
                isError = true,
                image = painterResource(R.drawable.delivery_car_small),
                text = "Get4m"
            ) {}
            PharmacyOrderOptionCard(
                type = Flat,
                isSelected = true,
                isServiceEnabled = true,
                isError = false,
                image = painterResource(R.drawable.truck_small),
                text = "Wait"
            ) {}
        }
    }
}

@LightDarkPreview
@Composable
private fun OrderIconLongPreview() {
    PreviewTheme {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
        ) {
            PharmacyOrderOptionCard(
                type = Long,
                isSelected = false,
                isServiceEnabled = true,
                isError = false,
                image = painterResource(R.drawable.pharmacy_small),
                text = "Go2"
            ) {}
            PharmacyOrderOptionCard(
                type = Long,
                isSelected = true,
                isServiceEnabled = true,
                isError = false,
                image = painterResource(R.drawable.delivery_car_small),
                text = "Get4m"
            ) {}
            PharmacyOrderOptionCard(
                type = Long,
                isSelected = true,
                isServiceEnabled = true,
                isError = false,
                image = painterResource(R.drawable.truck_small),
                text = "Wait"
            ) {}
        }
    }
}
