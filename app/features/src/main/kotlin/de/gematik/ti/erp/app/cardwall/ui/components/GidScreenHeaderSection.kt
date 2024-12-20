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

package de.gematik.ti.erp.app.cardwall.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.erezeptTextFieldColors
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Suppress("FunctionName")
@Composable
fun GidScreenHeaderSection(
    modifier: Modifier = Modifier,
    searchValue: TextFieldValue,
    focusRequester: FocusRequester,
    onValueChange: (TextFieldValue) -> Unit,
    onNavigateToHelp: () -> Unit
) {
    ExternalAuthenticationScreenHeader(
        modifier = modifier,
        searchValue = searchValue,
        focusRequester = focusRequester,
        onValueChange = onValueChange,
        onNavigateToHelp = onNavigateToHelp
    )
}

@Composable
private fun ExternalAuthenticationScreenHeader(
    modifier: Modifier = Modifier,
    searchValue: TextFieldValue,
    focusRequester: FocusRequester,
    onValueChange: (TextFieldValue) -> Unit,
    onNavigateToHelp: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(AppTheme.colors.neutral000)
            .then(modifier)
    ) {
        Text(
            stringResource(R.string.cardwall_gid_header),
            style = AppTheme.typography.h6
        )
        SpacerSmall()
        Text(
            stringResource(R.string.cardwall_gid_body),
            style = AppTheme.typography.body2l
        )
        SpacerSmall()
        TextButton(
            modifier = Modifier.align(Alignment.End),
            onClick = onNavigateToHelp,
            content = {
                Text(
                    stringResource(R.string.cardwall_gid_help_button),
                    style = AppTheme.typography.body1
                )
                SpacerTiny()
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = AppTheme.colors.primary600
                )
            }
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

@Composable
private fun SearchField(
    value: TextFieldValue,
    focusRequester: FocusRequester,
    onValueChange: (TextFieldValue) -> Unit
) =
    ErezeptOutlineText(
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
        colors = erezeptTextFieldColors(
            focusedLeadingIconColor = AppTheme.colors.neutral600,
            unfocusedLeadingIconColor = AppTheme.colors.neutral600,
            focusedContainerColor = AppTheme.colors.neutral100,
            unfocusedContainerColor = AppTheme.colors.neutral100,
            focusedPlaceholderColor = AppTheme.colors.neutral600,
            unfocusedPlaceholderColor = AppTheme.colors.neutral600,
            focussedBorderColor = Color.Unspecified,
            unfocusedBorderColor = Color.Unspecified,
            disabledBorderColor = Color.Unspecified,
            errorBorderColor = Color.Unspecified
        )
    )

@LightDarkPreview
@Composable
fun CardWallExternalAuthenticationScreenHeaderPreview() {
    PreviewAppTheme {
        ExternalAuthenticationScreenHeader(
            searchValue = TextFieldValue(""),
            focusRequester = FocusRequester(),
            onValueChange = {},
            onNavigateToHelp = {}
        )
    }
}
