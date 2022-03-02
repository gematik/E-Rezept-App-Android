/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shippingContact")
data class ShippingContactEntity(
    @PrimaryKey
    val id: Int = 0,
    val name: String = "",
    val line1: String = "",
    val line2: String = "",
    val postalCodeAndCity: String = "",
    val telephoneNumber: String = "",
    val mail: String = "",
    val deliveryInformation: String = ""
)
