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

package de.gematik.ti.erp.app.pharmacy.ui.components

import com.google.i18n.phonenumbers.PhoneNumberUtil
import de.gematik.ti.erp.app.eurezept.mapper.countryCodeToFlag
import de.gematik.ti.erp.app.eurezept.mapper.countryCodeToName

data class CountryCallingCode(
    val countryCode: String,
    val countryName: String,
    val callingCode: Int,
    val flagEmoji: String
) {
    val displayCallingCode: String
        get() = "+$callingCode"
}

fun getAvailableCountryCallingCodes(): List<CountryCallingCode> {
    val phoneUtil = PhoneNumberUtil.getInstance()
    val priorityIsoCodes = listOf("DE", "AT", "CH")
    val supportedRegions = phoneUtil.supportedRegions

    val priorityList = priorityIsoCodes.mapNotNull { code ->
        val callingCode = phoneUtil.getCountryCodeForRegion(code)
        if (callingCode > 0) {
            CountryCallingCode(
                countryCode = code,
                countryName = countryCodeToName(code),
                callingCode = callingCode,
                flagEmoji = countryCodeToFlag(code)
            )
        } else {
            null
        }
    }

    val otherList = (supportedRegions - priorityIsoCodes.toSet())
        .mapNotNull { code ->
            val callingCode = phoneUtil.getCountryCodeForRegion(code)
            if (callingCode > 0) {
                CountryCallingCode(
                    countryCode = code,
                    countryName = countryCodeToName(code),
                    callingCode = callingCode,
                    flagEmoji = countryCodeToFlag(code)
                )
            } else {
                null
            }
        }
        .filter { it.countryName.isNotBlank() }
        .sortedBy { it.countryName }

    return priorityList + otherList
}

fun parsePhoneToCallingCodeAndNationalNumber(
    phone: String,
    allCallingCodes: List<CountryCallingCode>,
    defaultCountryCode: String = "DE"
): Pair<CountryCallingCode, String> {
    val defaultCode = allCallingCodes.find { it.countryCode.equals(defaultCountryCode, ignoreCase = true) }
        ?: allCallingCodes.find { it.countryCode == "DE" }
        ?: CountryCallingCode("DE", countryCodeToName("DE"), 49, "🇩🇪")

    if (phone.isBlank()) {
        return defaultCode to ""
    }

    val trimmed = phone.trim()
    if (trimmed.startsWith("+")) {
        val phoneUtil = PhoneNumberUtil.getInstance()
        try {
            val parsed = phoneUtil.parse(trimmed, null)
            val callingCode = parsed.countryCode
            val region = phoneUtil.getRegionCodeForCountryCode(callingCode)
            val matched = allCallingCodes.find { it.countryCode.equals(defaultCountryCode, ignoreCase = true) && it.callingCode == callingCode }
                ?: allCallingCodes.find { it.countryCode == region && it.callingCode == callingCode }
                ?: allCallingCodes.find { it.callingCode == callingCode }
                ?: defaultCode
            val nationalNumber = trimmed.removePrefix("+$callingCode").trim()
            return matched to nationalNumber
        } catch (_: Exception) {
            val matched = if (trimmed.startsWith(defaultCode.displayCallingCode)) {
                defaultCode
            } else {
                allCallingCodes
                    .sortedByDescending { it.callingCode.toString().length }
                    .firstOrNull { trimmed.startsWith(it.displayCallingCode) }
            }
            if (matched != null) {
                val nationalNumber = trimmed.removePrefix(matched.displayCallingCode).trim()
                return matched to nationalNumber
            }
        }
    } else {
        val nationalNumber = trimmed.removePrefix("0").trim()
        return defaultCode to nationalNumber
    }

    return defaultCode to trimmed.removePrefix("+")
}
