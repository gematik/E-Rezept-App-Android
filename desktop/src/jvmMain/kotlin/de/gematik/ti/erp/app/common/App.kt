package de.gematik.ti.erp.app.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import de.gematik.ti.erp.app.common.strings.LocalStrings
import de.gematik.ti.erp.app.common.strings.Strings
import de.gematik.ti.erp.app.common.strings.getStrings
import java.util.Locale

@Composable
fun App(locale: Locale = Locale.getDefault(), content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalStrings provides getStrings(locale),
        content = content
    )
}

object App {
    val strings: Strings
        @Composable
        get() = LocalStrings.current
}
