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

package de.gematik.ti.erp.app.utils.extensions

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import de.gematik.ti.erp.app.navigation.oneSecondInfiniteTween
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val LayoutDelay = 330L

/**
 * https://google.github.io/accompanist/insets/
 */
fun Modifier.navigationBarsWithImePadding() = this
    .navigationBarsPadding()
    .imePadding()

/**
 * When the keyboard is open it provides the exact height of the keyboard in Dp
 * or 0 Dp when the keyboard is closed
 */
@Composable
fun rememberImeHeight(): Dp {
    val density = LocalDensity.current
    val insets = WindowInsets.ime
    return remember(insets.getBottom(density)) {
        with(density) { insets.getBottom(density).toDp() }
    }
}

@Composable
fun Modifier.imeHeight(): Modifier {
    val density = LocalDensity.current
    val insets = WindowInsets.ime
    val height = remember(insets.getBottom(density)) {
        with(density) { insets.getBottom(density).toDp() }
    }
    return this.padding(bottom = height)
}

@Composable
fun paddingKeyboardHeight(): PaddingValues = WindowInsets.ime.asPaddingValues(LocalDensity.current)

@OptIn(ExperimentalLayoutApi::class)
fun Modifier.animateScrollOnFocus(to: Int, listState: LazyListState, offset: Int = 0) = composed {
    val coroutineScope = rememberCoroutineScope()
    val mutex = MutatorMutex()

    var hasFocus by remember { mutableStateOf(false) }
    val keyboardVisible = WindowInsets.isImeVisible

    LaunchedEffect(hasFocus, keyboardVisible) {
        if (hasFocus && keyboardVisible) {
            mutex.mutate {
                delay(LayoutDelay)
                listState.animateScrollToItem(to, offset)
            }
        }
    }

    onFocusChanged {
        if (it.hasFocus) {
            hasFocus = true
            coroutineScope.launch {
                mutex.mutate(MutatePriority.UserInput) {
                    delay(LayoutDelay)
                    listState.animateScrollToItem(to, offset)
                }
            }
        } else {
            hasFocus = false
        }
    }
}

@Composable
fun Modifier.greyCircularBorder(): Modifier = this.then(
    Modifier.border(SizeDefaults.eighth, AppTheme.colors.neutral300, CircleShape)
)

@Composable
fun Modifier.circularBorder(
    color: Color,
    width: Dp = SizeDefaults.eighth
): Modifier = this.then(
    Modifier.border(
        color = color,
        width = width,
        shape = CircleShape
    )
)

@Composable
fun Modifier.animatedCircularBorder(
    animate: Boolean,
    startColor: Color = AppTheme.colors.neutral400,
    endColor: Color = AppTheme.colors.neutral600
): Modifier {
    return if (animate) {
        val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
        val width by infiniteTransition.animateValue(
            initialValue = SizeDefaults.zero,
            targetValue = SizeDefaults.half,
            typeConverter = Dp.VectorConverter,
            animationSpec = oneSecondInfiniteTween(),
            label = "width"
        )
        val color by infiniteTransition.animateColor(
            initialValue = startColor,
            targetValue = endColor,
            animationSpec = oneSecondInfiniteTween(),
            label = "color"
        )
        circularBorder(
            color = color,
            width = width
        )
    } else {
        Modifier
    }
}

fun Modifier.identifyItemOnScroll(
    windowHeightPx: Int,
    isVisible: (Boolean) -> Unit
) =
    this.then(
        Modifier.onGloballyPositioned { layoutCoordinates ->
            try {
                val itemPosition = layoutCoordinates.localToWindow(Offset.Zero)
                val itemHeight = layoutCoordinates.size.height
                isVisible((itemPosition.y + itemHeight) > 0 && itemPosition.y < windowHeightPx)
            } catch (e: Throwable) {
                isVisible(false)
            }
        }
    )

fun Modifier.disableCopyPasteFromKeyboard() = this.then(
    Modifier.onPreviewKeyEvent { keyEvent ->
        if ((keyEvent.key == Key.C && keyEvent.isCtrlPressed) ||
            (keyEvent.key == Key.X && keyEvent.isCtrlPressed) ||
            (keyEvent.key == Key.V && keyEvent.isCtrlPressed)
        ) {
            // Intercept copy, cut, and paste actions
            return@onPreviewKeyEvent true
        }
        false
    }
)
