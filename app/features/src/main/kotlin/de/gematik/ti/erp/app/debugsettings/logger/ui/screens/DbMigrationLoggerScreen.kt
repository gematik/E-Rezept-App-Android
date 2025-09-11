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

package de.gematik.ti.erp.app.debugsettings.logger.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.debugsettings.logger.presentation.rememberDbMigrationLoggerScreenController
import de.gematik.ti.erp.app.debugsettings.logger.preview.DbMigrationLoggerScreenPreviewData
import de.gematik.ti.erp.app.debugsettings.logger.preview.DbMigrationLoggerScreenPreviewParameterProvider
import de.gematik.ti.erp.app.listitem.GemListItemDefaults
import de.gematik.ti.erp.app.logger.model.DbMigrationLogEntry
import de.gematik.ti.erp.app.logger.model.DbMigrationLogEntry.Companion.checkVersions
import de.gematik.ti.erp.app.logger.model.DbMigrationLogEntry.Companion.toJson
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.topbar.AnimatedTitleContent
import de.gematik.ti.erp.app.utils.compose.EmptyScreenComponent
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.animatedElevationStickySearchField
import de.gematik.ti.erp.app.utils.compose.fullscreen.FullScreenLoadingIndicator
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.uistate.UiState

class DbMigrationLoggerScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val listState = rememberLazyListState()
        val context = LocalContext.current
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val focusManager = LocalFocusManager.current
        val controller = rememberDbMigrationLoggerScreenController()
        val dbMigrationLogEntries by controller.dbMigrationLogEntries.collectAsStateWithLifecycle()
        val searchValue by controller.searchValue.collectAsStateWithLifecycle()
        val expandedListItems = remember { controller.expandedListItems.toSet() }
        val onBack by rememberUpdatedState {
            navController.popBackStack()
        }

        BackHandler {
            onBack()
        }
        DbMigrationLoggerScreenScaffold(
            dbMigrationLogEntries = dbMigrationLogEntries,
            listState = listState,
            expandedListItems = expandedListItems,
            searchValue = searchValue,
            focusManager = focusManager,
            onChangeSearch = {
                controller.changeSearch(it)
            },
            onResetSearch = {
                controller.resetSearch()
            },
            onResetLogs = {
                controller.resetLogs()
            },
            onSave = {
                // controller.saveLogs()
            },
            onCopyLogEntry = { clipboard.setPrimaryClip(it) },
            onBack = {
                onBack()
            },
            onClickLogEntry = {
                controller.toggleListItems(it)
            }
        )
    }
}

@Composable
private fun DbMigrationLoggerScreenScaffold(
    dbMigrationLogEntries: UiState<List<DbMigrationLogEntry>>,
    listState: LazyListState,
    searchValue: String,
    focusManager: FocusManager,
    expandedListItems: Set<String>,
    onClickLogEntry: (String) -> Unit,
    onCopyLogEntry: (ClipData) -> Unit,
    onChangeSearch: (String) -> Unit,
    onResetSearch: () -> Unit,
    onResetLogs: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            NavigationTopAppBar(
                modifier = Modifier,
                navigationMode = NavigationBarMode.Back,
                title = {
                    AnimatedTitleContent(
                        listState = listState,
                        indexOfTitleItemInList = 0,
                        title = "DbMigrationLogs"
                    )
                },
                elevation = SizeDefaults.zero,
                backLabel = stringResource(R.string.back),
                closeLabel = stringResource(R.string.cancel),
                onBack = onBack,
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
                    ) {
                        IconButton(
                            modifier = Modifier,
                            enabled = dbMigrationLogEntries.data?.isNotEmpty() ?: false,
                            onClick = onResetLogs
                        ) {
                            Icon(
                                Icons.Rounded.Delete,
                                null,
                                tint = AppTheme.colors.primary700
                            )
                        }
                        IconButton(
                            modifier = Modifier,
                            enabled = dbMigrationLogEntries.data?.isNotEmpty() ?: false,
                            onClick = onSave
                        ) {
                            Icon(
                                Icons.Rounded.Download,
                                null,
                                tint = AppTheme.colors.primary700
                            )
                        }
                    }
                }
            )
        }
    ) { contentPadding ->
        UiStateMachine(
            state = dbMigrationLogEntries,
            onLoading = {
                FullScreenLoadingIndicator()
            },
            onEmpty = {
                EmptyScreenComponent(
                    title = "No DbLogEntries",
                    body = "No DbLogEntries found.",
                    button = {},
                    image = {}
                )
            },
            onError = {
                ErrorScreenComponent()
            },
            onContent = { filteredDbMigrationLogs ->
                DbMigrationLoggerScreenContent(
                    listState = listState,
                    contentPadding = contentPadding,
                    focusManager = focusManager,
                    filteredDbMigrationLogs = filteredDbMigrationLogs,
                    searchValue = searchValue,
                    expandedListItems = expandedListItems,
                    onClickLogEntry = onClickLogEntry,
                    onChangeSearch = onChangeSearch,
                    onResetSearch = onResetSearch,
                    onCopyLogEntry = onCopyLogEntry
                )
            }
        )
    }
}

@Composable
private fun DbMigrationLoggerScreenContent(
    contentPadding: PaddingValues,
    filteredDbMigrationLogs: List<DbMigrationLogEntry>,
    listState: LazyListState,
    focusManager: FocusManager,
    expandedListItems: Set<String>,
    searchValue: String,
    onCopyLogEntry: (ClipData) -> Unit,
    onClickLogEntry: (String) -> Unit,
    onChangeSearch: (String) -> Unit,
    onResetSearch: () -> Unit
) {
    LazyColumn(
        contentPadding = contentPadding,
        state = listState
    ) {
        item {
            Text(
                "DbMigrationLogs",
                style = AppTheme.typography.h6
            )
        }
        animatedElevationStickySearchField(
            lazyListState = listState,
            indexOfPreviousItemInList = 0,
            value = searchValue,
            onValueChange = onChangeSearch,
            onRemoveValue = onResetSearch,
            focusManager = focusManager,
            description = ""
        )
        items(
            items = filteredDbMigrationLogs
        ) { dbMigrationLogEntry ->
            DbMigrationLogEntryListItem(
                dbMigrationLogEntry = dbMigrationLogEntry,
                expanded = expandedListItems.contains(dbMigrationLogEntry.id),
                onClickLogEntry = { onClickLogEntry(dbMigrationLogEntry.id) },
                onCopyLogEntry = onCopyLogEntry
            )
        }
    }
}

@Composable
private fun DbMigrationLogEntryListItem(
    dbMigrationLogEntry: DbMigrationLogEntry,
    expanded: Boolean,
    onClickLogEntry: () -> Unit,
    onCopyLogEntry: (ClipData) -> Unit
) {
    val clipData = ClipData.newPlainText(dbMigrationLogEntry.name, dbMigrationLogEntry.toJson())
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Divider()
        ListItem(
            modifier = Modifier.clickable {
                onClickLogEntry()
            },
            colors = GemListItemDefaults.gemListItemColors(),
            overlineContent = {
                Text(
                    dbMigrationLogEntry.timestamp,
                    style = AppTheme.typography.caption1
                )
            },
            headlineContent = {
                Text(dbMigrationLogEntry.name)
            },
            trailingContent = {
                IconButton(
                    onClick = {
                        onCopyLogEntry(
                            clipData
                        )
                    }
                ) {
                    Icon(
                        Icons.Rounded.ContentCopy,
                        null,
                        tint = AppTheme.colors.primary700
                    )
                }
            },
            leadingContent = {
                if (dbMigrationLogEntry.checkVersions()) {
                    Icon(Icons.Rounded.Check, null, tint = AppTheme.colors.green600)
                } else {
                    Icon(Icons.Rounded.Close, null, tint = AppTheme.colors.red600)
                }
            }
        )
        Divider()
        AnimatedVisibility(visible = expanded) {
            ExpandedDbMigrationLogEntryListItem(
                dbMigrationLogEntry
            )
        }
    }
}

@Composable
private fun ExpandedDbMigrationLogEntryListItem(
    dbMigrationLogEntry: DbMigrationLogEntry
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium),
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
    ) {
        Column(
            Modifier.weight(0.5f)
        ) {
            Text(
                "Version 1",
                style = AppTheme.typography.h6
            )
            Divider()
            Text(dbMigrationLogEntry.v1)
        }
        Column(
            Modifier.weight(0.5f)
        ) {
            Text("Version 2", style = AppTheme.typography.h6)
            Divider()
            Text(dbMigrationLogEntry.v2)
        }
    }
}

@LightDarkPreview
@Composable
private fun DbMigrationLoggerScreenPreview(
    @PreviewParameter(DbMigrationLoggerScreenPreviewParameterProvider::class) data: DbMigrationLoggerScreenPreviewData
) {
    PreviewAppTheme {
        DbMigrationLoggerScreenScaffold(
            dbMigrationLogEntries = data.dbMigrationLogEntries,
            listState = rememberLazyListState(),
            searchValue = data.searchValue,
            focusManager = LocalFocusManager.current,
            expandedListItems = data.expandedListItems,
            onClickLogEntry = {},
            onChangeSearch = {},
            onResetSearch = {},
            onResetLogs = {},
            onSave = {},
            onBack = {},
            onCopyLogEntry = {}
        )
    }
}
