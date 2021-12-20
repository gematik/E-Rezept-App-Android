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
