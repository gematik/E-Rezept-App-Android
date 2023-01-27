/*
 * Copyright (c) 2023 gematik GmbH
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

import de.gematik.ti.erp.app.cardwall.ui.model.CardWallNfcPositionViewModelData
import de.gematik.ti.erp.app.cardwall.usecase.CardWallLoadNfcPositionUseCase
import androidx.lifecycle.ViewModel

class CardWallNfcPositionViewModel(
    private val nfcPositionUseCase: CardWallLoadNfcPositionUseCase
) : ViewModel() {
    val defaultState = CardWallNfcPositionViewModelData.NfcPosition()

    private val findNfc = nfcPositionUseCase.findNfcPositionForPhone()

    fun screenState() = findNfc?.let { CardWallNfcPositionViewModelData.NfcPosition(findNfc) } ?: defaultState
}
