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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall

internal fun LazyListScope.PharmacySearchButton(
    modifier: Modifier = Modifier,
    onStartSearch: () -> Unit
) {
    item {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(SizeDefaults.double))
                .background(color = AppTheme.colors.neutral100, shape = RoundedCornerShape(SizeDefaults.double))
                .clickable(role = Role.Button) { onStartSearch() }
                .padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.ShortMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Search,
                tint = AppTheme.colors.neutral600,
                contentDescription = null
            )
            SpacerSmall()
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.pharmacy_start_search_text),
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.body1,
                color = AppTheme.colors.neutral600
            )
        }
    }
}
