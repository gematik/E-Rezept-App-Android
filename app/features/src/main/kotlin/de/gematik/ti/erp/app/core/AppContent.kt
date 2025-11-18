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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.core

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.analytics.CardCommunicationAnalytics
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.base.dialog.observer.LocalDialogVisibilityObserver
import de.gematik.ti.erp.app.base.dialog.provider.DialogVisibilityProviderHolder
import de.gematik.ti.erp.app.base.falseStateFlow
import de.gematik.ti.erp.app.demomode.DemoModeIntent
import de.gematik.ti.erp.app.demomode.startAppWithNormalMode
import de.gematik.ti.erp.app.demomode.ui.DemoModeStatusBar
import de.gematik.ti.erp.app.demomode.ui.checkForDemoMode
import de.gematik.ti.erp.app.settings.presentation.rememberSettingsController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

val LocalCardCommunicationAnalytics =
    staticCompositionLocalOf<CardCommunicationAnalytics> { error("No Analytics provided!") }

@Composable
fun AppContent(
    content: @Composable () -> Unit
) {
    val settingsController = rememberSettingsController()
    val zoomState by settingsController.zoomState.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalDialogVisibilityObserver provides DialogVisibilityProviderHolder.provider
    ) {
        AppTheme {
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = MaterialTheme.colors.isLight
            val activity = LocalActivity.current as? BaseActivity
            val isZoomDisabledTemporarily by activity?.disableZoomTemporarily?.collectAsState() ?: falseStateFlow

            val isDialogVisible by LocalDialogVisibilityObserver.current.isVisible().collectAsState()

            SideEffect {
                systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
            }
            checkForDemoMode(
                demoModeStatusBarColor = AppTheme.colors.yellow500,
                demoModeContent = {
                    DemoModeStatusBar(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = AppTheme.colors.yellow500,
                        textColor = AppTheme.colors.neutral900,
                        demoModeActiveText = stringResource(R.string.demo_mode_text),
                        demoModeEndText = stringResource(R.string.demo_mode_cancel_button_text),
                        onClickDemoModeEnd = { activity?.let { DemoModeIntent.startAppWithNormalMode<MainActivity>(it) } }
                    )
                },
                appContent = {
                    Box(
                        modifier = Modifier
                            .switchBlur(isDialogVisible)
                            .zoomable(enabled = zoomState.zoomEnabled && !isZoomDisabledTemporarily)
                    ) {
                        content()
                    }
                }
            )
        }
    }
}

@Suppress("MagicNumber", "UnusedPrivateMember")
private fun Modifier.switchBlur(isBlurToBeEnabled: Boolean) = this.then(
    Modifier
        .blur(if (isBlurToBeEnabled) SizeDefaults.quarter else SizeDefaults.zero)
        .background(if (isBlurToBeEnabled) Color.Gray.copy(alpha = 0.5f) else Color.Transparent)
)

private fun Modifier.zoomable(
    minZoom: Float = 1f,
    maxZoom: Float = 3.5f,
    delayInMillis: Long = 1500,
    enabled: Boolean = true
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "zoomable"
        value = enabled
        properties["minZoom"] = minZoom
        properties["maxZoom"] = maxZoom
    }
) {
    require(minZoom >= 1f) { "Minimal zoom can't be smaller than 1" }

    val scale = remember { Animatable(minZoom) }
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var size by remember { mutableStateOf(Size.Zero) }

    LaunchedEffect(enabled) {
        if (!enabled) {
            launch { scale.animateTo(minZoom) }
            launch { offset.animateTo(Offset.Zero) }
        }
    }

    this
        .graphicsLayer(
            scaleX = scale.value,
            scaleY = scale.value,
            translationX = offset.value.x,
            translationY = offset.value.y
        )
        .pointerInput(enabled) {
            if (!enabled) {
                return@pointerInput
            }

            val decay = splineBasedDecay<Offset>(this)
            var job: Job? = null

            coroutineScope {
                forEachGesture {
                    val velocityTracker = VelocityTracker()

                    awaitPointerEventScope {
                        awaitFirstDown(requireUnconsumed = false)

                        do {
                            val event =
                                awaitPointerEvent(PointerEventPass.Initial)
                            val zoomChange = event.calculateZoom()
                            val offsetChange = event.calculatePan()

                            val newScale =
                                min(
                                    maxZoom,
                                    max(minZoom, scale.value * zoomChange)
                                )
                            val newOffset = Offset(
                                x = offset.value.x + offsetChange.x * scale.value,
                                y = offset.value.y + offsetChange.y * scale.value
                            )

                            val bound = Offset(
                                (size.width - size.width * scale.value) / 2,
                                (size.height - size.height * scale.value) / 2
                            )
                            offset.updateBounds(
                                lowerBound = bound,
                                upperBound = bound * -1f
                            )

                            velocityTracker.addPosition(
                                event.changes.first().uptimeMillis,
                                newOffset
                            )

                            launch {
                                scale.snapTo(newScale)
                                offset.snapTo(newOffset)
                            }

                            event.calculateCentroidSize()

                            if (scale.value > 1f) {
                                event.changes.forEach { it.consumePositionChange() }
                            }
                        } while (event.changes.any { it.pressed })
                    }

                    val velocity = velocityTracker.calculateVelocity()
                    launch {
                        offset.animateDecay(
                            Offset(
                                velocity.x,
                                velocity.y
                            ),
                            decay
                        )
                    }

                    job?.cancelAndJoin()
                    job = launch {
                        delay(delayInMillis)
                        launch { scale.animateTo(minZoom) }
                        launch { offset.animateTo(Offset.Zero) }
                    }
                }
            }
        }
        .onSizeChanged {
            size = it.toSize()
        }
}
