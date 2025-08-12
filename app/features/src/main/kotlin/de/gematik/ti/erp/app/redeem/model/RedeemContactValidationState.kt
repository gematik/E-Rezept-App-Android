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

import androidx.annotation.StringRes
import de.gematik.ti.erp.app.core.R

enum class RedeemContactValidationState(@StringRes val error: Int? = null) {
    NoError(null), // No error (valid or pickup)
    MissingOrderOption(R.string.pharmacy_order_no_pharmacy_error), // Missing order option, cannot validate
    MissingPersonalInfo(R.string.pharmacy_order_no_contact_error), // Name, address, postal code, city
    MissingPhone(R.string.pharmacy_order_no_telephone_error), // Phone or mail
    MissingDeliveryInfo(R.string.edit_shipping_contact_invalid_delivery_information); // Delivery instructions

    fun isValid(): Boolean = this == NoError
}
