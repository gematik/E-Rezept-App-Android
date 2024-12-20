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

package de.gematik.ti.erp.app.debugsettings.logger.ui.screens

import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.base.ClipBoardCopy
import de.gematik.ti.erp.app.debugsettings.logger.presentation.rememberLoggerScreenController
import de.gematik.ti.erp.app.logger.mapper.toHar
import de.gematik.ti.erp.app.logger.mapper.toJson
import de.gematik.ti.erp.app.logger.model.ContentLog
import de.gematik.ti.erp.app.logger.model.HeaderLog
import de.gematik.ti.erp.app.logger.model.LogEntry
import de.gematik.ti.erp.app.logger.model.RequestLog
import de.gematik.ti.erp.app.logger.model.ResponseLog
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
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import de.gematik.ti.erp.app.utils.extensions.show
import de.gematik.ti.erp.app.utils.extensions.showWithDismissButton
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

object LoggerScreen {

    @Composable
    fun Content(
        onBack: () -> Unit
    ) {
        val context = LocalContext.current
        val snackbar = LocalSnackbarScaffold.current
        val scope = rememberCoroutineScope()

        val listState = rememberLazyListState()
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
                        onClick = {
                            val logs = httpLogs.toHar().toJson()
                            ClipBoardCopy.copyToClipboard(
                                context = context,
                                text = logs
                            )
                        }
                    ) {
                        Text("Copy")
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
                    httpLogs = httpLogs
                )
            }
        )
    }

    @Composable
    private fun LoggerContent(
        listState: LazyListState,
        httpLogs: List<LogEntry>
    ) {
        if (httpLogs.isEmpty()) {
            ErrorScreenComponent()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState
            ) {
                itemsIndexed(httpLogs) { _, log ->
                    Log(log)
                    Divider()
                }
                item {
                    SpacerLarge()
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

@Composable
private fun TimeStampEntry(entry: LogEntry) {
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

@Composable
private fun RequestEntry(
    entry: LogEntry
) {
    Text(
        text = "Request",
        style = AppTheme.typography.subtitle1,
        fontWeight = FontWeight.Bold
    )
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

@Composable
private fun ResponseEntry(entry: LogEntry) {
    Text(
        text = "Response",
        style = AppTheme.typography.subtitle1,
        fontWeight = FontWeight.Bold
    )
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
        text = entry.response.content.text,
        style = AppTheme.typography.caption1
    )
}

@Composable
private fun TimingsEntry(
    entry: LogEntry
) {
    Text(
        text = "Timings",
        style = AppTheme.typography.subtitle1,
        fontWeight = FontWeight.Bold
    )
    SpacerTiny()
    Text(
        text = "sent at ${entry.timings.send}",
        style = AppTheme.typography.caption1
    )
    Text(
        text = "wait time ${entry.timings.wait}",
        style = AppTheme.typography.caption1
    )
    Text(
        text = "received at ${entry.timings.receive}",
        style = AppTheme.typography.caption1
    )
}

@Composable
private fun Log(
    entry: LogEntry
) {
    Column(
        modifier = Modifier.padding(PaddingDefaults.Medium)
    ) {
        TimeStampEntry(entry = entry)
        SpacerMedium()
        RequestEntry(entry = entry)
        SpacerMedium()
        ResponseEntry(entry = entry)
        SpacerMedium()
        TimingsEntry(entry = entry)
        SpacerMedium()
    }
}

@LightDarkPreview
@Composable
fun LoggerScreenPreview() {
    PreviewAppTheme {
        Log(
            entry = LogEntry(
                timestamp = "2021-09-01T12:00:00",
                request = RequestLog(
                    method = "GET",
                    url = "https://example.com",
                    headers = listOf(
                        HeaderLog(
                            name = "req header1",
                            value = "value1"
                        ),
                        HeaderLog(
                            name = "req header2",
                            value = "value2"
                        )
                    )
                ),
                response = ResponseLog(
                    status = 200,
                    statusText = "OK",
                    headers = listOf(
                        HeaderLog(
                            name = "header1",
                            value = "value1"
                        ),
                        HeaderLog(
                            name = "header2",
                            value = "value2"
                        )
                    ),
                    content = ContentLog(
                        mimeType = "text/plain",
                        text = "response text"
                    )
                ),
                timings = TimingsLog(
                    send = 1000,
                    wait = 2000,
                    receive = 3000
                )
            )
        )
    }
}
