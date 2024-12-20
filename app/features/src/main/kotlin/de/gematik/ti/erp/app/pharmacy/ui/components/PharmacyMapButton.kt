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

@file:Suppress("FunctionName")

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.features.R

@Composable
internal fun PharmacyMapButton(onClick: () -> Unit) {
    val contentDescription = stringResource(id = R.string.google_maps_button)
    PrimaryButton(
        shape = RoundedCornerShape(SizeDefaults.double),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = AppTheme.colors.neutral050,
            contentColor = AppTheme.colors.primary600
        ),
        elevation = ButtonDefaults.elevation(),
        modifier = Modifier.size(SizeDefaults.sevenfold),
        onClick = onClick
    ) {
        Icon(
            Icons.Rounded.Map,
            contentDescription = contentDescription
        )
    }
}
