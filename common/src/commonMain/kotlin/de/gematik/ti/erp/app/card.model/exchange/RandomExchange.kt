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

package de.gematik.ti.erp.app.card.model.exchange

import de.gematik.ti.erp.app.card.model.card.ICardChannel
import de.gematik.ti.erp.app.card.model.command.HealthCardCommand
import de.gematik.ti.erp.app.card.model.command.ResponseStatus
import de.gematik.ti.erp.app.card.model.command.executeSuccessfulOn
import de.gematik.ti.erp.app.card.model.command.getRandomValues
import de.gematik.ti.erp.app.card.model.command.select

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
