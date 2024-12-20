/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.redeem.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.redeem.model.DMCode
import de.gematik.ti.erp.app.utils.compose.DataMatrix
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.createBitMatrix

@Composable
fun DataMatrixCode(
    modifier: Modifier,
    code: DMCode
) {
    val matrix = remember(code) { createBitMatrix(code.payload) }

    DataMatrix(modifier, matrix, code.name)
}

@LightDarkPreview
@Composable
fun DataMatrixCodePreview() {
    DataMatrixCode(
        modifier = Modifier,
        code = DMCode(
            payload = "1213233647678679789790",
            nrOfCodes = 1,
            name = "Medication",
            selfPayerPrescriptionNames = listOf(),
            containsScanned = false
        )
    )
}
