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
import de.gematik.ti.erp.app.mock.BuildConfig
import java.util.Locale

private const val DARK_THEME_ON = "an"
private const val DARK_THEME_OFF = "aus"
private const val RELEASE_CANDIDATE = "RC"
private const val SEPARATOR = "-"
private const val SPACE = " "

class MockBuildConfigInformation : BuildConfigInformation {

    override fun versionName(): String {
        BuildConfig.VERSION_NAME.split(RELEASE_CANDIDATE)
            .takeIf {
                it.size > 1
            }?.let { splits ->
                val secondSplit = splits[1].split(SEPARATOR)
                if (secondSplit.size > 1) {
                    // Removes the R from R1.20.23 and makes it 1.20.23
                    val tag = splits[0].drop(1)
                    // Takes a 6z&hj4f58dzf9j0890hfj4938that509z97h and makes it 6z&hj4f58
                    val truncatedCommitHash = secondSplit[1].take(9)
                    // RC-1 or RC-2
                    val releaseCandidateNumber = secondSplit[0]
                    return listOf(
                        tag,
                        RELEASE_CANDIDATE,
                        SEPARATOR,
                        releaseCandidateNumber,
                        SEPARATOR,
                        truncatedCommitHash,
                        SPACE,
                        "(${BuildConfig.VERSION_CODE})"
                    ).joinToString(separator = "")
                }
            }
        return "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    }

    override fun versionCode(): String = "${BuildConfig.VERSION_CODE}"
    override fun model(): String = "${Build.MANUFACTURER} ${Build.MODEL} (${Build.PRODUCT})"
    override fun language(): String = Locale.getDefault().displayName

    @Composable
    override fun inDarkTheme(): String = if (isSystemInDarkTheme()) DARK_THEME_ON else DARK_THEME_OFF
    override fun nfcInformation(context: Context): String = "nicht vorhanden"
}
