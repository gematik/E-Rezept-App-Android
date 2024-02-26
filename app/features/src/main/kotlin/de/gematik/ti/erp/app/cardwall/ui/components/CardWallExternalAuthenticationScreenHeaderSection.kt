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

package de.gematik.ti.erp.app.cardwall.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall

@Suppress("FunctionName")
@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.CardWallExternalAuthenticationScreenHeaderSection(
    modifier: Modifier = Modifier,
    searchValue: TextFieldValue,
    focusRequester: FocusRequester,
    onValueChange: (TextFieldValue) -> Unit
) {
    stickyHeader {
        Column(
            modifier = Modifier
                .background(AppTheme.colors.neutral000)
                .then(modifier)
        ) {
            Text(
                stringResource(R.string.cdw_fasttrack_choose_insurance),
                style = MaterialTheme.typography.h6
            )
            SpacerSmall()
            Text(
                stringResource(R.string.cdw_fasttrack_help_info),
                style = AppTheme.typography.body2l
            )
            SpacerLarge()
            SearchField(
                value = searchValue,
                focusRequester = focusRequester,
                onValueChange = {
                    onValueChange(it)
                }
            )
            SpacerMedium()
        }
    }
}

@Composable
private fun SearchField(
    value: TextFieldValue,
    focusRequester: FocusRequester,
    onValueChange: (TextFieldValue) -> Unit
) =
    OutlinedTextField(
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (it.isFocused) {
                    focusRequester.requestFocus()
                }
            }
            .focusRequester(focusRequester),
        placeholder = {
            Text(
                stringResource(R.string.cdw_fasttrack_search_placeholder),
                style = AppTheme.typography.body1l
            )
        },
        shape = RoundedCornerShape(PaddingDefaults.Medium),
        leadingIcon = { Icon(Icons.Rounded.Search, null) },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = AppTheme.colors.neutral100,
            placeholderColor = AppTheme.colors.neutral600,
            leadingIconColor = AppTheme.colors.neutral600,
            focusedBorderColor = Color.Unspecified,
            unfocusedBorderColor = Color.Unspecified,
            disabledBorderColor = Color.Unspecified,
            errorBorderColor = Color.Unspecified
        )
    )
