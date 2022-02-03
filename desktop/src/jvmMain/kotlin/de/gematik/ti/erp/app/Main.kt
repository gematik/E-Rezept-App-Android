/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReusableContent
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import de.gematik.ti.erp.app.cardwall.AuthenticationUseCase
import de.gematik.ti.erp.app.common.App
import de.gematik.ti.erp.app.common.SpacerTiny
import de.gematik.ti.erp.app.common.theme.DesktopAppTheme
import de.gematik.ti.erp.app.communication.di.communicationModule
import de.gematik.ti.erp.app.core.DispatchersProvider
import de.gematik.ti.erp.app.fhir.FhirMapper
import de.gematik.ti.erp.app.idp.di.idpModule
import de.gematik.ti.erp.app.main.ui.MainNavigation
import de.gematik.ti.erp.app.main.ui.MainScreen
import de.gematik.ti.erp.app.main.ui.MainScreenViewModel
import de.gematik.ti.erp.app.navigation.ui.rememberNavigation
import de.gematik.ti.erp.app.network.di.networkModule
import de.gematik.ti.erp.app.prescription.di.prescriptionModule
import de.gematik.ti.erp.app.protocol.di.protocolModule
import de.gematik.ti.erp.app.vau.di.vauModule
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.awt.event.MouseWheelListener
import javax.swing.UIManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindSingleton
import org.kodein.di.bindings.UnboundedScope
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.subDI
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton
import java.util.logging.ConsoleHandler
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.SimpleFormatter

val BCProvider = BouncyCastleProvider()

val applicationScope = UnboundedScope()

@Composable
fun rememberScope(): UnboundedScope {
    val scope = remember { UnboundedScope() }
    DisposableEffect(Unit) {
        onDispose {
            scope.close()
        }
    }
    return scope
}

@Stable
class LogHandler : Handler() {
    init {
        formatter = SimpleFormatter()
    }

    private val buffer = MutableStateFlow<List<String>>(emptyList())

    @Stable
    val log: StateFlow<List<String>> = buffer

    override fun publish(record: LogRecord) {
        if (isLoggable(record)) {
            buffer.update {
                it + formatter.format(record)
            }
        }
    }

    override fun flush() {
    }

    override fun close() {
    }
}

val di = DI {
    import(vauModule)
    import(idpModule)
    import(networkModule)

    bindSingleton { object : DispatchersProvider {} }
    bindSingleton { AuthenticationUseCase(instance()) }
}

fun main() {
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (_: Exception) {
    }

    val logHandler by lazy { LogHandler() }
    if (BuildKonfig.INTERNAL) {
        Napier.base(
            DebugAntilog(
                handler = listOf(
                    ConsoleHandler().apply {
                        level = Level.ALL
                        formatter = SimpleFormatter()
                    },
                    logHandler
                )
            )
        )
    }

    singleWindowApplication(
        icon = BitmapPainter(
            useResource(
                if (BuildKonfig.INTERNAL) "images/erp_logo_dev.webp" else "images/erp_logo.webp",
                ::loadImageBitmap
            )
        ),
        title = "E-Rezept-Desktop",
        state = WindowState(width = 1200.dp, height = 800.dp)
    ) {
        val systemScale = LocalDensity.current.density
        val zoomRange = (systemScale / 1.5f)..(systemScale * 1.5f)

        App {
            MenuBar {
                val uriHandler = LocalUriHandler.current
                val infoText = App.strings.desktopMenuInfo()
                val dataText = App.strings.desktopMenuData()
                val dataLink = App.strings.desktopDataLink()
                val termsText = App.strings.desktopMenuTerms()
                val termsLink = App.strings.desktopTermsLink()
                Menu(infoText) {
                    Item(dataText) {
                        uriHandler.openUri(dataLink)
                    }
                    Item(termsText) {
                        uriHandler.openUri(termsLink)
                    }
                }
            }

            val systemDarkMode = isSystemInDarkTheme()

            // close the app scope linked to OkHttps thread pools
            DisposableEffect(Unit) {
                onDispose {
                    applicationScope.close()
                }
            }

            withDI(di) {
                val resourceScope = rememberScope()

                subDI(diBuilder = {
                    bind { scoped(resourceScope).singleton { FhirMapper(instance(), instance()) } }
                    importAll(prescriptionModule(resourceScope))
                    importAll(communicationModule(resourceScope))
                    importAll(protocolModule(resourceScope))
                    bind { scoped(resourceScope).singleton { DownloadUseCase(instance(), instance()) } }
                    bindSingleton {
                        MainScreenViewModel(
                            zoomRange = zoomRange,
                            defaultZoom = systemScale,
                            defaultDarkMode = systemDarkMode
                        )
                    }
                }) {
                    val navigation = rememberNavigation(MainNavigation.Welcome)

                    val mainViewModel by rememberInstance<MainScreenViewModel>()
                    val mainState by produceState(mainViewModel.defaultState) {
                        mainViewModel.screenState().collect {
                            value = it
                        }
                    }

                    LaunchedEffect(Unit) {
                        mainViewModel.logout.collect {
                            // close the scope manually
                            resourceScope.close()

                            navigation.navigate(MainNavigation.Welcome, clearBackStack = true)
                        }
                    }

                    DisposableEffect(Unit) {
                        val l = MouseWheelListener { e ->
                            if (e.isControlDown) {
                                mainViewModel.onZoom(if (e.wheelRotation < 0) 0.1f else -0.1f)
                            }
                        }
                        window.addMouseWheelListener(l)
                        onDispose {
                            window.removeMouseWheelListener(l)
                        }
                    }

                    ReusableContent(mainState.zoom) {
                        CompositionLocalProvider(
                            LocalDensity provides Density(density = mainState.zoom, fontScale = 1f)
                        ) {
                            DesktopAppTheme(darkTheme = mainState.darkMode) {
                                if (BuildKonfig.INTERNAL) {
                                    Box {
                                        MainScreen(mainViewModel, navigation)

                                        var showLoggingWindow by remember { mutableStateOf(false) }
                                        Row(modifier = Modifier.padding(8.dp).align(Alignment.BottomEnd), verticalAlignment = Alignment.CenterVertically) {
                                            Text(BuildKonfig.BUILD_FLAVOR, style = MaterialTheme.typography.caption)
                                            SpacerTiny()
                                            IconButton(
                                                onClick = { showLoggingWindow = true }
                                            ) {
                                                Icon(Icons.Rounded.BugReport, null, tint = Color.Red)
                                            }
                                        }

                                        if (showLoggingWindow) {
                                            LoggingWindow(
                                                logHandler,
                                                onCloseRequest = { showLoggingWindow = false }
                                            )
                                        }
                                    }
                                } else {
                                    MainScreen(mainViewModel, navigation)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoggingWindow(
    logHandler: LogHandler,
    onCloseRequest: () -> Unit
) {
    Window(
        onCloseRequest = onCloseRequest
    ) {
        val logs by logHandler.log.collectAsState()

        val clipboardManager = LocalClipboardManager.current

        Column {
            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(logs.joinToString("\n\n")))
                }
            ) {
                Text("Copy All")
            }

            if (logs.isNotEmpty()) {
                SelectionContainer(Modifier.fillMaxSize()) {
                    val state = rememberLazyListState()
                    Row {
                        LazyColumn(
                            Modifier.weight(1f),
                            state = state
                        ) {
                            items(items = logs) { log ->
                                Text(log)
                            }
                        }
                        VerticalScrollbar(rememberScrollbarAdapter(state))
                    }
                }
            }
        }
    }
}
