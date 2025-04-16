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

package de.gematik.ti.erp.app.idp.usecase

import android.content.SharedPreferences
import androidx.core.content.edit

private const val EXT_AUTH_CODE_CHALLENGE: String = "EXT_AUTH_CODE_CHALLENGE"
private const val EXT_AUTH_CODE_VERIFIER: String = "EXT_AUTH_CODE_VERIFIER"
private const val EXT_AUTH_STATE: String = "EXT_AUTH_STATE"
private const val EXT_AUTH_NONCE: String = "EXT_AUTH_NONCE"
private const val EXT_AUTH_SCOPE: String = "EXT_AUTH_SCOPE"
private const val EXT_AUTH_ID: String = "EXT_AUTH_ID"
private const val EXT_AUTH_NAME: String = "EXT_AUTH_NAME"
private const val EXT_AUTH_PROFILE: String = "EXT_AUTH_PROFILE"

actual class IdpPreferenceProvider {
    lateinit var sharedPreferences: SharedPreferences

    actual var externalAuthenticationPreferences: ExternalAuthenticationPreferences
        get() = ExternalAuthenticationPreferences(
            extAuthCodeChallenge = sharedPreferences.getString(EXT_AUTH_CODE_CHALLENGE, null),
            extAuthCodeVerifier = sharedPreferences.getString(EXT_AUTH_CODE_VERIFIER, null),
            extAuthState = sharedPreferences.getString(EXT_AUTH_STATE, null),
            extAuthNonce = sharedPreferences.getString(EXT_AUTH_NONCE, null),
            extAuthId = sharedPreferences.getString(EXT_AUTH_ID, null),
            extAuthScope = sharedPreferences.getString(EXT_AUTH_SCOPE, null),
            extAuthName = sharedPreferences.getString(EXT_AUTH_NAME, null),
            extAuthProfile = sharedPreferences.getString(EXT_AUTH_PROFILE, null)
        )
        set(value) {
            sharedPreferences.edit(commit = true) {
                putString(EXT_AUTH_STATE, value.extAuthState)
                putString(EXT_AUTH_NONCE, value.extAuthNonce)
                putString(EXT_AUTH_CODE_VERIFIER, value.extAuthCodeVerifier)
                putString(EXT_AUTH_CODE_CHALLENGE, value.extAuthCodeChallenge)
                putString(EXT_AUTH_SCOPE, value.extAuthScope)
                putString(EXT_AUTH_ID, value.extAuthId)
                putString(EXT_AUTH_NAME, value.extAuthName)
                putString(EXT_AUTH_PROFILE, value.extAuthProfile)
            }
        }
}
