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

package de.gematik.ti.erp.app.cardwall.model.nfc.card

import de.gematik.ti.erp.app.card.model.card.ICardKeyReference

/**
 * A password can be a regular password or multireference password
 *
 * * A "regular password" is used to store a secret, which is usually only known to one cardholder. The COS will allow certain services only if this secret has been successfully presented as part of a user verification. The need for user verification can be turned on (enable) or turned off (disable).
 * * A multireference password allows the use of a secret, which is stored as an at-tributary in a regular password (see (N015.200)), but under conditions that deviate from those of the regular password.
 *
 * @see "gemSpec_COS 'Spezifikation des Card Operating System'"
 */

private const val MIN_PWD_ID = 0
private const val MAX_PWD_ID = 31

class PasswordReference(val pwdId: Int) : ICardKeyReference {
    init {
        require(!(pwdId < MIN_PWD_ID || pwdId > MAX_PWD_ID)) {
            // gemSpec_COS#N015.000
            "Password ID out of range [$MIN_PWD_ID,$MAX_PWD_ID]"
        }
    }

    // gemSpec_COS#N072.800
    override fun calculateKeyReference(dfSpecific: Boolean): Int =
        pwdId + if (dfSpecific) {
            ICardKeyReference.DF_SPECIFIC_PWD_MARKER
        } else {
            0
        }
}
