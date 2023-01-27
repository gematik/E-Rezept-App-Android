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

package de.gematik.ti.erp.app.card.model.exchange

import de.gematik.ti.erp.app.card.model.card.ICardChannel
import de.gematik.ti.erp.app.card.model.cardobjects.Df
import de.gematik.ti.erp.app.card.model.cardobjects.Mf
import de.gematik.ti.erp.app.card.model.command.EXPECTED_LENGTH_WILDCARD_EXTENDED
import de.gematik.ti.erp.app.card.model.command.HealthCardCommand
import de.gematik.ti.erp.app.card.model.command.ResponseStatus
import de.gematik.ti.erp.app.card.model.command.executeSuccessfulOn
import de.gematik.ti.erp.app.card.model.command.read
import de.gematik.ti.erp.app.card.model.command.select
import de.gematik.ti.erp.app.card.model.identifier.ApplicationIdentifier
import de.gematik.ti.erp.app.card.model.identifier.FileIdentifier
import java.io.ByteArrayOutputStream

fun ICardChannel.retrieveCertificate(): ByteArray {
    HealthCardCommand.select(ApplicationIdentifier(Df.Esign.AID)).executeSuccessfulOn(this)
    HealthCardCommand.select(
        FileIdentifier(Mf.Df.Esign.Ef.CchAutE256.FID),
        selectDfElseEf = false,
        requestFcp = true,
        fcpLength = EXPECTED_LENGTH_WILDCARD_EXTENDED
    ).executeSuccessfulOn(this)

    val buffer = ByteArrayOutputStream()
    var offset = 0
    while (true) {
        val response = HealthCardCommand.read(offset)
            .executeOn(this)

        val data = response.apdu.data

        if (data.isNotEmpty()) {
            buffer.write(data)
            offset += data.size
        }

        when (response.status) {
            ResponseStatus.SUCCESS -> { }
            ResponseStatus.END_OF_FILE_WARNING,
            ResponseStatus.OFFSET_TOO_BIG -> break
            else -> error("Couldn't read certificate: ${response.status}")
        }
    }

    return buffer.toByteArray()
}
