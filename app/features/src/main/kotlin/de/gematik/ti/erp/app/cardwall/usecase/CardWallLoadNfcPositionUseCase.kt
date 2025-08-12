/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.cardwall.usecase

import android.content.Context
import android.os.Build
import de.gematik.ti.erp.app.cardwall.usecase.model.NfcPositionUseCaseData
import de.gematik.ti.erp.app.core.R
import kotlinx.serialization.json.Json
import java.io.InputStream

class CardWallLoadNfcPositionUseCase(
    private val context: Context
) {
    private val nfcPositions: List<NfcPositionUseCaseData.NfcData> by lazy {
        loadNfcPositionsFromJSON(
            context.resources.openRawResourceFd(R.raw.nfc_positions).createInputStream()
        ).sortedBy { it.marketingName.lowercase() }
    }

    fun findNfcPositionForPhone() = nfcPositions.find { it.modelNames.contains(Build.MODEL) }
}

private fun loadNfcPositionsFromJSON(jsonInput: InputStream): List<NfcPositionUseCaseData.NfcData> =
    Json.decodeFromString(jsonInput.bufferedReader().readText())
