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

package de.gematik.ti.erp.app.messages.domain.model

import android.content.Context
import android.os.Build
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.utils.extensions.isGooglePlayServiceAvailable

// only null or negative ids, since positive ids are for changelogs
const val WELCOME_MESSAGE_ID = "0"
const val SECURITY_WARNING_MESSAGE_ID = "-1"

class InternalMessageResources(
    private val context: Context
) {
    val language = context.resources.configuration.locales[0].language
    val assets = context.assets
    val messageFrom = context.getString(R.string.internal_message_from)
    fun getMessageTag(version: String): String = context.getString(R.string.internal_message_tag, version)

    val welcomeMessage = context.getString(R.string.welcome_text)
    val welcomeMessageTag = context.getString(R.string.welcome_tag)

    // only show this on Android devices with access to google which are not safe anymore
    val shouldShowSecurityWarningMessage = context.isGooglePlayServiceAvailable() && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S
    val securityWarningMessage = context.getString(R.string.security_warning_text)
    val securityWarningTag = context.getString(R.string.security_warning_tag)
}
