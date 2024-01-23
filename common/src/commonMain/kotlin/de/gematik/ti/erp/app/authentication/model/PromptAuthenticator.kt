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

package de.gematik.ti.erp.app.authentication.model

import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow

@Stable
interface PromptAuthenticator {
    enum class AuthResult {
        Authenticated,
        Cancelled,
        NoneEnrolled,
        UserNotAuthenticated,
        RedirectLinkNotRight
    }

    enum class AuthScope {
        Prescriptions, PairedDevices
    }

    fun authenticate(profileId: ProfileIdentifier, scope: AuthScope): Flow<AuthResult>

    suspend fun cancelAuthentication()
}
