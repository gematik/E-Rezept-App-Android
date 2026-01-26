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

package de.gematik.ti.erp.app.preview

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import de.gematik.ti.erp.app.theme.AppTheme
import kotlinx.datetime.TimeZone
import java.util.Locale

@Composable
fun PreviewTheme(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val baseConfig = LocalConfiguration.current

    val configWithLocale = Configuration(baseConfig).apply {
        setLocales(LocaleList(Locale.ENGLISH))
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr,
        LocalConfiguration provides configWithLocale,
        LocalTimeZone provides TimeZone.of("Europe/Berlin")
    ) {
        AppTheme {
            Surface(
                modifier = modifier
            ) {
                content()
            }
        }
    }
}

private val LocalTimeZone = staticCompositionLocalOf<TimeZone> { error("No Timezone provided!") }
