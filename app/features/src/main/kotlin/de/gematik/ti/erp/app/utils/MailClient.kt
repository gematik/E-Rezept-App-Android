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

package de.gematik.ti.erp.app.utils

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.provideEmailIntent

fun openMailClient(
    context: Context,
    address: String,
    body: String,
    subject: String
) = context.handleIntent(
    provideEmailIntent(
        address = address,
        body = body,
        subject = subject
    )
)

@Suppress("MaxLineLength")
@Composable
fun buildFeedbackBodyWithDeviceInfo(
    title: String = stringResource(R.string.settings_feedback_mail_title),
    userHint: String = stringResource(R.string.seetings_feedback_form_additional_data_info),
    errorState: String? = null,
    darkMode: String,
    versionName: String,
    language: String,
    phoneModel: String,
    nfcInfo: String
): String = """$title
      |
      |
      |
      |$userHint
      |
      |Systeminformationen
      |
      |Betriebssystem: Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}) (PATCH ${Build.VERSION.SECURITY_PATCH})
      |Modell: $phoneModel
      |App Version: $versionName (${BuildKonfig.GIT_HASH})
      |DarkMode: $darkMode
      |Sprache: $language
      |FehlerStatus: ${errorState ?: ""}
      |NFC: $nfcInfo
      |
""".trimMargin()
