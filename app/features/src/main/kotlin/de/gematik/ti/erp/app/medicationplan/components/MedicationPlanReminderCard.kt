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

package de.gematik.ti.erp.app.medicationplan.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny

@Composable
fun MedicationPlanReminderCard(
    modifier: Modifier = Modifier,
    @DrawableRes imageResource: Int,
    title: String,
    description: String
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium),
        colors = CardDefaults.cardColors().copy(containerColor = AppTheme.colors.neutral000),
        border = BorderStroke(width = 1.dp, color = AppTheme.colors.neutral300)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingDefaults.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DayImage(imageResource = imageResource)
            SpacerMedium()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTheme.typography.subtitle1,
                    color = AppTheme.colors.neutral900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                SpacerTiny()
                Text(
                    text = description,
                    style = AppTheme.typography.body2,
                    color = AppTheme.colors.neutral600,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DayImage(
    imageResource: Int
) {
    Image(
        painter = painterResource(id = imageResource),
        contentDescription = null,
        modifier = Modifier
            .size(SizeDefaults.sixfold)
            .clip(CircleShape)
    )
}
