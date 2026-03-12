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

package de.gematik.ti.erp.app.pharmacy.model

import de.gematik.ti.erp.app.fhir.constant.SafeJson
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PharmacyErpModel(
    val lastUsed: Instant = Instant.DISTANT_PAST,
    val isFavorite: Boolean = false,
    val isOftenUsed: Boolean = false,
    val usageCount: Int = 0,
    val telematikId: String,
    val name: String,
    val address: PharmacyAddressErpModel?,
    val contact: ContactInformationErpModel?,
    val position: PositionErpModel? = null
) {
    fun singleLineAddress(): String {
        return if (address != null) {
            return "${address.lineAddress}\n${address.zip} ${address.city}"
        } else {
            ""
        }
    }

    companion object {
        fun PharmacyErpModel.toJson(): String = SafeJson.value.encodeToString(this)
    }
}

@Serializable
data class PositionErpModel(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class ContactInformationErpModel(
    val phone: String,
    val mail: String,
    val url: String
)

@Serializable
data class PharmacyAddressErpModel(
    val lineAddress: String = "",
    val zip: String?,
    val city: String?
) {
    companion object {
        fun String.toAddressErpModel(): PharmacyAddressErpModel {
            val regex = """^(.*)\n(\S+) (.*)$""".toRegex()
            val match = regex.find(this.trim()) ?: return PharmacyAddressErpModel("", null, null)
            val (line, postalCode, city) = match.destructured

            return PharmacyAddressErpModel(
                lineAddress = line,
                zip = postalCode,
                city = city
            )
        }
    }
}

data class TelematikId(val value: String)
