package de.gematik.ti.erp.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReusableContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import de.gematik.ti.erp.app.cardwall.AuthenticationUseCase
import de.gematik.ti.erp.app.common.App
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
import de.gematik.ti.erp.app.vau.di.vauModule
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import java.awt.event.MouseWheelListener
import javax.swing.UIManager
import kotlinx.coroutines.flow.collect
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

    Napier.base(DebugAntilog())
    singleWindowApplication(
        icon = BitmapPainter(useResource("images/erp_logo.webp", ::loadImageBitmap)),
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
                                MainScreen(mainViewModel, navigation)
                            }
                        }
                    }
                }
            }
        }
    }
}
