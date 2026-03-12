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

package de.gematik.ti.erp.app.database.room.v2.shippinginfo

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shipping_info",
    indices = [
        // Helpful for lookups; keep non-unique in case multiple orders share details
        Index("mail", unique = false),
        Index("phone", unique = false),
        Index("city", unique = false),
        Index("zip", unique = false)
    ]
)
data class ShippingInfoEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val mail: String,
    val phone: String,

    val street: String,
    val addressDetail: String, // e.g., Apt/Floor/Company
    val city: String,
    val zip: String,

    val deliveryInfo: String // e.g., door code / drop-off note
)
