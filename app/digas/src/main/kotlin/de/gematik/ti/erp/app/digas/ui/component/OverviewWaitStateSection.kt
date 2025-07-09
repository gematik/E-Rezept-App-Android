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

package de.gematik.ti.erp.app.digas.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.datetime.timeStateParser
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.timestate.getTimeState
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import kotlinx.datetime.Instant

@Composable
fun OverviewWaitStateSection(
    lastRefreshedTime: Instant,
    isDownloading: Boolean,
    onRefresh: () -> Unit
) {
    val timeString = timeStateParser(
        timeState = getTimeState(lastRefreshedTime)
    )
    val rotation by rememberInfiniteTransition(label = "rotate")
        .animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing)
            ),
            label = "rotation"
        ).takeIf { isDownloading } ?: remember { mutableFloatStateOf(0f) }

    SpacerMedium()
    Column(
        Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(SizeDefaults.half))
                .clickable(
                    onClick = onRefresh,
                    onClickLabel = stringResource(R.string.refresh),
                    role = Role.Button
                )
                .padding(vertical = PaddingDefaults.Small)
                .padding(horizontal = PaddingDefaults.Large)
        ) {
            Text(
                text = stringResource(R.string.refresh),
                style = AppTheme.typography.body1,
                color = AppTheme.colors.primary700
            )
            SpacerTiny()
            Icon(
                modifier = Modifier.graphicsLayer {
                    rotationZ = rotation
                },
                imageVector = Icons.Outlined.Refresh,
                tint = AppTheme.colors.primary700,
                contentDescription = null
            )
        }
        SpacerTiny()
        Text(
            text = stringResource(
                R.string.last_updated_just_now,
                timeString
            ),
            style = AppTheme.typography.caption1,
            color = AppTheme.colors.neutral600
        )
        Text(
            text = stringResource(R.string.diga_insurance_waiting_info),
            style = AppTheme.typography.caption1,
            textAlign = TextAlign.Center,
            color = AppTheme.colors.neutral600
        )
    }
}

@Suppress("UnusedPrivateMember")
@LightDarkPreview
@Composable
private fun OverviewWaitStateSectionPreview() {
    PreviewTheme {
        OverviewWaitStateSection(
            lastRefreshedTime = Instant.parse("2025-07-01T10:00:00Z"),
            isDownloading = false
        ) {}
    }
}
