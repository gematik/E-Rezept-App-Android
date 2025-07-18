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

@file:Suppress("FunctionName")

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults

fun LazyListScope.FavouritePharmacies(
    modifier: Modifier = Modifier,
    pharmacies: List<OverviewPharmacyData.OverviewPharmacy>,
    onClickPharmacy: (OverviewPharmacyData.OverviewPharmacy) -> Unit
) {
    item {
        Box(
            modifier = modifier
        ) {
            Text(
                text = stringResource(R.string.pharmacy_my_pharmacies_header),
                style = AppTheme.typography.subtitle1,
                modifier = Modifier
                    .padding(vertical = PaddingDefaults.Medium)
                    .semanticsHeading(),
                textAlign = TextAlign.Start
            )
        }
    }
    items(pharmacies) {
        FavoritePharmacyCard(
            modifier = modifier,
            overviewPharmacy = it,
            onClickPharmacy = onClickPharmacy
        )
    }
}
