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

package de.gematik.ti.erp.app.digas.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import kotlinx.coroutines.delay

@Composable
fun SearchInputField(
    isLoading: Boolean,
    searchValue: TextFieldValue,
    focusManager: FocusManager,
    onSearchInputChange: (TextFieldValue) -> Unit,
    onBack: () -> Unit
) {
    var isLoadingStable by remember { mutableStateOf(isLoading) }
    val description = stringResource(id = R.string.diga_insurance_searchbar)

    LaunchedEffect(isLoading) {
        delay(timeMillis = 330)
        isLoadingStable = isLoading
    }
    Column(
        Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.neutral000)
            .padding(bottom = PaddingDefaults.Medium)
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = description
                }
                .padding(horizontal = PaddingDefaults.Medium)
                .border(
                    width = 0.5.dp,
                    shape = RoundedCornerShape(SizeDefaults.double),
                    color = AppTheme.colors.neutral700
                ),
            value = searchValue,
            onValueChange = onSearchInputChange,
            placeholder = {
                Text(
                    text = stringResource(id = R.string.search_pharmacies_start_search),
                    style = AppTheme.typography.body1,
                    color = AppTheme.colors.neutral600
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Unspecified,
                autoCorrectEnabled = true,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions {
                focusManager.clearFocus()
            },
            shape = RoundedCornerShape(SizeDefaults.double),
            textStyle = AppTheme.typography.body1,
            leadingIcon = {
                IconButton(modifier = Modifier.clearAndSetSemantics { }, onClick = {}) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = ""
                    )
                }
            },
            trailingIcon = {
                Crossfade(
                    targetState = isLoadingStable,
                    animationSpec = tween(durationMillis = 550),
                    label = "Search Loading"
                ) { isLoading ->
                    if (isLoading) {
                        Box(Modifier.size(SizeDefaults.sixfold)) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(SizeDefaults.triple)
                                    .align(Alignment.Center),
                                strokeWidth = SizeDefaults.quarter
                            )
                        }
                    } else {
                        if (searchValue.text.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchInputChange(TextFieldValue("")) }
                            ) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = stringResource(id = R.string.a11y_deleted_text)
                                )
                            }
                        }
                    }
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                textColor = AppTheme.colors.neutral900,
                leadingIconColor = AppTheme.colors.neutral600,
                trailingIconColor = AppTheme.colors.neutral600,
                backgroundColor = AppTheme.colors.neutral000,
                focusedIndicatorColor = AppTheme.colors.neutral000,
                unfocusedIndicatorColor = AppTheme.colors.neutral000
            )
        )
    }
}
