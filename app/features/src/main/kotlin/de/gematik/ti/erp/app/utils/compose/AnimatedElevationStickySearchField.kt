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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.digas.ui.components.BottomEdgeShape
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty

/**
 * A Composable function that creates a sticky search field within a [LazyListScope] (e.g., `LazyColumn`).
 * This search field animates its elevation based on the scroll position of the list,
 * typically becoming elevated when the item(s) above it are scrolled out of view.
 * It is designed to be used in conjunction with a non-elevated TopAppBar.
 *
 * The search field is best used togther with a non elevated topbar which uses AnimatedTitleContent
 * @see de.gematik.ti.erp.app.topbar.AnimatedTitleContent
 *
 * @param lazyListState The [LazyListState] associated with the parent `LazyColumn` or `LazyRow`.
 *                      This is used to determine the scroll position and trigger elevation changes.
 * @param focusManager The [FocusManager] used to control keyboard focus, specifically to clear
 *                     focus when the search action is performed on the keyboard.
 * @param value The current text value to be displayed in the search field.
 * @param onValueChange A callback that is invoked when the input service updates the text.
 *                      The new text value is provided as a parameter to the callback.
 * @param onRemoveValue A callback that is invoked when the clear button (trailing icon) in the
 *                      search field is clicked. This should typically clear the [value].
 * @param description A content description for the search field, used for accessibility.
 * @param indexOfPreviousItemInList The index of the item in the `LazyList` immediately preceding
 *                                  this sticky header. The elevation will appear when the
 *                                  `lazyListState.firstVisibleItemIndex` becomes greater than
 *                                  this value. Defaults to `0`, assuming this search field
 *                                  is placed after one initial item (e.g., a simple header).
 */
@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.animatedElevationStickySearchField(
    lazyListState: LazyListState,
    focusManager: FocusManager,
    value: String,
    onValueChange: (String) -> Unit,
    onRemoveValue: () -> Unit,
    description: String,
    indexOfPreviousItemInList: Int = 0
) {
    stickyHeader {
        val isHeaderElevated by remember {
            derivedStateOf { lazyListState.firstVisibleItemIndex > indexOfPreviousItemInList }
        }
        Surface(
            elevation = when {
                isHeaderElevated -> AppBarDefaults.TopAppBarElevation
                else -> SizeDefaults.zero
            },
            shape = BottomEdgeShape()
        ) {
            OutlinedTextField(
                modifier = Modifier.semantics {
                    contentDescription = description
                }.fillMaxWidth()
                    .padding(
                        horizontal = PaddingDefaults.Medium,
                        vertical = PaddingDefaults.Small
                    ),
                value = value,
                onValueChange = onValueChange,
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = ""
                    )
                },
                trailingIcon = {
                    if (value.isNotNullOrEmpty()) {
                        IconButton(
                            onClick = onRemoveValue
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = stringResource(id = R.string.a11y_deleted_text)
                            )
                        }
                    }
                },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.search_pharmacies_start_search),
                        style = AppTheme.typography.body1,
                        color = AppTheme.colors.neutral600
                    )
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { focusManager.clearFocus() }
                ),
                shape = RoundedCornerShape(SizeDefaults.double)
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun AnimatedElevationStickySearchFieldPreview() {
    PreviewAppTheme {
        val listState = rememberLazyListState()
        val focusManager = LocalFocusManager.current
        LazyColumn(state = listState) {
            animatedElevationStickySearchField(
                lazyListState = listState,
                focusManager = focusManager,
                value = "",
                onValueChange = { _ -> },
                onRemoveValue = {},
                description = ""
            )
        }
    }
}
