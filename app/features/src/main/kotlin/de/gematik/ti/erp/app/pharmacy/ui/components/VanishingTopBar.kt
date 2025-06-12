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

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium

val TopBarColor = Color(0xffd6e9fb)

// TODO: This is causing the crashes in the store, replaced for now
@Suppress("UnusedPrivateMember")
@Composable
private fun VanishingTopBar(
    listState: LazyListState,
    videoHeightPx: State<Float>,
    onBack: () -> Unit
) {
    var topBarHeightPx by remember { mutableFloatStateOf(0f) }

    val showTopBar by remember {
        derivedStateOf {
            when {
                listState.firstVisibleItemIndex > 0 -> true
                listState.firstVisibleItemScrollOffset - videoHeightPx.value > -topBarHeightPx -> true
                else -> false
            }
        }
    }
    val showTopBarText by remember {
        derivedStateOf {
            when {
                listState.firstVisibleItemIndex > 0 -> true
                else -> false
            }
        }
    }
    val topBarAlpha by animateFloatAsState(if (showTopBar) 1f else 0f, tween(), label = "topBarAlpha")
    val topBarElevation by animateDpAsState(if (showTopBar) 4.dp else 0.dp, tween(), label = "topBarElevation")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .onPlaced {
                topBarHeightPx = it.size.height.toFloat()
            },
        color = TopBarColor.copy(alpha = topBarAlpha),
        elevation = topBarElevation
    ) {
        Row(Modifier.statusBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                modifier = Modifier.padding(PaddingDefaults.Tiny),
                onClick = onBack
            ) {
                Box(
                    Modifier
                        .size(32.dp)
                        .background(AppTheme.colors.neutral000, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        contentDescription = null,
                        tint = AppTheme.colors.primary700,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            SpacerMedium()
            AnimatedVisibility(
                showTopBarText,
                enter = fadeIn(tween()),
                exit = fadeOut(tween())
            ) {
                Text(
                    text = stringResource(R.string.pharmacy_order_title),
                    textAlign = TextAlign.Center,
                    style = AppTheme.typography.h6,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
