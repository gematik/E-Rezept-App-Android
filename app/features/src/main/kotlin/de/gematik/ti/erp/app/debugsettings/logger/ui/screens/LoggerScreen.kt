/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.debugsettings.logger.ui.screens

import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CopyAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.base.ClipBoardCopy
import de.gematik.ti.erp.app.debugsettings.logger.presentation.rememberLoggerScreenController
import de.gematik.ti.erp.app.debugsettings.logger.ui.screens.LoggerScreen.LoggerContent
import de.gematik.ti.erp.app.logger.mapper.toHar
import de.gematik.ti.erp.app.logger.mapper.toJson
import de.gematik.ti.erp.app.logger.model.ContentLog
import de.gematik.ti.erp.app.logger.model.HeaderLog
import de.gematik.ti.erp.app.logger.model.LogEntry
import de.gematik.ti.erp.app.logger.model.LogEntry.Companion.toJson
import de.gematik.ti.erp.app.logger.model.RequestLog
import de.gematik.ti.erp.app.logger.model.RequestLog.Companion.toJson
import de.gematik.ti.erp.app.logger.model.ResponseLog
import de.gematik.ti.erp.app.logger.model.ResponseLog.Companion.toJson
import de.gematik.ti.erp.app.logger.model.TimingsLog
import de.gematik.ti.erp.app.permissions.getWriteExternalStoragePermissionLauncher
import de.gematik.ti.erp.app.permissions.isWritePermissionGranted
import de.gematik.ti.erp.app.permissions.writeExternalStorage
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import de.gematik.ti.erp.app.utils.extensions.show
import de.gematik.ti.erp.app.utils.extensions.showWithDismissButton
import de.gematik.ti.erp.app.utils.formatJson
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

object LoggerScreen {

    @Suppress("MemberNameEqualsClassName")
    @Composable
    fun LoggerScreen(onBack: () -> Unit) = Content(onBack)

    @Composable
    private fun Content(onBack: () -> Unit) {
        val context = LocalContext.current
        val snackbar = LocalSnackbarScaffold.current
        val scope = rememberCoroutineScope()

        val listState = rememberLazyListState()
        val expandedItems = remember { mutableStateMapOf<Int, Boolean>() }
        val controller = rememberLoggerScreenController()

        val onWriteStoragePermissionGrantedEvent = ComposableEvent<Unit>()

        val httpLogs by controller.httpLogs.collectAsStateWithLifecycle()

        val permissionLauncher: ManagedActivityResultLauncher<String, Boolean> = getWriteExternalStoragePermissionLauncher { isGranted ->
            when {
                isGranted -> onWriteStoragePermissionGrantedEvent.trigger()
                else -> snackbar.showWithDismissButton(
                    message = "Write permission denied",
                    actionLabel = "Close",
                    scope = scope
                )
            }
        }

        onWriteStoragePermissionGrantedEvent.listen {
            val path = "${ContextCompat.getExternalFilesDirs(context, null).first()}" +
                "/${BuildKonfig.VERSION_NAME}_logs.har"
            try {
                val logs = httpLogs.toHar().toJson()
                controller.saveHarToFile(
                    harContent = logs,
                    filePath = path
                )
            } catch (e: Throwable) {
                Napier.e { "$e" }
                snackbar.show(
                    message = "Error saving logs",
                    scope = scope
                )
            } finally {
                Napier.i { "file path $path" }
                snackbar.showWithDismissButton(
                    message = "Logs saved to $path",
                    actionLabel = "Close",
                    duration = SnackbarDuration.Indefinite,
                    scope = scope
                )
            }
        }

        AnimatedElevationScaffold(
            topBarTitle = "Logger",
            navigationMode = NavigationBarMode.Back,
            listState = listState,
            actions = {
                Row {
                    PrimaryButton(
                        modifier = Modifier.padding(end = PaddingDefaults.Medium),
                        enabled = httpLogs.isNotEmpty(),
                        onClick = controller::resetLogs
                    ) {
                        Text("Reset")
                    }
                    PrimaryButton(
                        modifier = Modifier.padding(end = PaddingDefaults.Medium),
                        enabled = httpLogs.isNotEmpty(),
                        onClick = {
                            if (context.isWritePermissionGranted()) {
                                onWriteStoragePermissionGrantedEvent.trigger()
                            } else {
                                scope.launch {
                                    permissionLauncher.launchBasedOnSdkVersion {
                                        onWriteStoragePermissionGrantedEvent.trigger()
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            },
            onBack = onBack,
            content = {
                LoggerContent(
                    listState = listState,
                    expandedItems = expandedItems,
                    httpLogs = httpLogs
                ) { index ->
                    expandedItems[index] = !(expandedItems[index] ?: false)
                }
            }
        )
    }

    @Composable
    internal fun LoggerContent(
        listState: LazyListState,
        expandedItems: Map<Int, Boolean>,
        httpLogs: List<LogEntry>,
        isExpanded: (Int) -> Unit
    ) {
        var searchQuery by remember { mutableStateOf("") }

        val filteredLogs = remember(searchQuery, httpLogs) {
            httpLogs.filter { log ->
                val statusMatches = log.response.status.toString().contains(searchQuery)
                val urlMatches = log.request.url.contains(searchQuery, ignoreCase = true)
                statusMatches || urlMatches
            }
        }

        Column {
            ErezeptOutlineText(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.Small),
                label = { Text("Search by status or URL") },
                singleLine = true
            )

            if (filteredLogs.isEmpty()) {
                ErrorScreenComponent()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {
                    itemsIndexed(filteredLogs) { index, log ->
                        LogCard(log = log, isExpanded = expandedItems[index] == true) {
                            isExpanded(index)
                        }
                        Divider()
                    }
                    item {
                        SpacerLarge()
                    }
                }
            }
        }
    }
}

// After Android 11 scoped storage is forced, so we don't need to request permission to write  app based external storage
private fun ManagedActivityResultLauncher<String, Boolean>.launchBasedOnSdkVersion(
    onNoTrigger: () -> Unit
) {
    when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.R -> launch(writeExternalStorage)
        else -> onNoTrigger()
    }
}

@Suppress("MagicNumber")
@Composable
private fun OverviewEntry(entry: LogEntry) {
    Row {
        Text(
            modifier = Modifier.weight(0.8f),
            text = "Overview",
            style = AppTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold
        )

        CopyButton(
            modifier = Modifier.weight(0.2f),
            textToCopy = entry.toJson()
        )
    }
    Text(
        text = "Request: ${entry.request.method} ",
        style = AppTheme.typography.subtitle2
    )
    Text(
        text = "Response: ${entry.response.status} ",
        style = AppTheme.typography.subtitle2
    )
    Text(
        text = "Timestamp",
        style = AppTheme.typography.subtitle1,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = entry.timestamp,
        style = AppTheme.typography.caption1
    )
}

@Composable
private fun Header(
    entry: List<HeaderLog>
) {
    SpacerTiny()
    Text(
        text = "Headers",
        style = AppTheme.typography.subtitle2
    )
    entry.forEach {
        Text(
            text = "${it.name} ${it.value}",
            style = AppTheme.typography.caption1
        )
    }
}

@Suppress("MagicNumber")
@Composable
private fun RequestEntry(
    entry: LogEntry
) {
    Row {
        Text(
            modifier = Modifier.weight(0.8f),
            text = "Request",
            style = AppTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold
        )

        CopyButton(
            modifier = Modifier.weight(0.2f),
            textToCopy = entry.request.toJson()
        )
    }
    Text(
        text = "method = ${entry.request.method}",
        style = AppTheme.typography.caption1
    )
    Text(
        text = "url = ${entry.request.url}",
        style = AppTheme.typography.caption1
    )
    if (entry.request.headers.isNotEmpty()) {
        Header(entry = entry.request.headers)
    }
}

@Suppress("MagicNumber")
@Composable
private fun ResponseEntry(entry: LogEntry) {
    Row {
        Text(
            modifier = Modifier.weight(0.8f),
            text = "Response",
            style = AppTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold
        )

        CopyButton(
            modifier = Modifier.weight(0.2f),
            textToCopy = entry.response.toJson()
        )
    }
    Text(
        text = "statuscode = ${entry.response.status}",
        style = AppTheme.typography.caption1
    )
    if (entry.response.statusText.isNotNullOrEmpty()) {
        Text(
            text = "text = ${entry.response.statusText}",
            style = AppTheme.typography.caption1
        )
    }
    if (entry.response.headers.isNotEmpty()) {
        Header(entry = entry.response.headers)
    }
    SpacerTiny()
    Text(
        text = "Content",
        style = AppTheme.typography.caption2
    )
    Text(
        text = "mimeType = ${entry.response.content.mimeType}",
        style = AppTheme.typography.caption1
    )
    Text(
        text = entry.response.content.text.formatJson(),
        style = AppTheme.typography.caption1
    )
}

@Composable
private fun TimingsEntry(
    modifier: Modifier,
    entry: LogEntry
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "Timings",
            style = AppTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold
        )
        SpacerTiny()
        Text(
            text = "sent at ${entry.timings.send()}",
            style = AppTheme.typography.caption1
        )
        Text(
            text = "wait time ${entry.timings.wait()}",
            style = AppTheme.typography.caption1
        )
        Text(
            text = "received at ${entry.timings.receive()}",
            style = AppTheme.typography.caption1
        )
    }
}

@Suppress("MagicNumber")
@Composable
private fun statusColor(statusCode: Int) = when (statusCode) {
    in 200..299 -> AppTheme.colors.green400 // Green for success
    in 300..399 -> AppTheme.colors.primary400 // Blue for redirect
    in 400..499 -> AppTheme.colors.red400 // Red for client error
    in 500..599 -> AppTheme.colors.yellow400 // Yellow for server error
    else -> AppTheme.colors.neutral400
}

@Composable
private fun LogCard(
    log: LogEntry,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .clickable { onToggleExpand() }
                .padding(PaddingDefaults.Medium)
        ) {
            Text(
                text = "[${log.response.status}]",
                style = AppTheme.typography.subtitle2,
                fontWeight = FontWeight.Bold,
                color = statusColor(log.response.status)
            )
            SpacerMedium()
            Text(
                text = log.request.url,
                style = AppTheme.typography.subtitle2
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            LogDetailsTabs(log)
        }
    }
}

@Composable
fun LogDetailsTabs(log: LogEntry) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .padding(vertical = PaddingDefaults.Medium)
            .fillMaxSize()
    ) {
        TabRow(
            modifier = Modifier.padding(vertical = PaddingDefaults.Medium),
            backgroundColor = AppTheme.colors.neutral025,
            selectedTabIndex = selectedTabIndex
        ) {
            Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }) {
                Text(
                    modifier = Modifier.padding(vertical = PaddingDefaults.Small),
                    text = "Overview"
                )
            }
            Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }) {
                Text(
                    modifier = Modifier.padding(vertical = PaddingDefaults.Small),
                    text = "Request"
                )
            }
            Tab(selected = selectedTabIndex == 2, onClick = { selectedTabIndex = 2 }) {
                Text(
                    modifier = Modifier.padding(vertical = PaddingDefaults.Small),
                    text = "Response"
                )
            }
        }

        when (selectedTabIndex) {
            0 -> {
                Column(modifier = Modifier.padding(PaddingDefaults.Medium)) {
                    OverviewEntry(entry = log)
                }
            }

            1 -> {
                Column(modifier = Modifier.padding(PaddingDefaults.Medium)) {
                    RequestEntry(entry = log)
                }
            }

            2 -> {
                Column(modifier = Modifier.padding(PaddingDefaults.Medium)) {
                    ResponseEntry(entry = log)
                }
            }
        }

        SpacerMedium()
        TimingsEntry(
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
            entry = log
        )
        SpacerMedium()
    }
}

@Composable
private fun CopyButton(
    modifier: Modifier = Modifier,
    textToCopy: String
) {
    val context = LocalContext.current

    IconButton(
        modifier = modifier.padding(top = PaddingDefaults.Tiny),
        onClick = {
            ClipBoardCopy.copyToClipboard(context, textToCopy)
        }
    ) {
        Icon(
            tint = AppTheme.colors.neutral600,
            imageVector = Icons.Rounded.CopyAll,
            contentDescription = null
        )
    }
}

@LightDarkPreview()
@Composable
fun LoggerContentPreview() {
    val mockLogs = listOf(
        LogEntry(
            timestamp = "2025-03-26T12:00:00Z",
            request = RequestLog(
                method = "GET",
                url = "https://api.example.com/resource",
                headers = listOf(
                    HeaderLog("Authorization", "Bearer xxx"),
                    HeaderLog("Content-Type", "application/json")
                )
            ),
            response = ResponseLog(
                status = 200,
                statusText = "OK",
                headers = listOf(
                    HeaderLog("Content-Type", "application/json"),
                    HeaderLog("Cache-Control", "no-cache")
                ),
                content = ContentLog(
                    mimeType = "application/json",
                    text = """{"message":"Hello, World!"}"""
                )
            ),
            timings = TimingsLog(
                send = 1000L,
                wait = 15000000L,
                receive = 25000000L
            )
        ),
        LogEntry(
            timestamp = "2025-03-26T12:01:00Z",
            request = RequestLog(
                method = "POST",
                url = "https://api.example.com/submit",
                headers = listOf(
                    HeaderLog("Authorization", "Bearer xxx"),
                    HeaderLog("Content-Type", "application/json")
                )
            ),
            response = ResponseLog(
                status = 404,
                statusText = "Not Found",
                headers = listOf(
                    HeaderLog("Content-Type", "application/json")
                ),
                content = ContentLog(
                    mimeType = "application/json",
                    text = """{"error":"Resource not found"}"""
                )
            ),
            timings = TimingsLog(
                send = 2000L,
                wait = 5000000L,
                receive = 10000000L
            )
        )
    )

    val expandedItems = remember { mutableStateMapOf<Int, Boolean>().apply { this[0] = true } }

    PreviewAppTheme {
        LoggerContent(
            listState = rememberLazyListState(),
            expandedItems = expandedItems,
            httpLogs = mockLogs,
            isExpanded = { index ->
                expandedItems[index] = !(expandedItems[index] ?: false)
            }
        )
    }
}
