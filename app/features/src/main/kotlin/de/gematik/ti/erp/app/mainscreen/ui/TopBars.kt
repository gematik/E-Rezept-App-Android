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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Tab
import androidx.compose.material.TabPosition
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults

@Composable
fun TextTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    onClick: (index: Int) -> Unit,
    backGroundColor: Color,
    tabs: List<String>,
    testTags: (List<String>)? = null
) {
    var contentWidth by remember { mutableStateOf(0) }

    TabRow(
        modifier = modifier,
        selectedTabIndex = selectedTabIndex,
        backgroundColor = backGroundColor,
        indicator = { tabPositions ->
            TabIndicator(tabPositions, selectedTabIndex, with(LocalDensity.current) { contentWidth.toDp() })
        },
        divider = {}
    ) {
        tabs.forEachIndexed { tabIndex: Int, tabText: String ->
            Tab(
                modifier = testTags?.let { Modifier.testTag(testTags[tabIndex]) } ?: Modifier,
                selected = tabIndex == selectedTabIndex,
                onClick = { onClick(tabIndex) },
                selectedContentColor = AppTheme.colors.primary700,
                unselectedContentColor = AppTheme.colors.neutral500
            ) {
                Text(
                    text = tabText,
                    style = AppTheme.typography.subtitle2,
                    modifier = Modifier
                        .padding(top = PaddingDefaults.Small)
                        .padding(bottom = PaddingDefaults.Small + 2.dp)
                        .align(Alignment.CenterHorizontally)
                        .wrapContentWidth()
                        .onSizeChanged { size -> contentWidth = size.width }
                )
            }
        }
    }
}

@Composable
private fun TabIndicator(tabPositions: List<TabPosition>, selectedTab: Int, contentWidth: Dp) {
    val currentContentWidth by animateDpAsState(
        targetValue = contentWidth,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
    )
    val indicatorOffset by animateDpAsState(
        targetValue = tabPositions[selectedTab].left + (tabPositions[selectedTab].width - contentWidth) / 2,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
    )

    Box(
        modifier = Modifier.wrapContentSize(Alignment.BottomStart)
            .offset(indicatorOffset)
            .width(currentContentWidth)
            .clip(RoundedCornerShape(2.dp))
            .height(2.dp)
            .background(color = AppTheme.colors.primary700)
    )
}
