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

package de.gematik.ti.erp.app.common

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import java.awt.Cursor
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalAnimationApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun HorizontalSplittable(
    split: Float = 0.5f,
    min: Float = 0.3f,
    max: Float = 0.7f,
    modifier: Modifier = Modifier,
    contentLeft: @Composable () -> Unit,
    contentRight: @Composable () -> Unit
) {
    require(min < max)

    var weight by remember(split) { mutableStateOf(max(min(split, max), min)) }
    var width by remember { mutableStateOf(1) }
    var hovered by remember { mutableStateOf(false) }
    var dragging by remember { mutableStateOf(false) }
    val pointer = remember(hovered, dragging) {
        if (hovered || dragging) {
            PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR))
        } else {
            PointerIcon.Default
        }
    }

    Box(modifier.pointerHoverIcon(pointer)) {
        Row(
            Modifier.fillMaxSize().onSizeChanged {
                width = it.width
            }
        ) {
            Box(Modifier.weight(weight)) {
                contentLeft()
            }
            Box(
                Modifier.fillMaxHeight()
                    .width(2.dp)
                    .draggable(
                        orientation = Orientation.Horizontal,
                        onDragStarted = {
                            dragging = true
                        },
                        onDragStopped = {
                            dragging = false
                        },
                        state = rememberDraggableState { delta ->
                            weight = max(min(weight + delta / width, max), min)
                        }
                    )
                    .pointerMoveFilter(
                        onEnter = {
                            hovered = true
                            false
                        },
                        onExit = {
                            hovered = false
                            false
                        }
                    )
            ) {
                VerticalDivider()
            }
            Box(Modifier.weight(1f - weight)) {
                contentRight()
            }
        }
    }
}
