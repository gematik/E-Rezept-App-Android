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

package de.gematik.ti.erp.app.appupdate.datasource.local

import android.content.SharedPreferences
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension

class AppUpdateManagerSelectionLocalDataSource(
    private val sharedPreferences: SharedPreferences
) {
    fun setAppUpdateManagerSelector(useOriginal: Boolean) {
        sharedPreferences.edit().putBoolean(APP_UPDATE_MANAGER_SELECTOR, useOriginal).apply()
    }

    fun getAppUpdateManagerSelector(): Boolean {
        return if (BuildConfigExtension.isReleaseMode) {
            true
        } else {
            sharedPreferences.getBoolean(APP_UPDATE_MANAGER_SELECTOR, true)
        }
    }

    companion object {
        const val APP_UPDATE_MANAGER_SELECTOR = "APP_UPDATE_MANAGER_SELECTOR"
    }
}
