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

package de.gematik.ti.erp.app.redeem.model

object ContactValidationRules {
    private const val MAX_TEXT_LENGTH = 50
    private const val MAX_HINT_TEXT_LENGTH = 500
    private const val MAX_PHONE_LENGTH = 25
    private const val MIN_PHONE_LENGTH = 4

    val TextRegex = Regex("[\\p{L}0-9\\-.,:!@_%+'/\"\\s]{1,$MAX_TEXT_LENGTH}")
    val HintRegex = Regex("[\\p{L}0-9\\-.,:!@_%+'/\"\\s]{1,$MAX_HINT_TEXT_LENGTH}")
    val PostalCodeRegex = Regex("^\\d{5}$")
    val PhoneRegex = Regex("[0-9\\-+'/\"\\s]{$MIN_PHONE_LENGTH,$MAX_PHONE_LENGTH}")
    val MailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{1,}\$")
}
