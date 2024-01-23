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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import java.util.UUID

@Composable
fun OutlinedDebugButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        border = ButtonDefaults.outlinedBorder.copy(brush = SolidColor(AppTheme.DebugColor)),
        colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.DebugColor),
        contentPadding = PaddingValues(horizontal = PaddingDefaults.Small, vertical = PaddingDefaults.Tiny),
        modifier = modifier.testTag(TestTag.Onboarding.SkipOnboardingButton)
    ) {
        Icon(Icons.Outlined.BugReport, null)
        SpacerSmall()
        Text(text)
        SpacerTiny()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.visualTestTag(tag: String) =
    composed(fullyQualifiedName = "de.gematik.ti.erp.app.utils.compose.visualTestTag", key1 = tag) {
        val activity = LocalContext.current as MainActivity
        val uuid = remember { UUID.randomUUID().toString() }

        DisposableEffect(tag) {
            onDispose {
                activity.elementsUsedInTests -= uuid
            }
        }

        Modifier
            .testTag(tag)
            .onGloballyPositioned {
                activity.elementsUsedInTests += uuid to MainActivity.ElementForTest(it.boundsInRoot(), tag)
            }
    }

@Composable
fun DebugOverlay(elements: Map<String, MainActivity.ElementForTest>) {
    Box(Modifier.fillMaxSize()) {
        elements.entries.forEach { (key, elementForTest) ->
            key(key) {
                Box(
                    Modifier
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(
                                Constraints.fixed(
                                    elementForTest.bounds.width.toInt(),
                                    elementForTest.bounds.height.toInt()
                                )
                            )
                            layout(placeable.width, placeable.height) {
                                placeable.place(elementForTest.bounds.topLeft.round())
                            }
                        }
                        .border(width = 2.dp, color = Color.Magenta, shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = elementForTest.tag,
                        color = Color.Magenta,
                        overflow = TextOverflow.Visible,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.5f))
                            .padding(start = 4.dp, end = 2.dp)
                    )
                }
            }
        }
    }
}
