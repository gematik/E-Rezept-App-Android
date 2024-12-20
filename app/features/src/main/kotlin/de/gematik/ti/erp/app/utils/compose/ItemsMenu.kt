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

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.DpOffset
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
fun <T> ItemsMenu(
    modifier: Modifier = Modifier,
    menuItems: List<T>,
    onClickItem: (T) -> Unit,
    dropDownMenuItem: @Composable (T) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isContextMenuVisible by rememberSaveable { mutableStateOf(false) }
    var pressOffset by remember { mutableStateOf(DpOffset.Zero) }
    var itemHeight by remember { mutableStateOf(SizeDefaults.zero) }
    val density = LocalDensity.current

    Card(
        modifier = modifier.onSizeChanged {
            itemHeight = with(density) { it.height.toDp() }
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = SizeDefaults.half
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(true) {
                    detectTapGestures(
                        onTap = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isContextMenuVisible = true
                            pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
                        },
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isContextMenuVisible = true
                            pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
                        }
                    )
                }
                .padding(SizeDefaults.double)
        ) {
            Text(text = "Open Menu")
            DropdownMenu(
                expanded = isContextMenuVisible,
                onDismissRequest = { isContextMenuVisible = false }
            ) {
                menuItems.forEach {
                    DropdownMenuItem(
                        text = { dropDownMenuItem.invoke(it) },
                        onClick = {
                            onClickItem(it)
                            isContextMenuVisible = false
                        }
                    )
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
fun ItemsMenuPreview() {
    PreviewAppTheme {
        ItemsMenu(
            menuItems = listOf("Item 1", "Item 2", "Item 3"),
            onClickItem = {},
            dropDownMenuItem = {
                Box(
                    modifier = Modifier.width(SizeDefaults.triple)
                ) {
                    Text(text = it)
                }
            }
        )
    }
}
