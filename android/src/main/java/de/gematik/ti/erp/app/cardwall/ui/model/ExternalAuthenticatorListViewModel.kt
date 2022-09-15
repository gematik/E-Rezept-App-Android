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

package de.gematik.ti.erp.app.cardwall.ui.model

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dagger.hilt.android.lifecycle.HiltViewModel
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.core.BaseViewModel
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ExternalAuthenticatorListViewModel @Inject constructor(
    private val idpUseCase: IdpUseCase,
    private val dispatchProvider: DispatchProvider
) : BaseViewModel() {

    suspend fun externalAuthenticatorIDList() = withContext(dispatchProvider.io()) {
        idpUseCase.downloadDiscoveryDocumentAndGetExternAuthenticatorIDs()
    }

    suspend fun startAuthorizationWithExternal(id: String): Uri =
        idpUseCase.getUniversalLinkForExternalAuthorization(id)
}
