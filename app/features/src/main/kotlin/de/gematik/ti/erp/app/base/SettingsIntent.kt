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

package de.gematik.ti.erp.app.base

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Open settings as new activity
 *
 * @param action: String. This specifies the action to be performed by the intent.
 * @param isSimpleIntent: Boolean. Use [isSimpleIntent] = true if your call is something like
 * context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
 */
fun Context.openSettingsAsNewActivity(
    action: String,
    isSimpleIntent: Boolean = false
) {
    try {
        if (isSimpleIntent) {
            val intent = Intent(action)
            ContextCompat.startActivity(this, intent, null)
        } else {
            val uri = Uri.fromParts("package", packageName, null)
            val intent = Intent(action)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setData(uri)
            ContextCompat.startActivity(this, intent, null)
        }
    } catch (e: ActivityNotFoundException) {
        // General settings intent
        val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ContextCompat.startActivity(this, intent, null)
    }
}
