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

package de.gematik.ti.erp.app.migration.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.database.migration.MigrationProgress
import de.gematik.ti.erp.app.migration.presentation.rememberDataMigrationViewModel
import de.gematik.ti.erp.app.migration.ui.components.DataMigrationDataContent
import de.gematik.ti.erp.app.migration.ui.components.DataMigrationErrorContent
import de.gematik.ti.erp.app.migration.ui.components.DataMigrationLoadingContent
import de.gematik.ti.erp.app.migration.ui.preview.DataMigrationScreenPreviewData
import de.gematik.ti.erp.app.migration.ui.preview.DataMigrationScreenPreviewParameterProvider
import de.gematik.ti.erp.app.utils.compose.BottomAppBar
import de.gematik.ti.erp.app.utils.compose.LightDarkLongPreview
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonLarge
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.openAppPlayStoreLink
import de.gematik.ti.erp.app.utils.uistate.UiState
@Composable
fun DataMigrationScreen(
    onContinueWithRealmFallback: (() -> Unit)?
) {
    val viewModel = rememberDataMigrationViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scaffoldState = rememberScaffoldState()
    val scrollState = rememberScrollState()
    DataMigrationScreenScaffold(
        uiState = uiState,
        scaffoldState = scaffoldState,
        scrollState = scrollState,
        onClickContinueWithRealmFallback = onContinueWithRealmFallback,
        onClickAccept = {
            viewModel.recoverMigrationFailure()
        }
    )
}

@Composable
internal fun DataMigrationScreenScaffold(
    uiState: UiState<MigrationProgress>,
    scaffoldState: ScaffoldState,
    scrollState: ScrollState,
    onClickContinueWithRealmFallback: (() -> Unit)?,
    onClickAccept: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        scaffoldState = scaffoldState,
        bottomBar = {
            UiStateMachine(
                uiState,
                onLoading = {},
                onEmpty = {},
                onError = {
                    BottomAppBar(
                        modifier = Modifier.navigationBarsPadding(),
                        backgroundColor = MaterialTheme.colors.surface
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PrimaryButtonLarge(
                                onClick = {
                                    onClickContinueWithRealmFallback?.invoke()
                                        ?: context.openAppPlayStoreLink()
                                }
                            ) {
                                Text(
                                    stringResource(
                                        if (onClickContinueWithRealmFallback == null) {
                                            R.string.data_migration_open_playstore
                                        } else {
                                            R.string.data_migration_accept
                                        }
                                    )
                                )
                            }
                        }
                    }
                },
                onContent = {
                    BottomAppBar(
                        modifier = Modifier.navigationBarsPadding(),
                        backgroundColor = MaterialTheme.colors.surface
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PrimaryButtonLarge(
                                onClick = onClickAccept
                            ) {
                                Text(
                                    stringResource(R.string.data_migration_accept)
                                )
                            }
                        }
                    }
                }
            )
        }
    ) {
            innerPadding ->
        DataMigrationScreenContent(
            uiState = uiState,
            scrollState = scrollState,
            innerPadding = innerPadding
        )
    }
}

@Composable
internal fun DataMigrationScreenContent(
    uiState: UiState<MigrationProgress>,
    innerPadding: PaddingValues,
    scrollState: ScrollState
) {
    UiStateMachine(
        uiState,
        onLoading = {
            DataMigrationLoadingContent(
                innerPadding,
                progress = uiState.data,
                scrollState = scrollState
            )
        },
        onEmpty = {
            DataMigrationLoadingContent(
                innerPadding,
                progress = uiState.data,
                scrollState = scrollState
            )
        },
        onError = {
            DataMigrationErrorContent(
                innerPadding = innerPadding,
                scrollState = scrollState
            )
        },
        onContent = {
            DataMigrationDataContent(
                innerPadding,
                scrollState = scrollState
            )
        }
    )
}

@LightDarkLongPreview
@Composable
fun DataMigrationScreenPreview(
    @PreviewParameter(DataMigrationScreenPreviewParameterProvider::class) previewData: DataMigrationScreenPreviewData
) {
    PreviewAppTheme {
        DataMigrationScreenScaffold(
            uiState = previewData.uiState,
            scrollState = rememberScrollState(),
            scaffoldState = rememberScaffoldState(),
            onClickAccept = {},
            onClickContinueWithRealmFallback = {}
        )
    }
}
