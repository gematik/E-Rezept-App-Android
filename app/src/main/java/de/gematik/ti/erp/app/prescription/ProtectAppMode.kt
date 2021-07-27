/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.prescription

import android.content.SharedPreferences
import de.gematik.ti.erp.app.di.ApplicationPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val PROTECTION_LEVEL_NONE = "NONE"
private const val PROTECTION_LEVEL_PASSWORD = "PASSWORD"

private const val PROTECT_APP_MODE = "PROTECT_APP_MODE"

@Singleton
class ProtectAppMode @Inject constructor(
    @ApplicationPreferences
    private val appPrefs: SharedPreferences
) {
    var appProtected = MutableStateFlow(
        when (appPrefs.getString(PROTECT_APP_MODE, PROTECTION_LEVEL_NONE)) {
            PROTECTION_LEVEL_NONE -> false
            else -> true
        }
    )
        private set

    fun protectWithPassword() {
        // TODO just for demo purposes
        appPrefs.edit().putString(PROTECT_APP_MODE, PROTECTION_LEVEL_PASSWORD).apply()
        appProtected.value = true
    }

    fun removeProtection() {
        // TODO more handling
    }
}
