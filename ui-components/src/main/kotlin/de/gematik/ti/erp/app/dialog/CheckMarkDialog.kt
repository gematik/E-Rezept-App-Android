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

package de.gematik.ti.erp.app.dialog

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import kotlinx.coroutines.delay

/**
 * Displays a floating dialog containing a bouncing checkmark icon.
 *
 * <p>This dialog:
 * 1. Animates the checkmark from 0→1 scale with a slight overshoot (back-easing).
 * 2. Pauses briefly at full size.
 * 3. Calls [onAnimationEnd] once the animation and pause complete.
 *
 * The dialog cannot be dismissed by back-press or outside-click.
 *
 * @param onAnimationEnd Lambda invoked once the checkmark animation and delay have finished.
 *                       Typically used to dismiss the dialog and trigger any follow-up action.
 */
@Suppress("MagicNumber")
@Composable
fun CheckMarkDialog(
    onAnimationEnd: () -> Unit
) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animate scale from 0f to 1f with EaseOutBack (springy overshoot)
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 600,
                easing = EaseOutBack
            )
        )

        // Hold full size briefly before ending
        delay(400)
        onAnimationEnd()
    }

    Dialog(
        onDismissRequest = onAnimationEnd,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier.size(SizeDefaults.fivefold),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = AppTheme.colors.green400,
                modifier = Modifier
                    .size(SizeDefaults.fivefold)
                    .scale(scale.value)
            )
        }
    }
}
