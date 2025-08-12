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

package de.gematik.ti.erp.app.shared.ui.screens.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall

@Requirement(
    "A_19090-01#2",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Display explanation of data processing for analytics opt-in"
)
@Composable
fun AllowAnalyticsContent(
    lazyListState: LazyListState,
    paddingValues: PaddingValues
) {
    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .wrapContentSize()
            .padding(horizontal = PaddingDefaults.Medium)
            .padding(bottom = paddingValues.calculateBottomPadding())
    ) {
        item {
            SpacerMedium()
            Text(
                stringResource(R.string.settings_tracking_dialog_title),
                style = AppTheme.typography.h6
            )
        }

        item {
            SpacerLarge()
            Text(
                stringResource(R.string.analytics_why_important_title),
                style = AppTheme.typography.subtitle1,
                color = AppTheme.colors.neutral900,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            SpacerSmall()
            Text(
                stringResource(R.string.analytics_why_important_text),
                style = AppTheme.typography.body1
            )
        }

        item {
            SpacerLarge()
            Text(
                stringResource(R.string.analytics_how_it_works_title),
                style = AppTheme.typography.subtitle1,
                color = AppTheme.colors.neutral900,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            SpacerSmall()
            Text(
                stringResource(R.string.analytics_how_it_works_intro),
                style = AppTheme.typography.body1
            )
        }

        item {
            SpacerSmall()

            BulletPointText(
                text = stringResource(R.string.analytics_bullet_usage_stats),
                modifier = Modifier.padding(bottom = PaddingDefaults.Small)
            )

            BulletPointText(
                text = stringResource(R.string.analytics_bullet_hardware_info),
                modifier = Modifier.padding(bottom = PaddingDefaults.Small)
            )

            BulletPointText(
                text = stringResource(R.string.analytics_bullet_settings),
                modifier = Modifier.padding(bottom = PaddingDefaults.Small)
            )
        }

        item {
            SpacerSmall()
            Text(
                stringResource(R.string.analytics_data_explanation),
                style = AppTheme.typography.body1
            )
        }

        item {
            SpacerLarge()
            Text(
                stringResource(R.string.analytics_can_end_title),
                style = AppTheme.typography.subtitle1,
                color = AppTheme.colors.neutral900,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            SpacerSmall()
            Text(
                stringResource(R.string.analytics_can_end_text),
                style = AppTheme.typography.body1
            )
        }
    }
}

@Composable
private fun BulletPointText(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(start = PaddingDefaults.Medium)
    ) {
        Text(
            text = stringResource(R.string.bulletpoint),
            style = AppTheme.typography.body1,
            modifier = Modifier.padding(end = PaddingDefaults.Small)
        )
        Text(
            text = text,
            style = AppTheme.typography.body1,
            modifier = Modifier.weight(1f)
        )
    }
}
