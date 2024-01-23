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

package de.gematik.ti.erp.app.settings.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import de.gematik.ti.erp.app.BuildKonfig

private const val FAKE_NFC_CAPABILITIES = "fake_nfc_capabilities"

class CardWallRepository(
    private val prefs: SharedPreferences
) {
    var hasFakeNFCEnabled: Boolean
        get() =
            if (BuildKonfig.INTERNAL) {
                prefs.getBoolean(FAKE_NFC_CAPABILITIES, false)
            } else {
                false
            }
        set(value) = prefs.edit { putBoolean(FAKE_NFC_CAPABILITIES, value) }
}
