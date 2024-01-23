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

package de.gematik.ti.erp.app.demomode.mapper.authentication

import de.gematik.ti.erp.app.authentication.mapper.PromptAuthenticationProvider
import de.gematik.ti.erp.app.authentication.model.InitialAuthenticationData
import de.gematik.ti.erp.app.authentication.model.PromptAuthenticator
import de.gematik.ti.erp.app.authentication.model.PromptAuthenticator.AuthResult.Authenticated
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class DemoPromptAuthenticationProvider : PromptAuthenticationProvider {
    override fun mapAuthenticationResult(
        id: ProfileIdentifier,
        initialAuthenticationData: InitialAuthenticationData,
        scope: PromptAuthenticator.AuthScope,
        authenticators: List<PromptAuthenticator>
    ): Flow<PromptAuthenticator.AuthResult> = flowOf(Authenticated)
}
