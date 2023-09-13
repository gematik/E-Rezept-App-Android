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

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.card.model.card.ICardChannel
import de.gematik.ti.erp.app.card.model.command.HealthCardCommand
import de.gematik.ti.erp.app.card.model.command.ResponseStatus
import de.gematik.ti.erp.app.card.model.command.executeSuccessfulOn
import de.gematik.ti.erp.app.card.model.command.getRandomValues
import de.gematik.ti.erp.app.card.model.command.select

@Requirement(
    "GS-A_4367#4",
    "GS-A_4368#4",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "Random numbers are generated using the RNG of the health card." +
        "This generator fulfills BSI-TR-03116#3.4 PTG.2 required by gemSpec_COS#14.9.5.1"
)
fun ICardChannel.getRandom(length: Int): ByteArray {
    HealthCardCommand.select(selectParentElseRoot = false, readFirst = false)
        .executeSuccessfulOn(this)

    while (true) {
        val response =
            HealthCardCommand.getRandomValues(
                length = length
            ).executeOn(this)

        require(
            when (response.status) {
                ResponseStatus.SUCCESS,
                ResponseStatus.SECURITY_STATUS_NOT_SATISFIED ->
                    true
                else ->
                    false
            }
        ) { "Get Random command failed with status: ${response.status}" }

        return response.apdu.data
    }
}
