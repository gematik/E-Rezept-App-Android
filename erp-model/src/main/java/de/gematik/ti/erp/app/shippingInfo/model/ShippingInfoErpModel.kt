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

package de.gematik.ti.erp.app.shippingInfo.model

import de.gematik.ti.erp.app.fhir.constant.SafeJson
import kotlinx.serialization.Serializable

@Serializable
data class ShippingInfoErpModel(
    val name: String = "",
    val mail: String = "",
    val phone: String = "",
    val street: String = "",
    val addressDetail: String = "", // e.g., Apt/Floor/Company
    val city: String = "",
    val zip: String = "",
    val deliveryInfo: String = "",
    val firstname: String = "",
    val lastname: String = "",
    val country: String = ""
) {

    fun resolvedFirstname(): String = firstname.ifBlank {
        splitFullName(name).first
    }

    fun resolvedLastname(): String = lastname.ifBlank {
        splitFullName(name).second
    }

    fun resolvedName(): String = when {
        name.isNotBlank() -> name
        firstname.isNotBlank() || lastname.isNotBlank() ->
            listOf(firstname, lastname).filter { it.isNotBlank() }.joinToString(" ")

        else -> ""
    }

    fun address() = listOf(
        street,
        addressDetail,
        zip,
        city
    ).filter { it.isNotBlank() }

    fun other() = listOf(
        phone,
        mail,
        deliveryInfo
    ).filter { it.isNotBlank() }

    fun isEmpty() = address().isEmpty() && other().isEmpty()

    companion object {
        /**
         * Splits a full name into (firstname, lastname) on the *first* whitespace, e.g.:
         * "Hans muller schmidth" -> ("Hans", "muller schmidth")
         * "Erika Mustermann" -> ("Erika", "Mustermann")
         * "Cher" -> ("Cher", "")
         */
        fun splitFullName(name: String): Pair<String, String> {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return "" to ""
            val parts = trimmed.split(Regex("\\s+"), limit = 2)
            val first = parts.firstOrNull().orEmpty()
            val last = if (parts.size > 1) parts[1].trim() else ""
            return first to last
        }

        val EmptyShippingInfoErpModel = ShippingInfoErpModel(
            name = "",
            firstname = "",
            lastname = "",
            street = "",
            addressDetail = "",
            zip = "",
            city = "",
            country = "DE",
            phone = "",
            mail = "",
            deliveryInfo = ""
        )

        fun ShippingInfoErpModel.toJson(): String = SafeJson.value.encodeToString(this)
    }
}
