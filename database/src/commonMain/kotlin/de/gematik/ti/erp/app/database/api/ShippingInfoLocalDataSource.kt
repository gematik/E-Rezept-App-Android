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
package de.gematik.ti.erp.app.database.api

import de.gematik.ti.erp.app.shippingInfo.model.ShippingInfoErpModel
import kotlinx.coroutines.flow.Flow

/**
 * Local data source abstraction for the user's shipping contact information.
 * Mirrors the V1/V2 + bridge pattern used for pharmacies.
 */
interface ShippingInfoLocalDataSource {
    /** Observe the current shipping contact if present. */
    fun observeShippingInfo(): Flow<ShippingInfoErpModel?>

    /** Retrieve the current shipping contact once. */
    suspend fun getShippingInfo(): ShippingInfoErpModel?

    /** Save or replace the current shipping contact. */
    suspend fun saveShippingInfo(contact: ShippingInfoErpModel)

    /** Delete the current shipping contact if it exists. */
    suspend fun deleteShippingInfo()
}

/** Lightweight DTO to avoid cross-module dependency cycles. */
data class ShippingContactDto(
    val name: String,
    val line1: String,
    val line2: String,
    val postalCode: String,
    val city: String,
    val telephoneNumber: String,
    val mail: String,
    val deliveryInformation: String
)
