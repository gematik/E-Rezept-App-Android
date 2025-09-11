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

package de.gematik.ti.erp.app.eurezept.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.padding.ApplicationInnerPadding
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
@Composable
fun EuRedeemScaffold(
    listState: LazyListState,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    topBarTitle: String = "",
    bottomBar: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    showCloseButton: Boolean = true,
    topBarColor: Color = colors.surface,
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    cancelButtonText: String = stringResource(R.string.eu_consent_close),
    applicationPadding: ApplicationInnerPadding? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val scaffoldState = rememberScaffoldState()

    AnimatedElevationScaffold(
        modifier = modifier,
        backLabel = stringResource(R.string.back),
        closeLabel = "",
        navigationMode = NavigationBarMode.Back,
        topBarTitle = topBarTitle,
        topBarColor = topBarColor,
        scaffoldState = scaffoldState,
        snackbarHost = snackbarHost,
        onBack = onBack,
        applicationPadding = applicationPadding,
        actions = {
            if (showCloseButton) {
                TextButton(
                    onClick = onCancel
                ) {
                    Text(
                        text = cancelButtonText,
                        color = AppTheme.colors.primary700
                    )
                }
            }
        },
        listState = listState,
        bottomBar = bottomBar,
        content = content
    )
}
