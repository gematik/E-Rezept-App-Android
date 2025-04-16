/*
 * Copyright 2025, gematik GmbH
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

@file:Suppress("ImplicitDefaultLocale")

package de.gematik.ti.erp.app.card.model.identifier

import org.bouncycastle.util.encoders.Hex

private const val AID_MIN_LENGTH = 5
private const val AID_MAX_LENGTH = 16

/**
 * An application identifier (AID) is used to address an application on the card
 */
class ApplicationIdentifier(aid: ByteArray) {
    val aid: ByteArray = aid.copyOf()
        get() =
            field.copyOf()

    init {
        require(!(aid.size < AID_MIN_LENGTH || aid.size > AID_MAX_LENGTH)) {
            // gemSpec_COS#N010.200
            String.format(
                "Application File Identifier length out of valid range [%d,%d]",
                AID_MIN_LENGTH,
                AID_MAX_LENGTH
            )
        }
    }

    constructor(hexAid: String) : this(Hex.decode(hexAid))
}
