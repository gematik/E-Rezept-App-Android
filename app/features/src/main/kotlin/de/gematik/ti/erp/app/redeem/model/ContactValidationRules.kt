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

import com.google.i18n.phonenumbers.PhoneNumberUtil

object ContactValidationRules {
    const val MAX_TEXT_LENGTH = 100
    const val MAX_NAME_PART_LENGTH = 45
    const val MAX_HINT_TEXT_LENGTH = 100
    const val MAX_PHONE_LENGTH = 32
    const val MAX_MAIL_LENGTH = 70

    const val MIN_STREET_LENGTH = 3
    const val MAX_STREET_LENGTH = 100

    const val MIN_CITY_LENGTH = 2
    const val MAX_CITY_LENGTH = 100

    const val MIN_POSTAL_CODE_LENGTH = 3
    const val MAX_POSTAL_CODE_LENGTH = 10

    val TextRegex = Regex("[\\p{L}0-9\\-.,:!@_%+'/\"\\s]{1,$MAX_TEXT_LENGTH}")
    val StreetRegex = Regex("[\\p{L}0-9\\-.,:!@_%+'/\"\\s]{$MIN_STREET_LENGTH,$MAX_STREET_LENGTH}")
    val CityRegex = Regex("[\\p{L}0-9\\-.,:!@_%+'/\"\\s]{$MIN_CITY_LENGTH,$MAX_CITY_LENGTH}")
    val NamePartRegex = Regex("^[\\p{L}0-9\\-.,'\\s]{1,$MAX_NAME_PART_LENGTH}$")
    val CountryRegex = Regex("^[A-Z]{2}$")
    val HintRegex = Regex("[\\p{L}0-9\\-.,:!@_%+'/\"\\s]{1,$MAX_HINT_TEXT_LENGTH}")
    val PostalCodeRegex = Regex("^[a-zA-Z0-9\\s\\-]{$MIN_POSTAL_CODE_LENGTH,$MAX_POSTAL_CODE_LENGTH}$")
    val PhoneRegex = Regex("^\\+[1-9][0-9\\-+'/\"\\s]{3,31}$")
    val MailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{1,}\$")

    fun isValidE164Phone(phone: String): Boolean {
        val cleanPhone = phone.filterNot { it.isWhitespace() }
        if (cleanPhone.isBlank() || cleanPhone.length > MAX_PHONE_LENGTH) return false
        if (!cleanPhone.matches(PhoneRegex)) return false
        return try {
            val phoneUtil = PhoneNumberUtil.getInstance()
            val parsed = phoneUtil.parse(cleanPhone, null)
            phoneUtil.isPossibleNumber(parsed) || cleanPhone.matches(PhoneRegex)
        } catch (_: Exception) {
            cleanPhone.matches(PhoneRegex)
        }
    }
}
