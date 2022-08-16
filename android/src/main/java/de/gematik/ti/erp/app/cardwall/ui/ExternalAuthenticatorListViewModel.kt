/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.cardwall.ui

import de.gematik.ti.erp.app.DispatchProvider
import androidx.lifecycle.ViewModel
import de.gematik.ti.erp.app.idp.api.models.AuthenticationId
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.withContext
import java.net.URI

class ExternalAuthenticatorListViewModel(
    private val idpUseCase: IdpUseCase,
    private val dispatchers: DispatchProvider
) : ViewModel() {

    suspend fun externalAuthenticatorIDList() = withContext(dispatchers.IO) {
        idpUseCase.loadExternAuthenticatorIDs()
    }

    suspend fun startAuthorizationWithExternal(profileId: ProfileIdentifier, auth: AuthenticationId): URI =
        idpUseCase.getUniversalLinkForExternalAuthorization(
            profileId = profileId,
            authenticatorId = auth.id,
            authenticatorName = auth.name
        )
}
