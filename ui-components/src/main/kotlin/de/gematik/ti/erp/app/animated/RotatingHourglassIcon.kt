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

package de.gematik.ti.erp.app.animated

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import de.gematik.ti.erp.app.theme.AppTheme
import kotlinx.coroutines.delay

@Composable
fun RotatingHourglassIcon() {
    var startRotation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        startRotation = true
    }

    val rotation by animateFloatAsState(
        targetValue = if (startRotation) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "Hourglass rotation"
    )

    Icon(
        imageVector = Icons.Filled.HourglassTop,
        contentDescription = null,
        tint = AppTheme.colors.yellow500,
        modifier = Modifier.rotate(rotation)
    )
}
