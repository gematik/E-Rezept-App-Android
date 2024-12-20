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

package de.gematik.ti.erp.app.utils.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.SwipeableState
import androidx.compose.material.contentColorFor
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ModalBottomSheet(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SwipeableState<ModalBottomSheetValue>,
    sheetShape: Shape = MaterialTheme.shapes.large,
    sheetElevation: Dp = ModalBottomSheetDefaults.Elevation,
    sheetBackgroundColor: Color = MaterialTheme.colors.surface,
    sheetContentColor: Color = contentColorFor(sheetBackgroundColor),
    scrimColor: Color = ModalBottomSheetDefaults.scrimColor
) {
    val scope = rememberCoroutineScope()

    BackHandler(sheetState.currentValue != ModalBottomSheetValue.Hidden) {
        scope.launch {
            sheetState.animateTo(ModalBottomSheetValue.Hidden)
        }
    }

    Box(modifier) {
        val visible = if (sheetState.currentValue != ModalBottomSheetValue.Hidden) 1f else 0f
        val alpha by animateFloatAsState(visible, tween())

        Box(
            Modifier
                .fillMaxSize()
                .alpha(alpha)
                .background(scrimColor)
                .then(
                    if (sheetState.currentValue != ModalBottomSheetValue.Hidden) {
                        Modifier
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    scope.launch { sheetState.animateTo(ModalBottomSheetValue.Hidden) }
                                }
                            }
                            .semantics(mergeDescendants = true) {}
                    } else {
                        Modifier
                    }
                )
        )

        BoxWithConstraints(Modifier.statusBarsPadding()) {
            val fullHeight = constraints.maxHeight.toFloat()
            val sheetHeightState = remember { mutableStateOf<Float?>(null) }

            Surface(
                Modifier
                    .fillMaxWidth()
                    .offset {
                        val y = sheetState.offset.value.roundToInt()
                        IntOffset(0, y)
                    }
                    .nestedScroll(sheetState.PreUpPostDownNestedScrollConnection)
                    .bottomSheetSwipeable(sheetState, fullHeight, sheetHeightState)
                    .onPlaced {
                        sheetHeightState.value = it.size.height.toFloat()
                    }
                    .drawBehind {
                        drawRect(sheetBackgroundColor, topLeft = Offset(x = 0f, y = center.y), size = size)
                    }
                    .semantics {
                        if (sheetState.currentValue != ModalBottomSheetValue.Hidden) {
                            dismiss {
                                scope.launch { sheetState.animateTo(ModalBottomSheetValue.Hidden) }
                                true
                            }
                        }
                    },
                shape = sheetShape,
                elevation = sheetElevation,
                color = sheetBackgroundColor,
                contentColor = sheetContentColor
            ) {
                Column(content = sheetContent)
            }
        }
    }
}

@Suppress("ModifierInspectorInfo")
@OptIn(ExperimentalMaterialApi::class)
private fun Modifier.bottomSheetSwipeable(
    sheetState: SwipeableState<ModalBottomSheetValue>,
    fullHeight: Float,
    sheetHeightState: State<Float?>
): Modifier {
    val sheetHeight = sheetHeightState.value
    val modifier = if (sheetHeight != null) {
        val anchors =
            mapOf(
                fullHeight to ModalBottomSheetValue.Hidden,
                max(fullHeight - sheetHeight + 1f, fullHeight / 2) to ModalBottomSheetValue.HalfExpanded,
                max(0f, fullHeight - sheetHeight) to ModalBottomSheetValue.Expanded
            )

        Modifier.swipeable(
            state = sheetState,
            anchors = anchors,
            orientation = Orientation.Vertical,
            enabled = sheetState.currentValue != ModalBottomSheetValue.Hidden,
            resistance = null
        )
    } else {
        Modifier
    }

    return this.then(modifier)
}

@ExperimentalMaterialApi
private val <T> SwipeableState<T>.PreUpPostDownNestedScrollConnection: NestedScrollConnection
    get() = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val delta = available.toFloat()
            return if (delta < 0 && source == NestedScrollSource.Drag) {
                performDrag(delta).toOffset()
            } else {
                Offset.Zero
            }
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            return if (source == NestedScrollSource.Drag) {
                performDrag(available.toFloat()).toOffset()
            } else {
                Offset.Zero
            }
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            val toFling = Offset(available.x, available.y).toFloat()
            return if (toFling < 0) {
                performFling(velocity = toFling)
                // since we go to the anchor with tween settling, consume all for the best UX
                available
            } else {
                Velocity.Zero
            }
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            performFling(velocity = Offset(available.x, available.y).toFloat())
            return available
        }

        private fun Float.toOffset(): Offset = Offset(0f, this)

        private fun Offset.toFloat(): Float = this.y
    }
