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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.card.model.command

import org.bouncycastle.asn1.BERTags
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

private const val CLA_COMMAND_CHAINING = 0x10
private const val CLA_NO_COMMAND_CHAINING = 0x00
private const val INS = 0x86
private const val NO_MEANING = 0x00

/**
 * Commands representing the General Authenticate commands in gemSpec_COS#14.7.2
 */

/**
 * UseCase: gemSpec_COS#14.7.2.1.1 PACE for end-user cards, Step 1 a
 *
 * @param commandChaining true for command chaining false if not
 */
fun HealthCardCommand.Companion.generalAuthenticate(commandChaining: Boolean) =
    HealthCardCommand(
        expectedStatus = generalAuthenticateStatus,
        cla = if (commandChaining) CLA_COMMAND_CHAINING else CLA_NO_COMMAND_CHAINING,
        ins = INS,
        p1 = NO_MEANING,
        p2 = NO_MEANING,
        data = DERTaggedObject(false, BERTags.APPLICATION, 28, DERSequence()).encoded,
        ne = NE_MAX_SHORT_LENGTH
    )

/**
 * UseCase: gemSpec_COS#14.7.2.1.1 PACE for end-user cards, Step 2a (tagNo 1), 3a (3) , 5a (5)
 *
 * @param commandChaining true for command chaining false if not
 * @param data byteArray with data
 */
fun HealthCardCommand.Companion.generalAuthenticate(
    commandChaining: Boolean,
    data: ByteArray,
    tagNo: Int
) =
    HealthCardCommand(
        expectedStatus = generalAuthenticateStatus,
        cla = if (commandChaining) CLA_COMMAND_CHAINING else CLA_NO_COMMAND_CHAINING,
        ins = INS,
        p1 = NO_MEANING,
        p2 = NO_MEANING,
        data = DERTaggedObject(
            true,
            BERTags.APPLICATION,
            28,
            DERTaggedObject(false, tagNo, DEROctetString(data))
        ).encoded,
        ne = NE_MAX_SHORT_LENGTH
    )
