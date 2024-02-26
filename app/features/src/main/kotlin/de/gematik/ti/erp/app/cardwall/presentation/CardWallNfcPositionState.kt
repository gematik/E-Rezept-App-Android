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

package de.gematik.ti.erp.app.cardwall.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.cardwall.usecase.CardWallLoadNfcPositionUseCase
import de.gematik.ti.erp.app.cardwall.usecase.model.NfcPositionUseCaseData
import org.kodein.di.compose.rememberInstance

class CardWallNfcPositionState(
    nfcPositionUseCase: CardWallLoadNfcPositionUseCase
) {
    private val findNfc = nfcPositionUseCase.findNfcPositionForPhone()

    val state = findNfc?.let {
        CardWallNfcPositionStateData.State(findNfc)
    } ?: CardWallNfcPositionStateData.defaultState
}

@Composable
fun rememberCardWallNfcPositionState(): CardWallNfcPositionState {
    val nfcPositionUseCase by rememberInstance<CardWallLoadNfcPositionUseCase>()

    return remember {
        CardWallNfcPositionState(nfcPositionUseCase)
    }
}

object CardWallNfcPositionStateData {
    @Immutable
    data class State(
        val nfcData: NfcPositionUseCaseData.NfcData
    )

    val defaultState = State(
        NfcPositionUseCaseData.NfcData(
            manufacturer = "",
            marketingName = "",
            modelNames = emptyList(),
            nfcPos = NfcPositionUseCaseData.NfcPos(
                x0 = 0.5,
                y0 = 0.3,
                x1 = 0.5,
                y1 = 0.3
            )
        )
    )
}
