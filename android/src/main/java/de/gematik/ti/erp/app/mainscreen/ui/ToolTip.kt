/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.core.MainViewModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.Dialog
import kotlinx.coroutines.launch

enum class ArrowPosition {
    Top,
    Right
}

@Immutable
data class ToolTipState(
    var arrowPosition: ArrowPosition = ArrowPosition.Top,
    var elementBound: Rect = Rect(0f, 0f, 0f, 0f)
)

@Composable
fun ToolTips(
    mainViewModel: MainViewModel,
    isInPrescriptionScreen: Boolean,
    toolTipBounds: MutableState<Map<Int, Rect>>
) {
    val coroutineScope = rememberCoroutineScope()

    var tooltipNr by remember { mutableStateOf(0) }

    val showMainScreenTooltips by produceState(initialValue = false) {
        mainViewModel.showMainScreenToolTips().collect {
            value = it
        }
    }
    if (isInPrescriptionScreen && showMainScreenTooltips) {
        when (tooltipNr) {
            0 -> ToolTip(
                onDismissRequest = {
                    tooltipNr += 1
                },
                tooltipState = ToolTipState(
                    arrowPosition = ArrowPosition.Right,
                    elementBound = toolTipBounds.value[0] ?: Rect.Zero
                ),
                content = stringResource(R.string.main_screen_tooltip_1_content)
            )

            1 -> ToolTip(
                onDismissRequest = {
                    tooltipNr += 1
                },
                tooltipState = ToolTipState(
                    arrowPosition = ArrowPosition.Top,
                    elementBound = toolTipBounds.value[1] ?: Rect.Zero
                ),
                content = stringResource(R.string.main_screen_tooltip_2_content)
            )

            2 -> ToolTip(
                onDismissRequest = {
                    coroutineScope.launch {
                        mainViewModel.mainScreenTooltipsShown()
                    }
                },
                tooltipState = ToolTipState(
                    arrowPosition = ArrowPosition.Top,
                    elementBound = toolTipBounds.value[2] ?: Rect.Zero
                ),
                content = stringResource(R.string.main_screen_tooltip_3_content)
            )
        }
    }
}

@Composable
fun ToolTip(
    onDismissRequest: () -> Unit,
    tooltipState: ToolTipState,
    content: String
) {
    val contentDescription = stringResource(R.string.main_screen_tooltip_tap_screen)
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        when (tooltipState.arrowPosition) {
            ArrowPosition.Top -> ToolTipWithArrowTop(tooltipState, content, contentDescription, onDismissRequest)
            ArrowPosition.Right -> ToolTipWithArrowRight(tooltipState, content, contentDescription, onDismissRequest)
        }
    }
}

@Composable
fun ToolTipWithArrowTop(
    tooltipState: ToolTipState,
    content: String,
    description: String,
    onDismissRequest: () -> Unit
) {
    val offset = with(LocalDensity.current) {
        DpOffset(
            x = tooltipState.elementBound.bottomCenter.x.toDp() - 12.dp,
            y = tooltipState.elementBound.bottomCenter.y.toDp() + 4.dp
        )
    }

    TooltipScaffold(
        description = description,
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painterResource(R.drawable.ic_tooltip_arrow_top),
                null,
                tint = AppTheme.colors.neutral800,
                modifier = Modifier
                    .offset(x = offset.x, y = offset.y)
            )
            val textAlignX = min(this@TooltipScaffold.maxWidth - 231.dp - PaddingDefaults.Large, offset.x - 16.dp)
            Text(
                text = content,
                modifier = Modifier
                    .width(231.dp)
                    .offset(x = textAlignX, y = offset.y)
                    .background(
                        color = AppTheme.colors.neutral800,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = PaddingDefaults.Large, vertical = PaddingDefaults.Medium),
                color = AppTheme.colors.neutral000,
                style = AppTheme.typography.body2
            )
        }
    }
}

@Composable
fun ToolTipWithArrowRight(
    tooltipState: ToolTipState,
    content: String,
    description: String,
    onDismissRequest: () -> Unit
) {
    val offset = with(LocalDensity.current) {
        DpOffset(
            x = tooltipState.elementBound.centerLeft.x.toDp() - 231.dp,
            y = tooltipState.elementBound.centerLeft.y.toDp() - 12.dp
        )
    }

    TooltipScaffold(
        description = description,
        onDismissRequest = onDismissRequest
    ) {
        Row(
            modifier = Modifier.offset(x = offset.x)
        ) {
            Text(
                text = content,
                modifier = Modifier
                    .width(231.dp)
                    .offset(y = offset.y - 16.dp)
                    .background(
                        color = AppTheme.colors.neutral800,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = PaddingDefaults.Large, vertical = PaddingDefaults.Medium),
                color = AppTheme.colors.neutral000,
                style = AppTheme.typography.body2
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_tooltip_arrow_right),
                contentDescription = null,
                tint = AppTheme.colors.neutral800,
                modifier = Modifier
                    .offset(y = offset.y)
            )
        }
    }
}

@Composable
private fun TooltipScaffold(
    description: String,
    onDismissRequest: () -> Unit,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) =
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = onDismissRequest,
                role = Role.Button,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .semantics { contentDescription = description },
        content = content
    )
