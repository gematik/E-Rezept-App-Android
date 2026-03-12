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
    val name: String,
    val mail: String,
    val phone: String,
    val street: String,
    val addressDetail: String, // e.g., Apt/Floor/Company
    val city: String,
    val zip: String,
    val deliveryInfo: String
) {

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
        val EmptyShippingInfoErpModel = ShippingInfoErpModel(
            name = "",
            street = "",
            addressDetail = "",
            zip = "",
            city = "",
            phone = "",
            mail = "",
            deliveryInfo = ""
        )

        fun ShippingInfoErpModel.toJson(): String = SafeJson.value.encodeToString(this)
    }
}
