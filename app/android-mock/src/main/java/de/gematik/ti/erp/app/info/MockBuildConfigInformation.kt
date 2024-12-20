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

package de.gematik.ti.erp.app.info

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import de.gematik.ti.erp.app.mock.BuildConfig
import java.util.Locale

private const val DARK_THEME_ON = "an"
private const val DARK_THEME_OFF = "aus"

class MockBuildConfigInformation : BuildConfigInformation {

    override fun versionName(): String = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

    override fun versionCode(): String = "${BuildConfig.VERSION_CODE}"
    override fun model(): String = "${Build.MANUFACTURER} ${Build.MODEL} (${Build.PRODUCT})"
    override fun language(): String = Locale.getDefault().displayName

    @Composable
    override fun inDarkTheme(): String = if (isSystemInDarkTheme()) DARK_THEME_ON else DARK_THEME_OFF
    override fun nfcInformation(context: Context): String = "nicht vorhanden"
    override fun isMockedApp(): Boolean = true
}
