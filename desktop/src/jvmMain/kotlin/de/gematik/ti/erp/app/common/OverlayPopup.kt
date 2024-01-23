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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DrawerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OverlayPopup(
    visible: Boolean,
    popupContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val transition = updateTransition(visible)
    val alpha by transition.animateFloat(transitionSpec = { tween() }) { if (it) 0.5f else 0f }

    val blurModifier = if (visible) {
        Modifier.graphicsLayer {
            renderEffect = BlurEffect(15f, 15f)
        }
    } else {
        Modifier
    }

    Box(Modifier.fillMaxSize()) {
        Box(blurModifier) {
            content()
        }

        val color = DrawerDefaults.scrimColor

        Canvas(
            Modifier
                .fillMaxSize()
        ) {
            drawRect(color, alpha = alpha)
        }

        Box {
            transition.AnimatedVisibility(
                visible = { it },
                modifier = Modifier.fillMaxSize(),
                exit = fadeOut(animationSpec = tween()) + shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween()
                ),
                enter = fadeIn(animationSpec = tween()) + expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween()
                )
            ) {
                Box {
                    val shape = RoundedCornerShape(16.dp)
                    Box(
                        modifier = Modifier
                            .padding(32.dp)
                            .widthIn(max = 720.dp)
                            .height(560.dp)
                            .fillMaxHeight(0.7f)
                            .align(Alignment.Center)
                            .shadow(6.dp, shape, false)
                            .clip(shape)
                    ) {
                        popupContent()
                    }
                }
            }
        }
    }
}
