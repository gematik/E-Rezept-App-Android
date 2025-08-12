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

package de.gematik.ti.erp.app.analytics.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.analytics.presentation.DebugTrackerScreenEvent
import de.gematik.ti.erp.app.analytics.presentation.DebugTrackerScreenEvent.DynamicEvent
import de.gematik.ti.erp.app.analytics.presentation.DebugTrackerScreenEvent.ScreenEvent
import de.gematik.ti.erp.app.analytics.presentation.DebugTrackerScreenViewModel
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkBigFontPreview
import de.gematik.ti.erp.app.utils.compose.Center
import de.gematik.ti.erp.app.utils.compose.ErezeptText
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigateBackButton
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import io.github.aakira.napier.Napier
import org.kodein.di.compose.rememberInstance

class DebugTrackerScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        val viewModel by rememberInstance<DebugTrackerScreenViewModel>()
        val events by viewModel.session.collectAsStateWithLifecycle()
        events.forEach {
            Napier.d { "DebugTrackerScreen---> $it" }
        }
        DebugTrackerScaffold(
            events = events,
            onBack = { navController.popBackStack() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugTrackerScaffold(
    events: List<DebugTrackerScreenEvent>,
    onBack: () -> Unit
) {
    val lazyListState = rememberLazyListState()
    AnimatedElevationScaffold(
        listState = lazyListState,
        topBar = { elevated ->
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.neutral025),
                title = {
                    AnimatedContent(
                        targetState = elevated,
                        label = "DebugTrackerScaffold",
                        transitionSpec = {
                            if (targetState) {
                                slideInVertically(initialOffsetY = { -it }) + fadeIn() togetherWith
                                    slideOutVertically(targetOffsetY = { it }) + fadeOut()
                            } else {
                                slideInVertically(initialOffsetY = { it }) + fadeIn() togetherWith
                                    slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                            }.using(SizeTransform(clip = false))
                        }
                    ) { isElevated ->
                        if (isElevated) {
                            Center {
                                Text(
                                    text = stringResource(R.string.tracked_events_title),
                                    color = AppTheme.colors.neutral800,
                                    style = AppTheme.typography.h5
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.tracked_events_title),
                                color = AppTheme.colors.neutral800,
                                style = AppTheme.typography.h6
                            )
                        }
                    }
                },
                navigationIcon = {
                    NavigateBackButton(
                        contentDescription = stringResource(R.string.back),
                        onClick = onBack
                    )
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.padding(it),
            state = lazyListState
        ) {
            itemsIndexed(events) { index, event ->
                SpacerMedium()
                when (event) {
                    is DynamicEvent -> {
                        Center {
                            Card(
                                border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral025)
                            ) {
                                Row(
                                    modifier = Modifier.padding(PaddingDefaults.Medium),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Card(
                                        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                                        backgroundColor = AppTheme.colors.primary100,
                                        border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral025)
                                    ) {
                                        ErezeptText.Body(
                                            modifier = Modifier
                                                .width(SizeDefaults.twentyfivefold)
                                                .padding(PaddingDefaults.Medium),
                                            text = event.key
                                        )
                                    }
                                    RightwardArrowLine()
                                    Card(
                                        modifier = Modifier
                                            .padding(horizontal = PaddingDefaults.Medium),
                                        backgroundColor = AppTheme.colors.primary100,
                                        border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral025)
                                    ) {
                                        ErezeptText.Body(
                                            modifier = Modifier.padding(PaddingDefaults.Small),
                                            text = event.value
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is ScreenEvent -> {
                        Center {
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = PaddingDefaults.Medium),
                                backgroundColor = AppTheme.colors.primary100,
                                border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral025)
                            ) {
                                Column(modifier = Modifier.padding(PaddingDefaults.Small)) {
                                    ErezeptText.Body(event.value)
                                }
                            }
                        }
                    }
                }

                if (index != events.size - 1) {
                    Center {
                        DownArrowLine()
                    }
                }
            }
            item { SpacerMedium() }
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun DownArrowLine(
    height: Dp = SizeDefaults.fivefold,
    lineColor: Color = AppTheme.colors.primary200
) {
    Canvas(modifier = Modifier.height(height)) {
        // Get the canvas width and height
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Draw a line starting from the top-center to the bottom-center
        drawLine(
            color = lineColor,
            strokeWidth = 5f,
            start = Offset(x = canvasWidth / 2, y = 0f),
            end = Offset(x = canvasWidth / 2, y = canvasHeight),
            cap = Stroke.DefaultCap
        )

        // Draw an arrowhead at the bottom-center
        val arrowSize = 20f
        drawLine(
            color = lineColor,
            strokeWidth = 5f,
            start = Offset(x = canvasWidth / 2 - arrowSize, y = canvasHeight - 20f), // Left side of arrow
            end = Offset(x = canvasWidth / 2, y = canvasHeight), // Tip of the arrow
            cap = Stroke.DefaultCap
        )

        drawLine(
            color = lineColor,
            strokeWidth = 5f,
            start = Offset(x = canvasWidth / 2 + arrowSize, y = canvasHeight - 20f), // Right side of arrow
            end = Offset(x = canvasWidth / 2, y = canvasHeight), // Tip of the arrow
            cap = Stroke.DefaultCap
        )
    }
}

@Suppress("MagicNumber")
@Composable
fun RightwardArrowLine(
    height: Dp = SizeDefaults.fivefold,
    lineColor: Color = AppTheme.colors.primary200
) {
    Canvas(
        modifier = Modifier.width(height)
    ) {
        // Get the canvas width and height
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Draw a horizontal line from the left-center to near the right-end
        drawLine(
            color = lineColor,
            strokeWidth = 5f,
            start = Offset(x = 0f, y = canvasHeight / 2),
            end = Offset(x = canvasWidth - 20f, y = canvasHeight / 2), // Leave space for arrowhead
            cap = Stroke.DefaultCap
        )

        // Draw the arrowhead at the end of the line (right side)
        val arrowSize = 20f
        drawLine(
            color = lineColor,
            strokeWidth = 5f,
            start = Offset(x = canvasWidth - 20f, y = canvasHeight / 2 - arrowSize / 2), // Top part of arrow
            end = Offset(x = canvasWidth, y = canvasHeight / 2), // Tip of the arrow
            cap = Stroke.DefaultCap
        )

        drawLine(
            color = lineColor,
            strokeWidth = 5f,
            start = Offset(x = canvasWidth - 20f, y = canvasHeight / 2 + arrowSize / 2), // Bottom part of arrow
            end = Offset(x = canvasWidth, y = canvasHeight / 2), // Tip of the arrow
            cap = Stroke.DefaultCap
        )
    }
}

@LightDarkPreview
@LightDarkBigFontPreview
@Composable
fun DemoTrackerScreenContentPreview() {
    PreviewAppTheme {
        DebugTrackerScaffold(
            listOf(
                ScreenEvent("Screen 1"),
                DynamicEvent("Count", "1"),
                ScreenEvent("Screen 1"),
                DynamicEvent("some long value being sent to be shared here", "3")
            )
        ) {}
    }
}
