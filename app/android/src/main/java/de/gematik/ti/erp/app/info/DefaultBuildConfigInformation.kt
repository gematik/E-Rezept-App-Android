/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.info

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import de.gematik.ti.erp.app.BuildConfig
import de.gematik.ti.erp.app.cardwall.usecase.deviceHasNFC
import java.util.Locale

class DefaultBuildConfigInformation : BuildConfigInformation {
    override fun versionName(): String = BuildConfig.VERSION_NAME
    override fun versionCode(): String = "${BuildConfig.VERSION_CODE}"
    override fun model(): String = "${Build.MANUFACTURER} ${Build.MODEL} (${Build.PRODUCT})"
    override fun language(): String = Locale.getDefault().displayName

    @Composable
    override fun inDarkTheme(): String = if (isSystemInDarkTheme()) DARK_THEME_ON else DARK_THEME_OFF
    override fun nfcInformation(context: Context): String =
        if (context.deviceHasNFC()) NFC_AVAILABLE else NFC_NOT_AVAILABLE

    companion object {
        private const val DARK_THEME_ON = "an"
        private const val DARK_THEME_OFF = "aus"
        private const val NFC_AVAILABLE = "vorhanden"
        private const val NFC_NOT_AVAILABLE = "nicht vorhanden"
    }
}
