/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.ErezeptText
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PreviewAppTheme
import kotlinx.datetime.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritePharmacyCard(
    modifier: Modifier = Modifier,
    overviewPharmacy: OverviewPharmacyData.OverviewPharmacy,
    onClickPharmacy: (OverviewPharmacyData.OverviewPharmacy) -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(SizeDefaults.double)),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.neutral000
        ),
        shape = RoundedCornerShape(SizeDefaults.double),
        border = BorderStroke(1.dp, AppTheme.colors.neutral300),
        onClick = { onClickPharmacy(overviewPharmacy) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            PharmacyImagePlaceholder(Modifier.padding(PaddingDefaults.Medium))
            Column(
                modifier = Modifier
                    .padding(
                        end = PaddingDefaults.Medium,
                        top = PaddingDefaults.Medium,
                        bottom = PaddingDefaults.Medium
                    )
                    .weight(1f)
            ) {
                ErezeptText.SubtitleOne(text = overviewPharmacy.pharmacyName)
                ErezeptText.Body(
                    text = overviewPharmacy.address,
                    maxLines = 1
                )
            }
            AnimatedVisibility(overviewPharmacy.isFavorite) {
                Icon(
                    Icons.Rounded.Star,
                    contentDescription = "favourite star",
                    modifier = Modifier
                        .padding(end = PaddingDefaults.Medium)
                        .size(SizeDefaults.triple),
                    tint = AppTheme.colors.yellow500
                )
            }
        }
    }
}

@Composable
private fun PharmacyImagePlaceholder(modifier: Modifier) {
    Image(
        painterResource(R.drawable.ic_green_cross),
        null,
        modifier = modifier
            .clip(RoundedCornerShape(SizeDefaults.half))
            .size(SizeDefaults.eightfold)
    )
}

@LightDarkPreview
@Composable
fun FavoritePharmacyCardPreview() {
    val time = Instant.parse("2022-01-01T00:00:00Z")
    PreviewAppTheme {
        FavoritePharmacyCard(
            overviewPharmacy = OverviewPharmacyData.OverviewPharmacy(
                lastUsed = time,
                isFavorite = true,
                usageCount = 1,
                telematikId = "123456789",
                pharmacyName = "Berlin Apotheke",
                address = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
                    "sed diam nonumy eirmod tempor invidunt ut"
            ),
            onClickPharmacy = {}
        )
    }
}
