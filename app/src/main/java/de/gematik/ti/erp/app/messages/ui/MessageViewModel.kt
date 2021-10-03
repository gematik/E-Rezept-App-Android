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

package de.gematik.ti.erp.app.messages.ui

import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.datamatrix.DataMatrixWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.core.BaseViewModel
import de.gematik.ti.erp.app.db.entities.CommunicationProfile
import de.gematik.ti.erp.app.messages.ui.models.CommunicationReply
import de.gematik.ti.erp.app.messages.usecase.MessageUseCase
import de.gematik.ti.erp.app.redeem.ui.BitMatrixCode
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val useCase: MessageUseCase,
    private val dispatchProvider: DispatchProvider
) : BaseViewModel() {
    fun fetchCommunications() =
        useCase.loadCommunicationsLocally(CommunicationProfile.ErxCommunicationReply)

    fun createBitmapMatrix(payload: String) =
        BitMatrixCode(DataMatrixWriter().encode(payload, BarcodeFormat.DATA_MATRIX, 1, 1))

    fun messageAcknowledged(message: CommunicationReply) {
        viewModelScope.launch(dispatchProvider.main()) {
            useCase.updateCommunicationResource(message.communicationId, message.consumed)
        }
    }
}
