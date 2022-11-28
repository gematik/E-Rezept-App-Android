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

package de.gematik.ti.erp.app.cardwall.usecase

import android.content.Context
import android.os.Build
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.cardwall.usecase.model.NfcPositionUseCaseData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.InputStream

class CardWallLoadNfcPositionUseCase(
    private val context: Context
) {
    private val nfcPositions: List<NfcPositionUseCaseData.NfcPosition> by lazy {
        loadNfcPositionsFromJSON(
            context.resources.openRawResourceFd(R.raw.nfc_positions).createInputStream()
        ).sortedBy { it.marketingName.lowercase() }
    }

    fun findNfcPositionForPhone() = nfcPositions.find { it.modelNames.contains(Build.MODEL) }
}

private fun loadNfcPositionsFromJSON(jsonInput: InputStream): List<NfcPositionUseCaseData.NfcPosition> =
    Json.decodeFromString(jsonInput.bufferedReader().readText())
